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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import au.com.bytecode.opencsv.CSVWriter;

import com.beust.jcommander.Parameter;

import de.uni_mannheim.informatik.dws.winter.utils.Executable;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableMapping;
import de.uni_mannheim.informatik.dws.winter.webtables.features.Feature;
import de.uni_mannheim.informatik.dws.winter.webtables.features.HorizontallyStackedFeature;
import de.uni_mannheim.informatik.dws.winter.webtables.features.ListFeature;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableParser;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class FeatureGenerator extends Executable {

	@Parameter(names = "-web")
	private String webTablesLocation;
	
	@Parameter(names = "-list")
	private boolean calculateList;
	
	@Parameter(names = "-horizontallyStacked")
	private boolean calculateHorizontallyStacked;
	
	@Parameter(names = "-rowNumbers")
	private String rowNumbersFile;
	
//	@Parameter(names = "-out")
//	private String outputLocation;
	
	public static void main(String[] args) throws IOException {
		FeatureGenerator g = new FeatureGenerator();
		
		if(g.parseCommandLine(FeatureGenerator.class, args)) {
			g.run();
		}
	}
	
	public void run() throws IOException {
		File source = new File(webTablesLocation);
		int done = 0;
		
		if(source.exists()) {

			LinkedList<String> names = new LinkedList<>();
			LinkedList<Feature> features = new LinkedList<>();
			
			names.add("TableName");
			
			if(calculateList) {
				names.add("List");
				features.add(new ListFeature());
			}
			
			if(calculateHorizontallyStacked) {
				names.add("HorizontallyStacked");
				features.add(new HorizontallyStackedFeature());
			}
			
			CsvTableParser csvParser = new CsvTableParser();
			JsonTableParser jsonParser = new JsonTableParser();
			
//			CSVWriter w = new CSVWriter(new FileWriter(new File(outputLocation)));
			CSVWriter w = new CSVWriter(new OutputStreamWriter(System.out));
			String[] header = new String[names.size()];
			names.toArray(header);
			w.writeNext(header);
			
			File[] tableFiles = null;
			
			if(source.isDirectory()) {
				tableFiles = source.listFiles();
			} else {
				tableFiles = new File[] { source };
			}
			
			CSVWriter wRow = null;
			
			if(rowNumbersFile!=null) {
				wRow = new CSVWriter(new FileWriter(new File(rowNumbersFile)));
				wRow.writeNext(new String[] { "TableName", "InstanceUri", "RowNumber" });
			}
			
			int progressStep = Math.max(10000, tableFiles.length/100);
			
			for(File tableFile : tableFiles) {
				Table t = null;
				
				try {
					if(tableFile.getName().endsWith("csv")) {
						t = csvParser.parseTable(tableFile);
					} else if(tableFile.getName().endsWith("json")) {
						t = jsonParser.parseTable(tableFile);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				if(t==null) {
					System.err.println(String.format("Unknown input format: %s", tableFile.getName()));
					continue;
				}
				
				String[] values = new String[names.size()];
				values[0] = tableFile.getName();
				boolean anyValue = false;
				for(int i = 0; i < features.size(); i++) {
					double value = features.get(i).calculate(t);
					if(value!=0.0) {
						anyValue=true;
					}
					values[i+1] = Double.toString(value);
				}
				
				if(anyValue) {
					w.writeNext(values);
				}
				
				/*
				 * Process Table Mapping (CSV only) 
				 */
				
				if(tableFile.getName().endsWith("csv") && wRow!=null) {
					TableMapping tm = TableMapping.read(tableFile.getAbsolutePath());
					for(int row = 0; row < tm.getMappedInstances().length; row++) {
						if(tm.getMappedInstance(row)!=null) {
							wRow.writeNext(new String[] { tableFile.getName(), tm.getMappedInstance(row).getFirst(), Integer.toString(row)});
						}
					}
				}
				
				if(done%progressStep==0) {
					System.err.println(String.format("%2.2f%%: %s", (float)done/(float)tableFiles.length*100.0, tableFile.getName()));
					w.flush();
				}
				done++;
			}
			
			w.close();
			
			if(wRow!=null) {
				wRow.close();
			}
		} else {
			System.err.println("Could not find web tables!");
		}
	}
}
