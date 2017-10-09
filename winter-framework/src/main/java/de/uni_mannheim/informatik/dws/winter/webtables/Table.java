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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ColumnType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.TypeConverter;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;
import de.uni_mannheim.informatik.dws.winter.utils.parallel.Consumer;
import de.uni_mannheim.informatik.dws.winter.utils.parallel.Parallel;
import de.uni_mannheim.informatik.dws.winter.utils.query.Func;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableKeyIdentification;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeDetector;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeGuesser;

/**
 * Represents a Web Table.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Table implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class TablePathComparator implements Comparator<Table> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Table o1, Table o2) {
			return o1.getPath().compareTo(o2.getPath());
		}

	}

	public static class TableIdComparator implements Comparator<Table> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Table o1, Table o2) {
			return Integer.compare(o1.getTableId(), o2.getTableId());
		}

	}

	public static class TablePathProjection implements Func<String, Table> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.
		 * Object)
		 */
		@Override
		public String invoke(Table in) {
			return in.getPath();
		}

	}

	private ArrayList<TableRow> rows = new ArrayList<>();
	private String path;
	TableSchema schema = new TableSchema();
	private int subjectColumnIndex = -1;
	private int tableId = 0;
	private TableMapping mapping;
	private TableContext context;

	public Table() {
	}

	public TableSchema getSchema() {
		return schema;
	}

	public int getSubjectColumnIndex() {
		return subjectColumnIndex;
	}

	public void setSubjectColumnIndex(int keyIndex) {
		this.subjectColumnIndex = keyIndex;
	}

	public boolean hasSubjectColumn() {
		return subjectColumnIndex >= 0;
	}

	public TableColumn getSubjectColumn() {
		if (hasSubjectColumn()) {
			return schema.get(getSubjectColumnIndex());
		} else {
			return null;
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Collection<TableColumn> getColumns() {
		return schema.getRecords();
	}

	public int getSize() {
		return rows.size();
	}

	public void addColumn(TableColumn c) {
		schema.addColumn(c);
	}

	public void addRow(TableRow r) {
		rows.add(r);
	}

	/**
	 * Removes all rows from the table
	 */
	public void clear() {
		rows.clear();
		rows.trimToSize();
	}

	/**
	 * Should be called after the loading is done, so unneeded memory overhead
	 * can be freed
	 */
	public void endLoad() {
		rows.trimToSize();
	}

	public List<TableRow> getRows() {
		return rows;
	}

	public void setRows(ArrayList<TableRow> rows) {
		this.rows = rows;
	}
	
	public TableRow get(int rowIndex) {
		if (rows.size() > rowIndex) {
			return rows.get(rowIndex);
		} else {
			return null;
		}
	}

	/**
	 * @return the tableId
	 */
	public int getTableId() {
		return tableId;
	}

	/**
	 * @param tableId
	 *            the tableId to set
	 */
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}

	/**
	 * @return the mapping
	 */
	public TableMapping getMapping() {
		if (mapping == null) {
			mapping = new TableMapping();
		}
		return mapping;
	}

	/**
	 * @param mapping
	 *            the mapping to set
	 */
	public void setMapping(TableMapping mapping) {
		this.mapping = mapping;
	}

	/**
	 * @return the context
	 */
	public TableContext getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(TableContext context) {
		this.context = context;
	}

	@Override
	public String toString() {
		return path;
	}

	/**
	 * Adds a new column to the schema and updates all rows with the new
	 * indices.
	 * 
	 * @param index
	 * @param c
	 */
	public void insertColumn(int index, TableColumn c) {

		if (subjectColumnIndex != -1 && index <= subjectColumnIndex) {
			subjectColumnIndex++;
		}

		getSchema().insertColumn(index, c);

		for (TableRow r : getRows()) {
			Object[] oldValues = r.getValueArray();
			Object[] newValues = new Object[oldValues.length + 1];

			for (int i = 0; i < oldValues.length; i++) {
				if (i < index) {
					newValues[i] = oldValues[i];
				} else if (i >= index) {
					newValues[i + 1] = oldValues[i];
				}
			}

			r.set(newValues);
		}
	}

	/**
	 * Removes a column from the schema and updates all rows with the new
	 * indices
	 * 
	 * @param c
	 */
	public void removeColumn(TableColumn c) {
		if (subjectColumnIndex != -1 && c.getColumnIndex() <= subjectColumnIndex) {
			subjectColumnIndex--;
		} else if (c.getColumnIndex() == subjectColumnIndex) {
			subjectColumnIndex = -1;
		}

		getSchema().removeColumn(c);

		for (TableRow r : getRows()) {
			Object[] oldValues = r.getValueArray();
			Object[] newValues = new Object[oldValues.length - 1];

			for (int i = 0; i < newValues.length; i++) {
				if (i < c.getColumnIndex()) {
					newValues[i] = oldValues[i];
				} else if (i >= c.getColumnIndex()) {
					newValues[i] = oldValues[i + 1];
				}
			}

			r.set(newValues);
		}
	}

	/**
	 * 
	 * Removes a row from the table by its row number. Call reorganiseRowNumbers() afterwards to make sure that row numbers reflect the row's position in the table.
	 * 
	 * @param rowNumber
	 * 				The row number of the row that should be removed
	 * @return
	 * 				Returns the row that was removed
	 */
	public TableRow removeRow(int rowNumber) {
		return getRows().remove(rowNumber);
	}
	
	/**
	 * Detects data types and units for all columns with data type 'unknown' and
	 * converts their values to the corresponding types
	 */
	public void inferSchemaAndConvertValues() {
		// type & unit detection

		inferSchema();
		convertValues();
	}

	/**
	 * Detects data types and units for all columns with data type 'unknown' and
	 * converts their values to the corresponding types
	 */
	public void inferSchemaAndConvertValues(TypeDetector td) {
		// type & unit detection
		inferSchema(td);
		convertValues();
	}

	/**
	 * Detects data types and units for all columns with data type 'unknown'
	 */
	public void inferSchema() {
		final TypeGuesser tg = new TypeGuesser();

		inferSchema(tg);
	}

	/**
	 * Detects data types and units for all columns with data type 'unknown'
	 */
	public void inferSchema(final TypeDetector td) { 
		try {
			Parallel.forLoop(0, getSchema().getSize(), new Consumer<Integer>() {

				@Override
				public void execute(Integer i) {
					String attributeLabel = getSchema().get(i).getHeader();
					String[] column = new String[getSize() + 1];

					if (getSchema().get(i).getDataType() == DataType.unknown) {

						// detect types and units per value
						int rowCounter = 0;
						column[rowCounter] = getSchema().get(i).getHeader();
						rowCounter++;

						boolean nullValues = true;
						for (TableRow r : getRows()) {

							if (nullValues == true && r.get(i) != null)
								nullValues = false;
							if(ListHandler.isArray(r.get(i))) {
								column[rowCounter] = (String) ((Object[])r.get(i))[0];
							} else {
								column[rowCounter] = (String) r.get(i);
							}
							rowCounter++;
						}

						ColumnType columnType = null;
						if (!nullValues) {
							columnType = td.detectTypeForColumn(column, attributeLabel);

							if (columnType == null || columnType.getType() == null)
								columnType = new ColumnType(DataType.string, null);
	
						} else {
							columnType = new ColumnType(DataType.string, null);
						}
						
						if (columnType.getType() == DataType.unit) {
							getSchema().get(i).setDataType(DataType.numeric);
							getSchema().get(i).setUnit(columnType.getUnit());
						} else {
							getSchema().get(i).setDataType(columnType.getType());
						}
						
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts all values to the type corresponding to the respective column's
	 * data type (no unit detection/conversion)
	 */
	public void convertValues() {
		TypeConverter tc = new TypeConverter();

		for (TableRow r : getRows()) {

			for (int i = 0; i < getSchema().getSize(); i++) {

				Object typedValue = null;
				
				if(ListHandler.isArray(r.get(i))) {
					
					Object[] values = (Object[])r.get(i);
					Object[] typedValues = new Object[values.length];
					
					for(int j = 0; j < values.length; j++) {
						typedValues[j] =tc.typeValue((String) values[j], getSchema().get(i).getDataType(), getSchema().get(i).getUnit()); 
					}
					
					typedValue = typedValues;
					
				} else {
					typedValue = tc.typeValue((String) r.get(i), getSchema().get(i).getDataType(), getSchema().get(i).getUnit());
				}
				
				r.set(i, typedValue);

			}

		}
	}

	/**
	 * Detects and sets the key column for this table
	 */
	public void identifySubjectColumn() {
		identifySubjectColumn(0.3);
	}

	/**
	 * Detects and sets the key column for this table
	 */
	public void identifySubjectColumn(double uniquenessThreshold) {
		identifySubjectColumn(uniquenessThreshold, false);
	}

	public void identifySubjectColumn(double uniquenessThreshold, boolean verbose) {
		if (hasSubjectColumn()) {
			// reset existing subject column
			setSubjectColumnIndex(-1);
		}

		TableKeyIdentification tki = new TableKeyIdentification();

		tki.setKeyUniquenessThreshold(uniquenessThreshold);
		tki.setVerbose(verbose);

		tki.identifyKeys(this);
	}

	/**
	 * Appends the rows of the given table to this table's rows. Both tables
	 * must have the same schema!
	 * 
	 * @param t
	 */
	public void append(Table t) {
		for (TableRow r : t.getRows()) {
			TableRow r2 = new TableRow(getRows().size(), this);
			r2.set(r.getValueArray());
			r2.addProvenanceForRow(r);
			addRow(r2);
		}
	}

	/**
	 * removes all provenance information from the table
	 */
	public void clearProvenance() {
		for (TableRow r : getRows()) {
			r.getProvenance().clear();
		}
		for (TableColumn c : getColumns()) {
			c.getProvenance().clear();
		}
	}

	/**
	 * re-assigns the row numbers to rows. After rows are removed from the
	 * table, the row numbers no longer reflect the position of the row in the
	 * table, which can be corrected by calling this method.
	 */
	public void reorganiseRowNumbers() {
		int number = 0;
		for (TableRow r : getRows()) {
			r.setRowNumber(number++);
		}
	}

	public Table project(Collection<TableColumn> projectedColumns) throws Exception {
		return project(projectedColumns, true);
	}
	
	public Table project(Collection<TableColumn> projectedColumns, boolean addProvenance) throws Exception {
		Table result = new Table();

		Map<Integer, Integer> columnIndexProjection = new HashMap<>();

		// project the table schema
		int idx = 0;
		for (int i = 0; i < getColumns().size(); i++) {
			TableColumn c = getSchema().get(i);

			if (projectedColumns.contains(c)) {
				columnIndexProjection.put(i, idx);
				result.addColumn(c.copy(result, idx++, addProvenance));
			}
		}

		// copy path and key index
		if (columnIndexProjection.containsKey(getSubjectColumnIndex())) {
			result.setSubjectColumnIndex(columnIndexProjection.get(getSubjectColumnIndex()));
		}
		result.setPath(getPath());

		// copy functional dependencies
		for (Collection<TableColumn> det : getSchema().getFunctionalDependencies().keySet()) {

			Collection<TableColumn> dep = getSchema().getFunctionalDependencies().get(det);
			if (projectedColumns.containsAll(det) && projectedColumns.containsAll(dep)) {
				Collection<TableColumn> newDet = new ArrayList<>(det.size());

				for (TableColumn c : det) {
					newDet.add(result.getSchema().get(columnIndexProjection.get(c.getColumnIndex())));
				}

				Collection<TableColumn> newDep = new ArrayList<>(dep.size());
				for (TableColumn c : dep) {
					newDep.add(result.getSchema().get(columnIndexProjection.get(c.getColumnIndex())));
				}

				result.getSchema().getFunctionalDependencies().put(newDet, newDep);
			}
		}

		// copy candidate keys
		for (Set<TableColumn> key : getSchema().getCandidateKeys()) {
			if (projectedColumns.containsAll(key)) {
				Set<TableColumn> newKey = new HashSet<>();
				for (TableColumn c : key) {
					newKey.add(result.getSchema().get(columnIndexProjection.get(c.getColumnIndex())));
				}
				result.getSchema().getCandidateKeys().add(newKey);
			}
		}

		// fill the new table with data
		for (TableRow r : getRows()) {
			Object[] oldValues = r.getValueArray();
			Object[] newValues = new Object[projectedColumns.size()];

			for (int i = 0; i < oldValues.length; i++) {
				if (columnIndexProjection.containsKey(i)) {
					newValues[columnIndexProjection.get(i)] = oldValues[i];
				}
			}

			TableRow nr = new TableRow(r.getRowNumber(), result);
			nr.set(newValues);
			result.addRow(nr);
		}

		return result;
	}

	public Table copySchema() {
		Table result = new Table();

		// copy the table schema
		for (int i = 0; i < getColumns().size(); i++) {
			TableColumn c = getSchema().get(i);

			result.addColumn(c.copy(result, i));
		}

		// copy path and key index
		result.setSubjectColumnIndex(getSubjectColumnIndex());
		result.setPath(getPath());

		// copy functional dependencies
		for (Collection<TableColumn> det : getSchema().getFunctionalDependencies().keySet()) {
			Collection<TableColumn> dep = getSchema().getFunctionalDependencies().get(det);
			Collection<TableColumn> newDet = new ArrayList<>(det.size());

			for (TableColumn c : det) {
				newDet.add(result.getSchema().get(c.getColumnIndex()));
			}

			Collection<TableColumn> newDep = new ArrayList<>(dep.size());
			for (TableColumn c : dep) {
				newDep.add(result.getSchema().get(c.getColumnIndex()));
			}

			result.getSchema().getFunctionalDependencies().put(newDet, newDep);
		}

		// copy candidate keys
		for (Set<TableColumn> key : getSchema().getCandidateKeys()) {
			result.getSchema().getCandidateKeys().add(key);
		}

		return result;
	}

	public static enum ConflictHandling {
		KeepFirst, KeepBoth, ReplaceNULLs
	}

	/**
	 * 
	 * Removes duplicates from the table. The provided key is used to find the
	 * duplicates. If duplicates are found, the first record matching the key is
	 * kept, all others are removed.
	 * 
	 * @param key
	 */
	public void deduplicate(Collection<TableColumn> key) {
		deduplicate(key, ConflictHandling.KeepFirst);
	}

	/**
	 * 
	 * Removes duplicates from the table. The provided key is used to find the
	 * duplicates. If duplicates are found, the remaining values are checked for
	 * conflicts and the ConflictHandling is applied:
	 * ConflictHandling.KeepFirst: The first record is kept, all others are
	 * removed ConflictHandling.KeepBoth: All conflicting records are kept
	 * ConflictHandling.ReplaceNULLs: Like KeepBoth, but if conflicts are only
	 * between a value and a NULL, the NULLs are replaced such that only one
	 * record needs to be kept
	 * 
	 * @param key
	 * @param conflictHandling
	 */
	public void deduplicate(Collection<TableColumn> key, ConflictHandling conflictHandling) {
		/***********************************************
		 * De-Duplication
		 ***********************************************/

		// use the provided key to perform duplicate detection
		// keep a map of (key values)->(first row with these values) for the
		// chosen key
		HashMap<List<Object>, TableRow> seenKeyValues = new HashMap<>();

		// iterate the table row by row
		Iterator<TableRow> rowIt = getRows().iterator();
		while (rowIt.hasNext()) {
			TableRow r = rowIt.next();

			// get the values of the key for the current row
			ArrayList<Object> keyValues = new ArrayList<>(key.size());
			for (TableColumn c : key) {
				keyValues.add(r.get(c.getColumnIndex()));
			}

			// check if the key values have been seen before
			if (seenKeyValues.containsKey(keyValues)) {

				TableRow existing = seenKeyValues.get(keyValues);

				if (conflictHandling != ConflictHandling.KeepFirst) {

					// check the remaining attributes for equality
					boolean equal = true;
					boolean conflictingNullsOnly = true;
					List<Integer> nullIndices = new LinkedList<>();
					for (TableColumn c : Q.without(getColumns(), key)) {
						Object existingValue = existing.get(c.getColumnIndex());
						Object duplicateValue = r.get(c.getColumnIndex());

						if (existingValue != null && existingValue.equals(duplicateValue)) {
							// equal values
						}
						if (existingValue == null && duplicateValue != null
								|| existingValue != null && duplicateValue == null) {
							// conflict with a NULL value
							equal = false;
							nullIndices.add(c.getColumnIndex());
						} else {
							equal = false;
							conflictingNullsOnly = false;
						}
					}

					if (!equal) {
						// the records are not equal
						if (conflictHandling == ConflictHandling.KeepBoth
								|| conflictHandling == ConflictHandling.ReplaceNULLs && !conflictingNullsOnly) {
							// if handling is set to keep both we don't merge
							// if handling is set to replace nulls, but there is
							// a conflict between non-null values, we don't
							// merge
							continue;
						} else {
							// if handling is set to replace nulls, and there
							// are only conflicts between values and nulls, we
							// set the values in the existing record and remove
							// the second record
							for (Integer idx : nullIndices) {
								if (existing.get(idx) == null) {
									existing.set(idx, r.get(idx));
								}
							}
						}
					}
				}

				// remove the duplicate row
				rowIt.remove();
				// and add the table name of the duplicate row to the existing
				// row
				existing.addProvenanceForRow(r);
			} else {
				// if not, add the current key values to the list of seen values
				seenKeyValues.put(keyValues, r);

				// add the row itself as provenance information (so we have all
				// source information if later rows are merged with this one)
				// r.addProvenanceForRow(r);
			}
		}

		reorganiseRowNumbers();
	}
	
	public Map<TableColumn, Double> getColumnDensities() {

		Map<TableColumn, Double> densities = new HashMap<>();
		Map<TableColumn, Integer> valuesByColumn = new HashMap<>();
		
		for(TableRow r : getRows()) {
			
			for(TableColumn c : getColumns()) {
				
				if(r.get(c.getColumnIndex())!=null) {
					MapUtils.increment(valuesByColumn, c);
				}
				
			}
			
		}
		
		for(TableColumn c : getColumns()) {
			
			Integer values = valuesByColumn.get(c);
			if(values==null) {
				values = 0;
			}
			double density = values / (double)getRows().size();
			
			densities.put(c, density);
		}
		
		return densities;
	}
	
	public Map<TableColumn, Double> getColumnUniqueness() {

		Map<TableColumn, Double> uniqueness = new HashMap<>();
		Map<TableColumn, Set<Object>> valuesByColumn = new HashMap<>();
		
		for(TableRow r : getRows()) {
			
			for(TableColumn c : getColumns()) {
				
				if(r.get(c.getColumnIndex())!=null) {
					Set<Object> domain = valuesByColumn.get(c);
					if(domain==null) {
						domain = new HashSet<>();
						valuesByColumn.put(c, domain);
					}
					domain.add(r.get(c.getColumnIndex()));
				}
				
			}
			
		}
		
		for(TableColumn c : getColumns()) {
			
			Set<Object> domain = valuesByColumn.get(c);
			if(domain==null) {
				domain = new HashSet<>();
			}
			double uniq = domain.size() / (double)getRows().size();
			
			uniqueness.put(c, uniq);
		}
		
		return uniqueness;
	}
}
