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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.utils.query.Func;

/**
 * Represents a row of a Web Table.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableRow implements Serializable {
	
	public TableRow() {
		super();
	}

	public static class TableRowIdentifierProjection implements Func<String, TableRow> {

		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public String invoke(TableRow in) {
			return in.getIdentifier();
		}
		
	}

	private static final long serialVersionUID = 1L;
	private Object[] values;
	private int rowNumber;
	private Table table;
	private List<String> provenance;
	
	public TableRow(int rowNumber, Table table) {
		this.table = table;
		this.rowNumber = rowNumber;
		provenance = new LinkedList<>();
//		addProvenanceForRow(this);
	}
	
	public void addProvenanceForRow(TableRow row) {
		if(row.getProvenance().size()>0) {
			getProvenance().addAll(row.getProvenance());
		} else {
			getProvenance().add(row.getIdentifier());
		}
	}
	
	public String getIdentifier() {
		//return String.format("%s~Row%s", table.getPath(), rowNumber);
		return formatRowIdentifier(table.getPath(), rowNumber);
	}
	
	public static String formatRowIdentifier(String tableName, int rowNumber) {
		return String.format("%s~Row%s", tableName, rowNumber);
	}
	
	public void set(int columnIndex, Object value) {
		values[columnIndex] = value;
	}
	public Object get(int columnIndex) {
		if(columnIndex>=values.length) {
			return null;
		} else {
			return values[columnIndex];
		}
	}
	public void set(Object[] values) {
		this.values = values;
	}
	public Object[] getValueArray() {
		return values;
	}

	public int getRowNumber() {
		if(table==null) {
			return -1;
		} else {
			if(rowNumber==-1) {
				rowNumber = table.getRows().indexOf(this);
			}
			return rowNumber;
		}
	}
	
	/** 
	 * enforces that the row number is re-calculated the next time {@link TableRow#getRowNumber() } is called.
	 */
	public void invalidateRowNumber() {
		rowNumber=-1;
	}
	
	protected void setRowNumber(int number) {
		rowNumber = number;
	}
	
	public Table getTable() {
		return table;
	}

	public Object getKeyValue() {
		if(table.hasSubjectColumn()) {
			return get(table.getSubjectColumnIndex());
		} else {
			return null;
		}
	}
	
	/**
	 * @return the provenance
	 */
	public List<String> getProvenance() {
		return provenance;
	}
	
	/**
	 * @param provenance the provenance to set
	 */
	public void setProvenance(List<String> provenance) {
		this.provenance = provenance;
	}
	
	public String format(int columnWidth) {
		StringBuilder sb = new StringBuilder();
		
		// sort columns for output
		ArrayList<TableColumn> columns = new ArrayList<>(table.getColumns());
		Collections.sort(columns, new TableColumn.TableColumnByIndexComparator());
		
		boolean first=true;
		for(TableColumn c : columns) {
			
			if(!first) {
				sb.append(" | ");
			}
			
			Object v = get(c.getColumnIndex());
			String value = v==null ? "null" : v.toString(); 
			sb.append(padRight(value,columnWidth));

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
    
    public TableRow copy(Table t) {
    	TableRow r = new TableRow(t.getRows().size(), t);
    	r.set(getValueArray());
    	r.setProvenance(getProvenance());
    	r.addProvenanceForRow(this);
    	return r;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof TableRow) {
    		TableRow r = (TableRow)obj;
    		return getIdentifier().equals(r.getIdentifier()) ;
    	}
    	return super.equals(obj);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
    	return getIdentifier().hashCode();
    }
    
    public Object[] project(Collection<TableColumn> projectedColumns) {
		Map<Integer, Integer> columnIndexProjection = new HashMap<>();
		
		// project the table schema
		int idx = 0;
		for(int i = 0; i < table.getColumns().size(); i++) {
			TableColumn c = table.getSchema().get(i);
			
			if(projectedColumns.contains(c)) {
				columnIndexProjection.put(i, idx++);
			}
		}
		
		Object[] oldValues = getValueArray();
		Object[] newValues = new Object[projectedColumns.size()];
		
		for(int i = 0; i < oldValues.length; i++) {
			if(columnIndexProjection.containsKey(i)) {
				newValues[columnIndexProjection.get(i)] = oldValues[i];
			}
		}
		
		return newValues;
    }
}
