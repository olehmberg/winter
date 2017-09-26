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
import com.beust.jcommander.Parameter;

import de.uni_mannheim.informatik.dws.winter.utils.Executable;
import de.uni_mannheim.informatik.dws.winter.utils.ProgressReporter;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.CSVTableWriter;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.JsonTableWriter;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.RdfN3TableWriter;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.RdfXmlTableWriter;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.TableWriter;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ConvertTable extends Executable {

	public static enum format
	{
		CSV,
		JSON,
		RDFN3,
		RDFXML
	}
	
	@Parameter(names = "-format", required=true)
	private format outputFormat;
	
	@Parameter(names = "-out", required=true)
	private String outputDirectory;
	
	public static void main(String[] args) throws Exception {
		ConvertTable ct = new ConvertTable();
		
		if(ct.parseCommandLine(ConvertTable.class, args)) {
			ct.run();
		}
	}
	
	public void run() throws Exception {
	
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
		
		CsvTableParser csvParser = new CsvTableParser();
		JsonTableParser jsonParser = new JsonTableParser();
		
		TableWriter writer;
		
		switch (outputFormat) {
		case CSV:
			writer = new CSVTableWriter();
			break;
		case JSON:
			writer = new JsonTableWriter();
			break;
		case RDFN3:
			writer = new RdfN3TableWriter();
			break;
		case RDFXML:
			writer = new RdfXmlTableWriter();
			break;
		default:
			System.err.println("Invalid output format specified!");
			return;
		}
		
		File outDir = new File(outputDirectory);
		outDir.mkdirs();
		
		ProgressReporter p = new ProgressReporter(files.length, "Converting Files");
		
		for(String file : files) {
			
			Table t = null;
			
			File f = new File(file);
			if(dir!=null) {
				f = new File(dir,file);
			}
			
			if(file.endsWith("csv")) {
				t = csvParser.parseTable(f);
			} else if(file.endsWith("json")) {
				t = jsonParser.parseTable(f);
			} else {
				System.err.println(String.format("Cannot parse table '%s' (file format must be 'csv' or 'json')!", file));
			}
			
			if(t!=null) {
				writer.write(t, new File(outDir, file));
			}
			
			p.incrementProgress();
			p.report();
		}
		
	}
	
}
