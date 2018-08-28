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
package de.uni_mannheim.informatik.dws.winter.webtables;

import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents the schema of a Web Table.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableSchema implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<TableColumn> columns;
	private HashMap<String, TableColumn> columnsById;
	
	private Map<Set<TableColumn>, Set<TableColumn>> functionalDependencies;
	private Set<Set<TableColumn>> candidateKeys;
	
	public TableSchema() {
		columns = new ArrayList<>();
		columnsById = new HashMap<>();
		functionalDependencies = new HashMap<>();
		candidateKeys = new HashSet<>();
	}
	
	public void addColumn(TableColumn column) {
		columns.add(column);
		columnsById.put(column.getIdentifier(), column);
	}
	
	/**
	 * Inserts a column at the given position. Does not update the table rows to conform to the new schema!
	 * @param index
	 * @param column
	 */
	protected void insertColumn(int index, TableColumn column) {
		for(TableColumn c : columns) {
			if(c.getColumnIndex()>=index) {
				c.setColumnIndex(c.getColumnIndex()+1);
			}
		}
		column.setColumnIndex(index);
		columns.add(index, column);
		columnsById.put(column.getIdentifier(), column);
	}
	
	/**
	 * Removes a column. Does not update the table rows to conform to the new schema!
	 * @param column
	 */
	public void removeColumn(TableColumn column) {
		// remove column by index instead of id (unfortunately, the LodCsv-Tables contain multiple columns with the same URI, which is also their id)
		Iterator<TableColumn> colIt = columns.iterator();
		while(colIt.hasNext()) {
			if(colIt.next().getColumnIndex()==column.getColumnIndex()) {
				colIt.remove();
			}
		}

		// remove the columns from the FDs
		Set<Entry<Set<TableColumn>, Set<TableColumn>>> oldMap = functionalDependencies.entrySet();
		functionalDependencies = new HashMap<>();
		for(Entry<Set<TableColumn>, Set<TableColumn>> e : oldMap) {
			// if a column was removed, also update the FDs
			Set<TableColumn> det = new HashSet<>(Q.where(e.getKey(), (c)->columns.contains(c)));
			Set<TableColumn> dep = new HashSet<>(Q.where(e.getValue(), (c)->columns.contains(c)));
			functionalDependencies.put(det, dep);
		}

		// update candidate keys
		Set<Set<TableColumn>> oldKeys = candidateKeys;
		candidateKeys = new HashSet<>();
		for(Set<TableColumn> key : oldKeys) {
			candidateKeys.add(new HashSet<>(Q.where(key, (c)->columns.contains(c))));
		}

		// update column indices		
		for(TableColumn c: columns) {
			if(c.getColumnIndex()>column.getColumnIndex()) {
				c.setColumnIndex(c.getColumnIndex()-1);
			}
		}

		updateIdentifiers();
	}
	
	/**
	 * Re-builds all HashMaps and HashSets that use the column identifiers to reflect changes
	 */
	protected void updateIdentifiers() {
		// update columns by id
		columnsById.clear();
		for(TableColumn c: columns) {
			c.updateIdentifier();
			columnsById.put(c.getIdentifier(), c);
		}

		// update functional dependencies: update the hash values by re-inserting all data into a new HashMap
		Set<Entry<Set<TableColumn>, Set<TableColumn>>> oldMap = functionalDependencies.entrySet();
		functionalDependencies = new HashMap<>();
		for(Entry<Set<TableColumn>, Set<TableColumn>> e : oldMap) {
			Set<TableColumn> det = new HashSet<>(e.getKey());
			Set<TableColumn> dep = new HashSet<>(e.getValue());
			functionalDependencies.put(det, dep);
		}

		// update candidate keys
		Set<Set<TableColumn>> oldKeys = candidateKeys;
		candidateKeys = new HashSet<>();
		for(Set<TableColumn> key : oldKeys) {
			candidateKeys.add(new HashSet<>(key));
		}
	}

	public TableColumn get(int index) {
		return columns.get(index);
	}
	
	public TableColumn getRecord(String id) {
		return columnsById.get(id);
	}
	
	public Collection<TableColumn> getRecords() {
		return columns;
	}
	
	public String format() {
		StringBuilder sb = new StringBuilder();
		
		for(TableColumn tc : columns) {
			sb.append(String.format("[%d] %s\t%s\t%s\n", tc.getColumnIndex(), tc.getDataType(), tc.getIdentifier(), tc.getHeader()));
		}
		
		return sb.toString();
	}
	
	public String format(int columnWidth) {
		StringBuilder sb = new StringBuilder();
		
		boolean first=true;
		for(TableColumn c : columns) {
			
			if(!first) {
				sb.append(" | ");
			}
			 
			sb.append(padRight(c.getHeader(),columnWidth));

			first = false;
		}
		
		return sb.toString();
	}
	
	public String formatDataTypes(int columnWidth) {
		StringBuilder sb = new StringBuilder();
		
		boolean first=true;
		for(TableColumn c : columns) {
			
			if(!first) {
				sb.append(" | ");
			}
			 
			sb.append(padRight(c.getDataType().toString(),columnWidth));

			first = false;
		}
		
		return sb.toString();
	}
	
    protected String padRight(String s, int n) {
        if(n==0) {
            return "";
        }
        if (s.length() > n) {
            s = s.substring(0, n);
        }
        s = s.replace("\n", " ");
        return String.format("%1$-" + n + "s", s);
    }
	
	public int getSize() {
		return columns.size();
	}
	
	public int indexOf(TableColumn tc) {
		for(int i = 0; i < getSize(); i++) {
			if(get(i)==tc) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * @return the functionalDependencies
	 */
	public Map<Set<TableColumn>, Set<TableColumn>> getFunctionalDependencies() {
		return functionalDependencies;
	}
	
	/**
	 * @param functionalDependencies the functionalDependencies to set
	 */
	public void setFunctionalDependencies(
			Map<Set<TableColumn>, Set<TableColumn>> functionalDependencies) {
		this.functionalDependencies = functionalDependencies;
	}
	
	/**
	 * @return the candidateKeys
	 */
	public Collection<Set<TableColumn>> getCandidateKeys() {
		return candidateKeys;
	}
	
	/**
	 * @param candidateKeys the candidateKeys to set
	 */
	public void setCandidateKeys(
		Set<Set<TableColumn>> candidateKeys) {
		this.candidateKeys = candidateKeys;
	}
}
