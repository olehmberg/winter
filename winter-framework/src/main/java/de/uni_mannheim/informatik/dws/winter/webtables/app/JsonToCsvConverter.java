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

import com.beust.jcommander.Parameter;

import de.uni_mannheim.informatik.dws.winter.utils.Executable;
import de.uni_mannheim.informatik.dws.winter.utils.FileUtils;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.CSVTableWriter;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class JsonToCsvConverter extends Executable {

	@Parameter(names = "-json", required=true)
	private String jsonLocation;
	
	@Parameter(names = "-result", required=true)
	private String resultLocation;
	
	public static void main(String[] args) throws IOException {
		JsonToCsvConverter conv = new JsonToCsvConverter();
		
		if(conv.parseCommandLine(JsonToCsvConverter.class, args)) {
			
			conv.run();
			
		}
	}
	
	public void run() throws IOException {
		
		File jsonFile = new File(jsonLocation);
		File resultFile = new File(resultLocation);
		
		if(resultFile.exists()) {
			 resultFile.mkdirs();
		}
		
		JsonTableParser p = new JsonTableParser();
		p.setConvertValues(false);
		
		CSVTableWriter w = new CSVTableWriter();
		
		for(File f : FileUtils.listAllFiles(jsonFile)) {
			System.out.println(String.format("Converting %s", f.getName()));
			
			Table t = p.parseTable(f);
			
			w.write(t, new File(resultFile, t.getPath()));
		}
		
	}
	
}
