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
import java.io.OutputStreamWriter;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;

/**
 * 
 * Writes the table data as N3 RDF data.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RdfN3TableWriter implements TableWriter {

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.t2k.webtables.writers.TableWriter#write(de.uni_mannheim.informatik.dws.t2k.webtables.Table, java.io.File)
	 */
	@Override
	public File write(Table t, File f) throws IOException {
		if(!f.getName().endsWith(".n3")) {
			f = new File(f.getAbsolutePath() + ".n3");
		}
		
		return write(t,f,new FileWriter(f));
	}

	protected File write(Table t, File f, OutputStreamWriter w) throws IOException {
		
		// write prefixes
		String[][] prefixes = new String[][] {
			{ "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" }, 
			{ "rdfs", "http://www.w3.org/2000/01/rdf-schema#" },
			{ "source", "file://" + t.getPath() + "#" }
		};
		for(String[] pref : prefixes) {
			w.write(String.format("@prefix %s:\t<%s> .\n", pref[0], pref[1]));
		}
		
		// write schema
		w.write("source:type a rdfs:Class");
		for(TableColumn c : t.getColumns()) {
			w.write(String.format("source:column%d a rdf:Property; rdfs:label \"%s\" .\n", c.getColumnIndex(), c.getHeader()));
		}
		
		// write records
       for(TableRow r : t.getRows()) {
           
    	   StringBuilder sb = new StringBuilder();
    	   
    	   sb.append("[] a source:type");
    	   
            for(TableColumn c: t.getColumns()) {
                
            	Object value = r.get(c.getColumnIndex());
            	
            	if(value!=null) {
            		sb.append(String.format("; source:column%d \"%s\"", c.getColumnIndex(), value));
            	}
                
            }
            
            sb.append(" .\n");
            
            w.write(sb.toString());
        }
        
        w.close();
		
        return f;
	}

}
