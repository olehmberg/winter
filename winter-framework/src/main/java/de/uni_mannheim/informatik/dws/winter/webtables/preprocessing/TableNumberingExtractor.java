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

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableNumberingExtractor {

	private static final Pattern numberingPattern = Pattern.compile("(\\d*)\\. (.+)");

	public void removeNumbering(Collection<Table> tables) {
		for(Table t : tables) {			
			// collect all numberings
			for(TableRow r : t.getRows()) {
				
				for(TableColumn c : t.getColumns()) {
					
					Object value = r.get(c.getColumnIndex());
					if(value!=null && !value.getClass().isArray()) { // ignore arrays for now
						
						String stringValue = value.toString();
						Matcher m = numberingPattern.matcher(stringValue);
						
						if(m.matches()) {
							
							String number = m.group(1);
							String rest = m.group(2).trim();

							// remove the numbering from the cell value
							r.set(c.getColumnIndex(), rest);
						}
						
					}
					
				}
				
			}
		}
	}

	public Map<Integer, Map<Integer, TableColumn>> extractNumbering(Collection<Table> tables
			) {

		// maps table id -> column id -> disambiguation column
		Map<Integer, Map<Integer, TableColumn>> tableToColumnToNumbering = new HashMap<>();
		
		for(Table t : tables) {
			
			Map<TableColumn, Map<TableRow, String>> numberings = new HashMap<>();
			
			// collect all numberings
			for(TableRow r : t.getRows()) {
				
				for(TableColumn c : t.getColumns()) {
					
					Object value = r.get(c.getColumnIndex());
					if(value!=null && !value.getClass().isArray()) { // ignore arrays for now
						
						String stringValue = value.toString();
						Matcher m = numberingPattern.matcher(stringValue);
						
						if(m.matches()) {
							
							String number = m.group(1);
							String rest = m.group(2).trim();
							
							Map<TableRow, String> innerMap = MapUtils.get(numberings, c, new HashMap<>());
							innerMap.put(r, number);
						
							// remove the numbering from the cell value
							r.set(c.getColumnIndex(), rest);
						}
						
					}
					
				}
				
			}
			
			Map<TableColumn, TableColumn> newColumns = new HashMap<>();
			
			// decide which new columns to create
			for(TableColumn c : numberings.keySet()) {
				Map<TableRow, String> values = numberings.get(c);
				
				double percent = values.size() / (double)t.getRows().size();
				
				if(percent>=0.05) {
					
					TableColumn newCol = new TableColumn(t.getColumns().size(), t);
					newCol.setDataType(DataType.numeric);
					
					if(c.getHeader()!=null && !"".equals(c.getHeader())) {
						newCol.setHeader(String.format("Context # of %s", c.getHeader()));
					}
					
					t.insertColumn(newCol.getColumnIndex(), newCol);
					
					newColumns.put(c, newCol);
					
					Map<Integer, TableColumn> columnToNumbering = MapUtils.get(tableToColumnToNumbering, t.getTableId(), new HashMap<>());
					columnToNumbering.put(c.getColumnIndex(), newCol);
				}
						
			}
			
			// fill new columns with values
			for(TableRow r : t.getRows()) {
				
				for(TableColumn c : numberings.keySet()) {
					
					TableColumn c2 = newColumns.get(c);
					
					if(c2!=null) {
					
						Map<TableRow, String> values = numberings.get(c);
						
						String value = values.get(r);
						
						if(value!=null) {
							
							r.set(c2.getColumnIndex(), value);
							
						}
					
					}
				}
				
			}
			
		}
		
		return tableToColumnToNumbering;
	}
}
