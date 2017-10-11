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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.Unit;
import de.uni_mannheim.informatik.dws.winter.utils.query.Func;

/**
 * Represents a column of a Web Table.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableColumn implements Serializable, Comparable<TableColumn> {

	public static class ColumnIdentifierProjection implements Func<String, TableColumn> {

		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public String invoke(TableColumn in) {
			return in.getIdentifier();
		}
		
	}
	
	public static class DataTypeProjection implements Func<DataType, TableColumn> {

		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public DataType invoke(TableColumn in) {
			return in.getDataType();
		}
		
	}
	
	public static class ColumnHeaderProjection implements Func<String, TableColumn> {

		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public String invoke(TableColumn in) {
			return in.getHeader();
		}
		
	}
	
	public static class ColumnIndexProjection implements Func<Integer, TableColumn> {

		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public Integer invoke(TableColumn in) {
			return in.getColumnIndex();
		}
		
	}
	
	public static class ColumnIndexAndHeaderProjection implements Func<String, TableColumn> {

		private String regexToRemove;
		
		public ColumnIndexAndHeaderProjection() {
		}
		
		public ColumnIndexAndHeaderProjection(String regexToRemove) {
			this.regexToRemove = regexToRemove; 
		}
		
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public String invoke(TableColumn in) {
			if(regexToRemove==null) {
				return String.format("[%d]%s", in.getColumnIndex(), in.getHeader());
			} else {
				return String.format("[%d]%s", in.getColumnIndex(), in.getHeader()).replaceAll(regexToRemove, "");
			}
		}
		
	}
	
	public static class TableColumnByIndexComparator implements Comparator<TableColumn> {

		@Override
		public int compare(TableColumn record1, TableColumn record2) {
			return Integer.compare(record1.getColumnIndex(), record2.getColumnIndex());
		}
	}
	
	private static final long serialVersionUID = 1L;
	private DataType dataType;
	private String header;
	private int columnIndex;
	private Table table;
	private String uri;
	private Unit unit;
	private List<String> provenance;
	private Collection<String> synonyms;
	
	public TableColumn() {
		provenance = new LinkedList<>();
		synonyms = new LinkedList<>();
	}
	
	public TableColumn(int columnIndex, Table table) {
		this.columnIndex = columnIndex;
		this.table = table;
		provenance = new LinkedList<>();
		synonyms = new LinkedList<>();
	}
	
	public void addProvenanceForColumn(TableColumn column) {
		if(column.getProvenance()!=null && column.getProvenance().size() > 0) {
			getProvenance().addAll(column.getProvenance());
			getProvenance().add(column.getIdentifier());
		} else {
			getProvenance().add(column.getIdentifier());
		}
	}
	
	public String getIdentifier() {
		return String.format("%s~Col%s", table.getPath(), columnIndex);
	}
	
	public String getUniqueName() {
		return getIdentifier();
	}
	
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	
	public DataType getDataType() {
		return dataType;
	}
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	
	public Table getTable() {
		return table;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}
	/**
	 * @param unit the unit to set
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
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

	/**
	 * @return the synonyms
	 */
	public Collection<String> getSynonyms() {
		return synonyms;
	}
	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(Collection<String> synonyms) {
		this.synonyms = synonyms;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TableColumn o) {
		return getIdentifier().compareTo(o.getIdentifier());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TableColumn) {
			return getIdentifier().equals(((TableColumn) obj).getIdentifier());
		} else {
			return super.equals(obj);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}
	
	public TableColumn copy(Table t, int columnIndex) {
		return copy(t, columnIndex, true);
	}
	
	public TableColumn copy(Table t, int columnIndex, boolean addProvenance) {
		TableColumn c = new TableColumn(columnIndex, t);
		c.setDataType(getDataType());
		c.setHeader(getHeader());
		c.setUnit(getUnit());
		c.setUri(getUri());
		c.setProvenance(new LinkedList<>(getProvenance()));
		if(addProvenance) {
			c.addProvenanceForColumn(this);
		}
		c.setSynonyms(new HashSet<>(getSynonyms()));
		return c;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{%d}[%d] %s", getTable().getTableId(), getColumnIndex(), getHeader());
	}
	
	public TableColumnStatistics calculateColumnStatistics() {
		return new TableColumnStatistics(this);
	}
}
