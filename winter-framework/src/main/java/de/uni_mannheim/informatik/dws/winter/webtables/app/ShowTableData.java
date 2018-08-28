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
package de.uni_mannheim.informatik.dws.winter.webtables.app;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.Parameter;

import au.com.bytecode.opencsv.CSVWriter;
import de.uni_mannheim.informatik.dws.winter.utils.Executable;
import de.uni_mannheim.informatik.dws.winter.utils.ProgressReporter;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.winter.utils.query.Func;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableContext;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.preprocessing.TableDisambiguationExtractor;
import de.uni_mannheim.informatik.dws.winter.webtables.preprocessing.TableNumberingExtractor;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.JsonTableWriter;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ShowTableData extends Executable {

	@Parameter(names = "-d")
	private boolean showData = false;
	
	@Parameter(names = "-w")
	private int columnWidth = 20;
	
	@Parameter(names = "-keyColumnIndex")
	private Integer keyColumnIndex = null;
	
	@Parameter(names = "-convertValues")
	private boolean convertValues = false;
	
	@Parameter(names = "-update")
	private boolean update = false;
	
	@Parameter(names = "-detectKey")
	private boolean detectKey = false;
	
	@Parameter(names = "-listColumnIds")
	private boolean listColumnIds;
	
	@Parameter(names = "-header")
	private boolean showHeader = false;
	
	@Parameter(names = "-rows")
	private int numRows = 0;
	
	@Parameter(names = "-csv")
	private boolean createCSV = false;
	
	@Parameter(names = "-dep")
	private boolean showDependencyInfo = false;
	
	@Parameter(names = "-prov")
	private boolean showProvenanceInfo = false;
	
	@Parameter(names = "-pre")
	private boolean applyPreprocessing = false;
	
	private static final Logger logger = WinterLogManager.getLogger();
	
	public static void main(String[] args) throws IOException {
		ShowTableData s = new ShowTableData();
		
		if(s.parseCommandLine(ShowTableData.class, args) && s.getParams()!=null) {
			
			s.run();
		}
	}
	
	public void run() throws IOException {
		
		JsonTableParser p = new JsonTableParser();
		JsonTableWriter w = new JsonTableWriter();
		// p.setConvertValues(convertValues | detectKey);

		CsvTableParser csvP = new CsvTableParser();
		// csvP.setConvertValues(convertValues | detectKey);
		
		String[] files = getParams().toArray(new String[getParams().size()]);
		
		File dir = null;
		if(files.length==1) {
			dir = new File(files[0]);
			if(dir.isDirectory()) {
				files = dir.list();
			} else {
				dir = null;
			}
		}
		
		ProgressReporter prg = new ProgressReporter(files.length, "Processing Tables");
		
		CSVWriter csvW = null;
		if(createCSV) {
			csvW = new CSVWriter(new OutputStreamWriter(System.out));
		}
		
		for(String s : files) {
			
			Table t = null;
			
			File f = new File(s);
			if(dir!=null) {
				f = new File(dir,s);
			}
			
			if(s.endsWith("json")) {
				t = p.parseTable(f);
			} else if(s.endsWith("csv")) {
				t = csvP.parseTable(f);
			} else {
				logger.error(String.format("Unknown table format '%s' (must be .json or .csv)", f.getName()));
				continue;
			}
			
			if(applyPreprocessing) {
				new TableDisambiguationExtractor().extractDisambiguations(Q.toList(t));
				new TableNumberingExtractor().extractNumbering(Q.toList(t));
			}
			
			if(convertValues) {
				t.convertValues();
			}

			// update the table if requested
			if(detectKey) {
				t.identifySubjectColumn(0.3,true);
				logger.error(String.format("* Detected Entity-Label Column: %s", t.getSubjectColumn()==null ? "?" : t.getSubjectColumn().getHeader()));
			}
			if(keyColumnIndex!=null) {
				logger.error(String.format("* Setting Entity-Label Column: %s", t.getSchema().get(keyColumnIndex)));
				t.setSubjectColumnIndex(keyColumnIndex);
			}
			if(update) {
				w.write(t, f);
			}
			
			if(createCSV) {
				// create a csv file with the table meta data
				csvW.writeNext(new String[] {
						s,
						Integer.toString(t.getRows().size()),
						Integer.toString(t.getColumns().size()),
						t.getContext().getUrl(),
						t.getContext().getPageTitle(),
						t.getContext().getTableTitle(),
						Integer.toString(getOriginalTables(t).size()),
						t.getSubjectColumn()!=null ? "" : Integer.toString(t.getSubjectColumn().getColumnIndex())
				});
			} else if(listColumnIds) {
				// list the columns in the table
				for(TableColumn c : t.getColumns()) {
					if(!showHeader) {
						logger.info(c.getIdentifier());
					} else {
						logger.info(c.toString());
					}
				}
			} else {
				// print the table meta data in human readable format

				TableContext ctx = t.getContext();
				
				logger.info(String.format("*** Table %s ***", s));
				if(ctx!=null) {
					logger.info(String.format("* URL: %s", ctx.getUrl()));
					logger.info(String.format("* Title: %s", ctx.getPageTitle()));
					logger.info(String.format("* Heading: %s", ctx.getTableTitle()));
				}
				logger.info(String.format("* # Columns: %d", t.getColumns().size()));
				logger.info(String.format("* # Rows: %d", t.getRows().size()));
				logger.info(String.format("* Created from %d original tables", getOriginalTables(t).size()));
				logger.info(String.format("* Entity-Label Column: %s", t.getSubjectColumn()==null ? "?" : t.getSubjectColumn().getHeader()));

				if(showProvenanceInfo) {
					// collect all provenance data
					Set<String> provenance = getOriginalTables(t);
					
					if(provenance.size()>0) {
						logger.info("Provenance:");
						logger.info(String.format("\t%s", 
								StringUtils.join(Q.sort(provenance), ",")
								));
					} else {
						logger.info("Table has no provenance data attached.");
					}
				}
				
				if(showDependencyInfo) {
					
					if(t.getSchema().getFunctionalDependencies()!=null && t.getSchema().getFunctionalDependencies().size()>0) {
						logger.info("*** Functional Dependencies ***");
						for(Collection<TableColumn> det : t.getSchema().getFunctionalDependencies().keySet()) {
							Collection<TableColumn> dep = t.getSchema().getFunctionalDependencies().get(det);
							logger.info(
									String.format(
											"{%s}->{%s}", 
											StringUtils.join(Q.project(det, new TableColumn.ColumnHeaderProjection()), ","),
											StringUtils.join(Q.project(dep, new TableColumn.ColumnHeaderProjection()), ",")));
						}
					}
					if(t.getSchema().getCandidateKeys()!=null && t.getSchema().getCandidateKeys().size()>0) {
						logger.info("*** Candidate Keys ***");
						for(Collection<TableColumn> candidateKey : t.getSchema().getCandidateKeys()) {
							logger.info(
									String.format("{%s}", StringUtils.join(Q.project(candidateKey, new TableColumn.ColumnHeaderProjection()), ",")));
						}
					}
				}

				if(showData) {
					logger.info(t.getSchema().format(columnWidth));
					logger.info(t.getSchema().formatDataTypes(columnWidth));
					
					int maxRows = Math.min(numRows, t.getRows().size());
					
					if(maxRows==0) {
						maxRows = t.getRows().size();
					}
					
					for(int i = 0; i < maxRows; i++) {
						TableRow r = t.getRows().get(i);
						if(showProvenanceInfo) {
							logger.info(StringUtils.join(r.getProvenance(), " / "));
						}
						logger.info(r.format(columnWidth));
					}
				} else {
					logger.info(StringUtils.join(Q.project(t.getColumns(), 
							new Func<String, TableColumn>() {
	
								@Override
								public String invoke(TableColumn in) {
									return String.format("%s (%s)", in.getHeader(), in.getDataType());
								}}
							), ", "));
				}
			
				prg.incrementProgress();
				prg.report();
			}
		}
		
		if(createCSV) {
			csvW.close();
		}
		
	}
	
	private Set<String> getOriginalTables(Table t) {
		
		Set<String> tbls = new HashSet<>();
		
		for(TableColumn c : t.getColumns()) {
			for(String prov : c.getProvenance()) {
				
				tbls.add(prov.split("~")[0]);
				
			}
		}
		
		return tbls;
	}
}
