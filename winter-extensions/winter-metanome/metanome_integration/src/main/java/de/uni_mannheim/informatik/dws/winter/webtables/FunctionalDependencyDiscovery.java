/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package de.uni_mannheim.informatik.dws.winter.webtables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// import de.hpi.isg.pyro.algorithms.Pyro;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
// import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
// import de.metanome.algorithm_integration.results.UniqueColumnCombination;
import de.metanome.algorithms.hyfd.HyFD;
// import de.metanome.algorithms.tane.TaneAlgorithm;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.utils.StringUtils;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.metanome.WebTableFileInputGenerator;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.CSVTableWriter;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class FunctionalDependencyDiscovery {

	public static Set<TableColumn> closure(Set<TableColumn> forColumns, Map<Set<TableColumn>, Set<TableColumn>> functionalDependencies) {
		return closure(forColumns, functionalDependencies, false);
	}

	public static Set<TableColumn> closure(Set<TableColumn> forColumns, Map<Set<TableColumn>, Set<TableColumn>> functionalDependencies, boolean log) {
		return closure(forColumns, Pair.fromMap(functionalDependencies), log);
	}

	/**
	 * Calculates the closure of the given subset of columns under the given set of functional dependencies.
	 * 
	 *  
	 * Let H be a heading, let F be a set of FDs with respect to H, and let Z be a subset of H. Then the closure Z+ of Z under F is the maximal subset C of H such that Z → C is implied by the FDs in F.
	 * 
	 * Z+ := Z ;
	 * do "forever" ;
	 * for each FD X → Y in F
	 * do ;
	 * if X is a subset of Z+
	 * then replace Z+ by the union of Z+ and Y ;
	 * end ;
	 * if Z+ did not change on this iteration
	 * then quit ;  // computation complete 
	 * end ;
	 * 
	 * from: "Database Design and Relational Theory", Chapter 7, O'Reilly
	 * 
	 * @param forColumns the subset of columns
	 * @param functionalDependencies the set of functional dependencies
	 * @param log specifies if details should be written to the console
	 * @return Returns a collection of all columns that are in the calculated closure (and are hence determined by the given subset of columns)
	 */	
	public static Set<TableColumn> closure(Set<TableColumn> forColumns, Collection<Pair<Set<TableColumn>, Set<TableColumn>>> functionalDependencies, boolean log) {
		
		Set<TableColumn> result = new HashSet<>();
		
		List<Pair<Collection<TableColumn>, Collection<TableColumn>>> trace = new LinkedList<>();

		if(functionalDependencies==null) {
			result = null;
		} else {
			result.addAll(forColumns);
			trace.add(new Pair<>(forColumns, null));
			
			int lastCount;
			do {
				lastCount = result.size();
			
				for(Pair<Set<TableColumn>,Set<TableColumn>> fd : functionalDependencies) {
					Collection<TableColumn> determinant = fd.getFirst();

					if(result.containsAll(determinant)) {
						Collection<TableColumn> dependant = fd.getSecond();
						
						Collection<TableColumn> newColumns = Q.without(dependant, result);

						result.addAll(dependant);

						
						if(newColumns.size()>0) {
							trace.add(new Pair<>(determinant, newColumns));
						}
					}
					
				}
				
			} while(result.size()!=lastCount);
		}
		
		if(log) {
			StringBuilder sb = new StringBuilder();
			for(Pair<Collection<TableColumn>,Collection<TableColumn>> step : trace) {
				if(step.getSecond()==null) {
					sb.append(String.format("{%s}", StringUtils.join(Q.project(step.getFirst(), (c)->c.toString()), ",")));
				} else {
					sb.append(String.format(" / {%s}->{%s}", 
						StringUtils.join(Q.project(step.getFirst(), (c)->c.toString()), ","),
						StringUtils.join(Q.project(step.getSecond(), (c)->c.toString()), ",")
					));
				}
			}
			System.out.println(String.format("[FunctionalDepdencyUtils.closure] %s", sb.toString()));
		}

		return result;
	}

	public static Set<TableColumn> minimise(Set<TableColumn> attributes, int numAttributesInRelation, Map<Set<TableColumn>,Set<TableColumn>> functionalDependencies) {
		Set<TableColumn> result = new HashSet<>(attributes);
		Iterator<TableColumn> it = result.iterator();

		while(it.hasNext()) {
			TableColumn candidate = it.next();
			Set<TableColumn> reduced = new HashSet<>(result);
			reduced.remove(candidate);
			if(closure(reduced, functionalDependencies).size()==numAttributesInRelation) {
				it.remove();
			}
		}

		return result;
	}

	public static Set<Set<TableColumn>> listCandidateKeys(Table t) {
		return listCandidateKeys(t.getSchema().getFunctionalDependencies(), new HashSet<>(t.getColumns()));
	}

	public static Set<Set<TableColumn>> listCandidateKeys(Map<Set<TableColumn>,Set<TableColumn>> functionalDependencies, Set<TableColumn> columns) {
		
		List<Set<TableColumn>> candidates = new LinkedList<>();

		Set<TableColumn> firstKey = minimise(columns, columns.size(), functionalDependencies);

		candidates.add(firstKey);

		int known = 1;
		int current = 0;

		while(current < known) {
			for(Set<TableColumn> det : functionalDependencies.keySet()) {
				if(det.size()>0) {
					Set<TableColumn> dep = functionalDependencies.get(det);
					Set<TableColumn> key = Q.union(det, Q.without(candidates.get(current), dep));

					boolean superKey = Q.any(candidates, (k)->key.containsAll(k));

					if(!superKey) {
						Set<TableColumn> newKey = minimise(key, columns.size(), functionalDependencies);
						candidates.add(newKey);
						known++;
					}
				}
			}
			current++;
		}

		return new HashSet<>(candidates);
	}

	public static void calculcateFunctionalDependencies(Collection<Table> tables, File csvLocation) throws Exception {
		PrintStream tmp = new PrintStream(new File("HyFD.out"));
		final PrintStream out = System.out;
		
		try {
			// calculate functional dependencies
			CSVTableWriter csvWriter = new CSVTableWriter();
			for(Table t : tables) {
				out.println(String.format("[calculcateFunctionalDependencies] calculating functional dependencies for table #%d %s {%s}", 
						t.getTableId(),
						t.getPath(),
						StringUtils.join(Q.project(t.getColumns(), new TableColumn.ColumnHeaderProjection()), ",")));
				
				File tableAsCsv = csvWriter.write(t, new File(csvLocation, t.getPath()));
				
				System.setOut(tmp);
				
				Map<Set<TableColumn>, Set<TableColumn>> fds = calculateFunctionalDependencies(t, tableAsCsv);
				t.getSchema().setFunctionalDependencies(fds);
				Set<Set<TableColumn>> candidateKeys = listCandidateKeys(t);
				
				if(candidateKeys.size()==0) {
					candidateKeys.add(new HashSet<>(t.getColumns()));
				}
				t.getSchema().setCandidateKeys(candidateKeys);
			}
		} catch(AlgorithmExecutionException e) {
			throw new Exception(e.getMessage());
		} finally {
			System.setOut(out);
		}

	}

	public static File taneRoot = null;

	public static void calculateApproximateFunctionalDependencies(Collection<Table> tables, File csvLocation, double errorThreshold) throws Exception {
		CSVTableWriter csvWriter = new CSVTableWriter();
		File taneDataLocation = new File(taneRoot, "original");
		File taneDescriptionLocation = new File(taneRoot, "descriptions");
		// File taneExec = new File(taneLocation, "bin/taneg3");
		// File tanePrepare = new File(taneLocation, "bin/select.perl");
		for(Table t : tables) {
			System.out.println(String.format("[calculateApproximateFunctionalDependencies] calculating functional dependencies for table #%d %s {%s}", 
					t.getTableId(),
					t.getPath(),
					StringUtils.join(Q.project(t.getColumns(), new TableColumn.ColumnHeaderProjection()), ",")));

			// write file
			// File tableAsCsv = csvWriter.write(t, new File(taneDataLocation, t.getPath()));
			File tableAsCsv = new File(taneDataLocation, t.getPath());
			BufferedWriter w = new BufferedWriter(new FileWriter(tableAsCsv));
			for(TableRow r : t.getRows()) {
				Object[] values = r.getValueArray();
				for(int i = 0; i < values.length; i++) {
					Object o = values[i];
					if(i>0) {
						w.write(",");
					}
					if(o!=null) {
						w.write(o.toString().replace(",", ""));
					}
				}
				w.write("\n");
			}
			w.close();

			// write description
			String descriptionFileName = t.getPath() + ".dsc";
			File description = new File(taneDescriptionLocation, descriptionFileName);
			w = new BufferedWriter(new FileWriter(description));
			w.write("Umask = 007\n");
			w.write(String.format("DataIn = ../original/%s\n", tableAsCsv.getName()));
			w.write("RemoveDuplicates = OFF\nAttributesOut = $BASENAME.atr\nStandardOut = ../data/$BASENAME.dat\nSavnikFlachOut = ../data/$BASENAME.rel\nNOOFDUPLICATES=1\n");
			w.close();

			// prepare dataset
			String cmd = "../bin/select.perl ../descriptions/" + descriptionFileName;
			System.out.println(String.format("%s$ %s", taneDataLocation.getAbsolutePath(), cmd));
			Process p = Runtime.getRuntime().exec(cmd, null, taneDataLocation);
			String line = null;
			BufferedReader r = null;
			r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while((line = r.readLine()) != null) {
				System.out.println(line);
			}
			r.close();
			r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while((line = r.readLine()) != null) {
				System.out.println(line);
			}
			r.close();
			
			// run tane
			String nameWithoutExtension = t.getPath().replaceAll("\\..{3,4}$", "");
			File dataLocation = new File(taneRoot, "data/" + nameWithoutExtension + ".dat");
			cmd = String.format("./bin/taneg3 11 %d %d %s %f", t.getRows().size(), t.getColumns().size(), dataLocation.getAbsolutePath(), errorThreshold);
			System.out.println(String.format("%s$ %s", taneRoot.getAbsolutePath(), cmd));
			p = Runtime.getRuntime().exec(cmd, null, taneRoot);

			Map<Set<TableColumn>, Set<TableColumn>> functionalDependencies = new HashMap<>();
			Set<Set<TableColumn>> keys = new HashSet<>();
			r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while((line = r.readLine()) != null) {
				System.out.println(line);
				// FDs lines always start with a number or ->
				String[] values = line.split("\\s");
				boolean isFdLine = false;

				if(line.startsWith("->")) {
					isFdLine = true;
				} else {
					try {
						Integer.parseInt(values[0]);
						isFdLine = true;
					} catch(NumberFormatException ex) { isFdLine = false; }
				}

				if(isFdLine) {
					Set<TableColumn> det = new HashSet<>();
					TableColumn dep = null;

					boolean depStart = false;
					for(int i = 0; i < values.length; i++) {
						if(depStart) {
							int idx = Integer.parseInt(values[i]) - 1;
							dep = t.getSchema().get(idx);
							break;
						} else {
							if("->".equals(values[i])) {
								depStart = true;
							} else {
								int idx = Integer.parseInt(values[i]) - 1;
								det.add(t.getSchema().get(idx));
							}
						}
					}

					Set<TableColumn> mergedDep = null;
					// check if we already have a dependency with the same determinant
					if(functionalDependencies.containsKey(det)) {
						// if so, we add the dependent to the existing dependency
						mergedDep = functionalDependencies.get(det);
					} 
					if(mergedDep==null) {
						// otherwise, we create a new dependency
						mergedDep = new HashSet<>();
						functionalDependencies.put(det, mergedDep);
					}
					mergedDep.add(dep);
					
					System.out.println(String.format("{%s}->{%s}",
						StringUtils.join(Q.project(det, (c)->c.getHeader()), ","),
						StringUtils.join(Q.project(mergedDep, (c)->c.getHeader()), ",")
					));

					if(line.contains("key")) {
						keys.add(det);
					}
				}
			}
			r.close();
			r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while((line = r.readLine()) != null) {
				System.out.println(line);
			}
			r.close();
			
			t.getSchema().setFunctionalDependencies(functionalDependencies);
			t.getSchema().setCandidateKeys(keys);
		}
	}
	public static Map<Set<TableColumn>, Set<TableColumn>> calculateFunctionalDependencies(final Table t, File tableAsCsv) throws Exception {
		return calculateFunctionalDependencies(t, tableAsCsv, null);
	}

	public static Map<Set<TableColumn>, Set<TableColumn>> calculateFunctionalDependencies(final Table t, File tableAsCsv, final Set<Pair<Set<TableColumn>, Set<TableColumn>>> fds) throws Exception {
		HyFD dep = new HyFD();
		dep.setBooleanConfigurationValue(HyFD.Identifier.VALIDATE_PARALLEL.name(), true);
		final Map<Set<TableColumn>, Set<TableColumn>> functionalDependencies = new HashMap<>();

		try {		
			RelationalInputGenerator input = new WebTableFileInputGenerator(tableAsCsv);
			dep.setRelationalInputConfigurationValue(HyFD.Identifier.INPUT_GENERATOR.name(), input);
			dep.setResultReceiver(new FunctionalDependencyResultReceiver() {
				
				@Override
				public void receiveResult(FunctionalDependency arg0)
						throws CouldNotReceiveResultException, ColumnNameMismatchException {
					
					synchronized (this) {
						Set<TableColumn> det = new HashSet<>();
						
						// identify determinant
						for(ColumnIdentifier ci : arg0.getDeterminant().getColumnIdentifiers()) {						    		
							Integer colIdx = Integer.parseInt(ci.getColumnIdentifier());
					
							det.add(t.getSchema().get(colIdx));
						}

						// add dependant
						Set<TableColumn> dep = null;
						// check if we already have a dependency with the same determinant
						if(functionalDependencies.containsKey(det)) {
							// if so, we add the dependent to the existing dependency
							dep = functionalDependencies.get(det);
						} 
						if(dep==null) {
							// otherwise, we create a new dependency
							dep = new HashSet<>();
							functionalDependencies.put(det, dep);
						}
						Integer colIdx = Integer.parseInt(arg0.getDependant().getColumnIdentifier());
						dep.add(t.getSchema().get(colIdx));
						if(fds!=null) {
							fds.add(new Pair<>(det, dep));
						}
					}
				}
				
				@Override
				public Boolean acceptedResult(FunctionalDependency arg0) {
					return true;
				}
			});
			
			dep.execute();
		} catch(AlgorithmExecutionException e) {
			throw new Exception(e.getMessage());
		}
		
		return functionalDependencies;
	}

}