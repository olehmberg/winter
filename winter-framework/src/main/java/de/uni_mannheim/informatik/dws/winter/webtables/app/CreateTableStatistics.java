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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.Parameter;
import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVWriter;
import de.uni_mannheim.informatik.dws.winter.utils.Executable;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableWithMappingSchema;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class CreateTableStatistics extends Executable {

	@Parameter(names = "-tables", required = true)
	private String tablesLocation;
	
	@Parameter(names = "-results", required = true)
	private String resultsLocation;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		CreateTableStatistics exe = new CreateTableStatistics();
		
		if(exe.parseCommandLine(CreateTableStatistics.class, args)) {
		
			exe.run();
			
		}
		
	}
	
	public void run() throws FileNotFoundException, IOException {
		
		CSVWriter resultStatisticsWriter = new CSVWriter(new FileWriter(new File(new File(resultsLocation), "table_statistics.csv"), true));
		
		for(File f : new File(tablesLocation).listFiles()) {
	        Gson gson = new Gson();
	        
	        String json = IOUtils.toString(new FileInputStream(f));
	        
	        // get the data from the JSON source
	        JsonTableSchema data = gson.fromJson(json, JsonTableSchema.class);
	        
	        // check if any data was parsed ... if the file used the schema with mappings, data will not have any contents
	        // but as no exception is thrown, we have to check attributes of data for null ...
	        if(data.getRelation()==null) {
	        	
	        	JsonTableWithMappingSchema moreData = gson.fromJson(json, JsonTableWithMappingSchema.class);
	        	
	        	data = moreData.getTable();
	        }
	        
	        if(data.getRelation()!=null) {
	        	
	        	int rows = 0;
	        	int cols = data.getRelation().length;
	        	
	        	for(String[] values : data.getRelation()) {
	        		rows = Math.max(values.length, rows);
	        	}
	        	
	        	rows -= data.getNumberOfHeaderRows();
	        	
    			resultStatisticsWriter.writeNext(new String[] {
    					new File(tablesLocation).getName(),
    					f.getName(),
    					Integer.toString(rows),
    					Integer.toString(cols)
    			});
	    		
	        }
		}
		
		resultStatisticsWriter.close();
	
		
	}
	
}
