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
package de.uni_mannheim.informatik.dws.winter.webtables.writers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;

/**
 * Writes a Web Table in the CSV format.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class CSVTableWriter implements TableWriter {

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.t2k.webtables.writers.TableWriter#write(de.uni_mannheim.informatik.dws.t2k.webtables.Table, java.io.File)
	 */
	@Override
	public File write(Table t, File f) throws IOException {
		if(!f.getName().endsWith(".csv")) {
			f = new File(f.getAbsolutePath() + ".csv");
		}
		
		return write(t,f,new CSVWriter(new FileWriter(f)));
	}
	
	public File write(Table t, File f, char separator, char quoteChar, char escapeChar) throws IOException {
		
		if(!f.getName().endsWith(".csv")) {
			f = new File(f.getAbsolutePath() + ".csv");
		}
		
		return write(t,f,new CSVWriter(new FileWriter(f), separator, quoteChar, escapeChar));
	}

	protected File write(Table t, File f, CSVWriter w) throws IOException {

//		if(!f.getName().endsWith(".csv")) {
//			f = new File(f.getAbsolutePath() + ".csv");
//		}
		
//        CSVWriter w = new CSVWriter(new FileWriter(f));
        
        List<String> values = new LinkedList<>();
        
        // write headers
        for(TableColumn c : t.getColumns()) {
            values.add(c.getHeader());
        }
        w.writeNext(values.toArray(new String[values.size()]));
        
        // write values
       for(TableRow r : t.getRows()) {
            
            values.clear();
            
            for(TableColumn c: t.getColumns()) {
                
            	Object value = r.get(c.getColumnIndex());
            	
            	if(value!=null) {
            		values.add(value.toString());
            	} else {
            		values.add(null);
            	}
                
            }
            
            w.writeNext(values.toArray(new String[values.size()]));
        }
        
        w.close();
		
        return f;
	}

}
