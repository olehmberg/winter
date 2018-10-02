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


import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithms.hyfd.HyFD;
import de.metanome.algorithms.tane.TaneAlgorithm;
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

public static void calculateApproximateFunctionalDependencies(Collection<Table> tables, File csvLocation, double errorThreshold) throws Exception {
		PrintStream tmp = new PrintStream(new File("TANE.out"));
		final PrintStream out = System.out;
		
		try {
			// calculate functional dependencies
			CSVTableWriter csvWriter = new CSVTableWriter();
			for(Table t : tables) {
				out.println(String.format("[calculateApproximateFunctionalDependencies] calculating functional dependencies for table #%d %s {%s}", 
						t.getTableId(),
						t.getPath(),
						StringUtils.join(Q.project(t.getColumns(), new TableColumn.ColumnHeaderProjection()), ",")));
				
				File tableAsCsv = csvWriter.write(t, new File(csvLocation, t.getPath()));
				
				System.setOut(tmp);
				
				Map<Set<TableColumn>, Set<TableColumn>> fds = calculateApproximateFunctionalDependencies(t, tableAsCsv, errorThreshold);
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

	public static Map<Set<TableColumn>, Set<TableColumn>> calculateApproximateFunctionalDependencies(final Table t, File tableAsCsv, double errorThreshold) throws Exception {
		TaneAlgorithm tane = new TaneAlgorithm();
		tane.setErrorThreshold(errorThreshold);
		
		final Map<Set<TableColumn>, Set<TableColumn>> functionalDependencies = new HashMap<>();
		
		try {
			RelationalInputGenerator input = new WebTableFileInputGenerator(tableAsCsv);
			tane.setRelationalInputConfigurationValue(TaneAlgorithm.INPUT_TAG, input);
			tane.setResultReceiver(new FunctionalDependencyResultReceiver() {
				
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
					
					}
				}
				
				@Override
				public Boolean acceptedResult(FunctionalDependency arg0) {
					return true;
				}
			});
			
			tane.execute();
		} catch(AlgorithmExecutionException e) {
			throw new Exception(e.getMessage());
		}
		
		return functionalDependencies;
	}

public static Map<Set<TableColumn>, Set<TableColumn>> calculateFunctionalDependencies(final Table t, File tableAsCsv) throws Exception {
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