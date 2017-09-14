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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.webtables.ListHandler;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.lod.LodTableColumn;
import edu.stanford.nlp.util.StringUtils;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class LodCsvTableWriter implements TableWriter {

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.t2k.webtables.writers.TableWriter#write(de.uni_mannheim.informatik.dws.t2k.webtables.Table, java.io.File)
	 */
	@Override
	public File write(Table t, File f) throws IOException {
		if(!f.getName().endsWith(".csv")) {
			f = new File(f.getAbsolutePath() + ".csv");
		}
		
		return write(t,f,new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8")));
	}

	protected void writeLine(List<String> values, BufferedWriter w) throws IOException {
		w.write(String.format("\"%s\"\n", StringUtils.join(values, "\",\"")));		
		values.clear();
	}
	
	protected File write(Table t, File f, BufferedWriter w) throws IOException {

        List<String> values = new LinkedList<>();
        
        // write headers
        for(TableColumn c : t.getColumns()) {
            values.add(c.getHeader());
        }
        writeLine(values, w);
        
        // write URIs
        for(TableColumn c : t.getColumns()) {
            values.add(c.getUri());
        }
        writeLine(values, w);
        
        // write data types
        for(TableColumn c : t.getColumns()) {
        	if(c instanceof LodTableColumn) {
        		LodTableColumn lc = (LodTableColumn)c;
        		values.add(lc.getXmlType());
        	} else {
        		values.add("");
        	}
        }
        writeLine(values, w);
        
        // write property ranges
        for(TableColumn c : t.getColumns()) {
        	if(c instanceof LodTableColumn) {
        		LodTableColumn lc = (LodTableColumn)c;
        		values.add(lc.getRange());
        	} else {
        		values.add("");
        	}
        }
        writeLine(values, w);
        
        // write values
       for(TableRow r : t.getRows()) {
            
            values.clear();
            
            for(TableColumn c: t.getColumns()) {
                
            	Object value = r.get(c.getColumnIndex());
            	
            	if(value!=null) {
            		
            		if(ListHandler.isArray(value)) {
            			//TODO format list
            			
            			List<String> stringValues = new LinkedList<>();
            			for(Object v : (Object[])value) {
            				stringValues.add(formatValue(v, c));
            			}
            			
            			values.add(ListHandler.formatList(stringValues));
            			
            		} else {
            			values.add(formatValue(value, c));
            		}

            	} else {
            		values.add("NULL");
            	}
                
            }
            
            writeLine(values, w);
        }
        
        w.close();
		
        return f;
	}
	
	protected String formatValue(Object value, TableColumn c) {
		if(c.getDataType()==DataType.date) {
			LocalDateTime dt = (LocalDateTime)value;
			return dt.toLocalDate().toString();
		} else if(c.getDataType()==DataType.numeric) { 
		
			LodTableColumn lc = (LodTableColumn)c;
			switch (lc.getXmlType()) {
			case "XMLSchema#double":
        	case "XMLSchema#float":
        	case "minute":
        		return value.toString();
			default:
				Integer i = ((Double)value).intValue();
				return i.toString();
			}
		} else {
			return value.toString();
		}
	}


}