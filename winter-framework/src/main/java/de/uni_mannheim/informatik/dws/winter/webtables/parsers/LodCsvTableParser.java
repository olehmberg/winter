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
package de.uni_mannheim.informatik.dws.winter.webtables.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.utils.StringCache;
import de.uni_mannheim.informatik.dws.winter.webtables.ListHandler;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableMapping;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.lod.LodTableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.lod.LodTableRow;

public class LodCsvTableParser extends TableParser {

	//TODO change implementation to make use of all features provided by the TableParser class
	
	public static String delimiter = "\",\"";
	
	private boolean useStringCache = true;
	private boolean parseLists = false;
	private boolean useRowIndexFromFile = true;
	
	/**
	 * @param parseLists the parseLists to set
	 */
	public void setParseLists(boolean parseLists) {
		this.parseLists = parseLists;
	}
	
	/**
	 * Specifies how the row numbers are determined
	 * @param useRowIndexFromFile 
	 * 					if true, the row number is the same as the line number from the file that was loaded
	 *					if false, the row number is the row index in the table 						
	 */
	public void setUseRowIndexFromFile(boolean useRowIndexFromFile) {
		this.useRowIndexFromFile = useRowIndexFromFile;
	}
	
	public void setUseStringCache(boolean use) {
		useStringCache = use;
	}
	
    public Table parseTable(File file) {
        Reader r = null;
        Table t = null;
        try {
            if (file.getName().endsWith(".gz")) {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
                r = new InputStreamReader(gzip, "UTF-8");
            } else {
                r = new InputStreamReader(new FileInputStream(file), "UTF-8");
            }
            
            t = parseTable(r, file.getName());
            
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return t;
    }
    
    public Table parseTable(Reader reader, String fileName) throws IOException {        
        // create new table
        Table table = new Table();
        // take care of the header of the table
        table.setPath(fileName);

        // table may contain additional annotations (currently not used for DBpedia tables)
        TableMapping tm = new TableMapping();
        
        try {
            String[] columnNames;
            String[] columntypes;
            String[] columnURIs;
            String[] columnRanges;

            BufferedReader in = new BufferedReader(reader);

            // read the property names
            String fileLine = in.readLine();
            columnNames = fileLine.split(delimiter);

            // skip annotations
            boolean isMetaData = columnNames[0].startsWith("#");
            // if the current line starts with #, check for valid annotations
            while(isMetaData) {
                isMetaData = false;
                
                // check all valid annotations
                for(String s : TableMapping.VALID_ANNOTATIONS) {
                    if(columnNames[0].startsWith(s)) {
                        isMetaData = true;
                        break;
                    }
                }
                
                // if the current line is an annotation, read the next line and start over
                if(isMetaData) {
                	tm.parseMetadata(fileLine);
                	
                    fileLine = in.readLine();
                    columnNames = fileLine.split(delimiter);
                    isMetaData = columnNames[0].startsWith("#");
                }
            }
            
            // read the property URIs
            columnURIs = in.readLine().split(delimiter);

            // read the datatypes
            fileLine = in.readLine();
            columntypes = fileLine.split(delimiter);

            // skip the last header (range)
            fileLine = in.readLine();
            columnRanges = fileLine.split(delimiter);
            
            // process all properties (=columns)
            int i = 0;
            for (String columnName : columnNames) {

                // replace trailing " for the last column
                columntypes[i] = columntypes[i].replace("\"", "");
                columnURIs[i] = columnURIs[i].replace("\"", "");
                columnRanges[i] = columnRanges[i].replace("\"", "");
                columnName = columnName.replace("\"", "");

                // create the column
                LodTableColumn c = new LodTableColumn(i, table);
                c.setHeader(columnName);
                
                if(columnName.endsWith("_label")) {
                	c.setReferenceLabel(true);
                }
                c.setUri(columnURIs[i]);
                c.setXmlType(columntypes[i]);
                c.setRange(columnRanges[i]);
                
                // set the type if it's a primitive
                //TODO what about other primitive types?
                String datatype = columntypes[i];
                switch (datatype) {
                case "XMLSchema#date":
                case "XMLSchema#gYear":
                    c.setDataType(DataType.date);
                    break;
                case "XMLSchema#double":
                case "XMLSchema#float":
                case "XMLSchema#nonNegativeInteger":
                case "XMLSchema#positiveInteger":
                case "XMLSchema#integer":    
                case "XMLSchema#negativeInteger": 
                case "minute":
                    c.setDataType(DataType.numeric);
                    break;
                case "XMLSchema#string":
                case "rdf-schema#Literal":
                    c.setDataType(DataType.string);                    
                    break;
                default:                    
                    c.setDataType(DataType.unknown);
                }

                // add the column to the table
                table.addColumn(c);
                i++;
            }
            
            int row = 4;
            if(!useRowIndexFromFile) {
            	row = 0;
            }
            Object[] values;
            
            // read the table rows
            while ((fileLine = in.readLine()) != null) {
                
            	// handle the column splitting
            	fileLine = fileLine.substring(1, fileLine.length() - 1);
                String[] stringValues = fileLine.split(delimiter);  
                
                // create the value array
                values = new Object[columnNames.length];
                
                // transfer values
            	for (int j = 0; j < stringValues.length; j++) {
					if(stringValues[j].equalsIgnoreCase("NULL")) {
						values[j] = null;
					} else {
						if(parseLists && ListHandler.checkIfList(stringValues[j])) {
							// value list
							String[] list = ListHandler.splitList(stringValues[j]);
							Object[] listValues = new Object[list.length];
							
							for(int listIndex = 0; listIndex < list.length; listIndex++) {
								if(useStringCache) {
									listValues[listIndex] = StringCache.get(list[listIndex]);
								} else {
									listValues[j] = list[listIndex];
								}
							}
							
							values[j] = listValues;
						} else {
							// single value
							if(useStringCache) {
								values[j] = StringCache.get(stringValues[j]);
							} else {
								values[j] = stringValues[j];
							}
						}
					}
				}
                
            	// create the row, set the values and add it to the table
                TableRow r = new LodTableRow(row++, table);
                r.set(values);
                table.addRow(r);
            }
            
        } catch(Exception ex) {
        	ex.printStackTrace();
        }
        
        reader.close();
        
        if(isConvertValues()) {
        	table.inferSchemaAndConvertValues();
        }
        
        return table;
    }
	
    public static void endLoadData() {
    	StringCache.clear();
    }
}
