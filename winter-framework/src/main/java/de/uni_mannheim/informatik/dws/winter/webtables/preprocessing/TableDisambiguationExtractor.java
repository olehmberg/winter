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
package de.uni_mannheim.informatik.dws.winter.webtables.preprocessing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;

/**
 * Extracts disambiguations from cell values into new columns 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableDisambiguationExtractor {

	private final static Pattern bracketsPattern = Pattern.compile(".*\\(([^)]*)\\).*");
	private final static Pattern bracketsPattern2 = Pattern.compile("\\(([^)]*)\\)");
	
	public void removeDisambiguations(Collection<Table> tables) {
		for(Table t : tables) {
			
			// collect all disambiguations
			for(TableRow r : t.getRows()) {
				
				for(TableColumn c : t.getColumns()) {
					
					Object value = r.get(c.getColumnIndex());
					if(value!=null && !value.getClass().isArray()) { // ignore arrays for now
						
						String stringValue = value.toString();
						Matcher m = bracketsPattern.matcher(stringValue);
						
						if(m.matches()) {
							// remove the disambiguation from the cell value
							stringValue = bracketsPattern2.matcher(stringValue).replaceAll("").trim();
							if(stringValue.trim().isEmpty()) {
								stringValue = null;
							}
							r.set(c.getColumnIndex(), stringValue);
						}
						
					}
					
				}
				
			}
		}
	}

	public Map<Integer, Map<Integer, TableColumn>> extractDisambiguations(Collection<Table> tables
			) {
		
		// maps table id -> column id -> disambiguation column
		Map<Integer, Map<Integer, TableColumn>> tableToColumnToDisambiguation = new HashMap<>();
		
		for(Table t : tables) {
			
			Map<TableColumn, Map<TableRow, String>> disambiguations = new HashMap<>();
			
			// collect all disambiguations
			for(TableRow r : t.getRows()) {
				
				for(TableColumn c : t.getColumns()) {
					
					Object value = r.get(c.getColumnIndex());
					if(value!=null && !value.getClass().isArray()) { // ignore arrays for now
						
						String stringValue = value.toString();
						Matcher m = bracketsPattern.matcher(stringValue);
						
						if(m.matches()) {
							
							String disambiguation = m.group(1);
							
							Map<TableRow, String> innerMap = MapUtils.get(disambiguations, c, new HashMap<>());
							innerMap.put(r, disambiguation);
						
							// remove the disambiguation from the cell value
							stringValue = bracketsPattern2.matcher(stringValue).replaceAll("").trim();
							if(stringValue.trim().isEmpty()) {
								stringValue = null;
							}
							r.set(c.getColumnIndex(), stringValue);
						}
						
					}
					
				}
				
			}
			
			Map<TableColumn, TableColumn> newColumns = new HashMap<>();
			
			// decide which new columns to create
			for(TableColumn c : disambiguations.keySet()) {
				Map<TableRow, String> values = disambiguations.get(c);
				
				double percentDisambiguated = values.size() / (double)t.getRows().size();
				
				if(percentDisambiguated>=0.05) {
					TableColumn newCol = createDisambiguationColumn(c);
					
					t.insertColumn(newCol.getColumnIndex(), newCol);
					
					newColumns.put(c, newCol);
					
					Map<Integer, TableColumn> columnToDisambiguation = MapUtils.get(tableToColumnToDisambiguation, t.getTableId(), new HashMap<>());
					columnToDisambiguation.put(c.getColumnIndex(), newCol);
				}
						
			}
			
			// fill new columns with values
			for(TableRow r : t.getRows()) {
				
				for(TableColumn c : disambiguations.keySet()) {
					
					TableColumn c2 = newColumns.get(c);
					
					if(c2!=null) {
					
						Map<TableRow, String> values = disambiguations.get(c);
						
						String value = values.get(r);
						
						if(value!=null) {
							
							r.set(c2.getColumnIndex(), value);
							
						}
					
					}
				}
				
			}
			
		}
		
		return tableToColumnToDisambiguation;
	}

	public TableColumn createDisambiguationColumn(TableColumn forColumn) {
		TableColumn newCol = new TableColumn(forColumn.getTable().getColumns().size(), forColumn.getTable());
		newCol.setDataType(forColumn.getDataType());
		
		if(forColumn.getHeader()!=null && !"".equals(forColumn.getHeader())) {
			newCol.setHeader(String.format("Disambiguation of %s", forColumn.getHeader()));
		}
		
		return newCol;
	}

}
