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

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ColumnType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.TypeConverter;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;
import de.uni_mannheim.informatik.dws.winter.utils.StringUtils;
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

		// changing the path will change the identifiers of all rows and columns of this table
		// so we have to update all HashMaps which contain rows or columns
		getSchema().updateIdentifiers();
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
	 * Adds the current data types of all columns to this tables mapping
	 */
	public void addDataTypesToMapping() {
		for(TableColumn c : getColumns()) {
			getMapping().setDataType(c.getColumnIndex(), c.getDataType());
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

	/**
	 * Projects the table and returns a map that translates the column indices to the projected column indices
	 * @param projectedColumns	the columns to project
	 * @return	a map that translates the column indices to the projected column indices
	 */
	public Map<Integer, Integer> projectColumnIndices(Collection<TableColumn> projectedColumns) {
		Map<Integer, Integer> columnIndexProjection = new HashMap<>();

		// project the table schema
		int idx = 0;
		for (int i = 0; i < getColumns().size(); i++) {
			TableColumn c = getSchema().get(i);

			if (projectedColumns.contains(c)) {
				columnIndexProjection.put(i, idx++);
			}
		}
		
		return columnIndexProjection;
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
		for (Set<TableColumn> det : getSchema().getFunctionalDependencies().keySet()) {

			Set<TableColumn> dep = getSchema().getFunctionalDependencies().get(det);
			Set<TableColumn> depIntersection = Q.intersection(projectedColumns,dep);
			if (det!=null && dep!=null && projectedColumns.containsAll(det) && depIntersection.size()>0) {
				Set<TableColumn> newDet = new HashSet<>();

				for (TableColumn c : det) {
					newDet.add(result.getSchema().get(columnIndexProjection.get(c.getColumnIndex())));
				}

				Set<TableColumn> newDep = new HashSet<>();
				for (TableColumn c : depIntersection) {
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

			if(oldValues!=null) {
				for (int i = 0; i < oldValues.length; i++) {
					if (columnIndexProjection.containsKey(i)) {
						newValues[columnIndexProjection.get(i)] = oldValues[i];
					}
				}
			}

			TableRow nr = new TableRow(r.getRowNumber(), result);
			nr.set(newValues);
			result.addRow(nr);
		}

		return result;
	}

	public Table join(Table otherTable, Collection<Pair<TableColumn,TableColumn>> joinOn, Collection<TableColumn> projection) throws Exception {
		
		// hash the join keys
		Map<TableColumn, Map<Object, Collection<TableRow>>> index = new HashMap<>();
		for(TableRow r : otherTable.getRows()) {
			for(Pair<TableColumn, TableColumn> p : joinOn) {
				TableColumn joinKey = p.getSecond();
				Object value = r.get(joinKey.getColumnIndex());
				if(value!=null) {
					Map<Object, Collection<TableRow>> columnValues = MapUtils.getFast(index, joinKey, (c)->new HashMap<Object,Collection<TableRow>>());
					Collection<TableRow> rowsWithValue = MapUtils.getFast(columnValues, value, (o)->new LinkedList<TableRow>());
					rowsWithValue.add(r);
				}
			}
		}
		
		// create the result table
		Table result = project(Q.intersection(getColumns(), projection));
		result.getSchema().setFunctionalDependencies(new HashMap<>());
		result.getSchema().setCandidateKeys(new HashSet<>());
		result.clear();
		Map<TableColumn, TableColumn> inputColumnToOutputColumn = new HashMap<>();
		for(Map.Entry<Integer, Integer> translation : projectColumnIndices(Q.intersection(getColumns(), projection)).entrySet()) {
			inputColumnToOutputColumn.put(getSchema().get(translation.getKey()), result.getSchema().get(translation.getValue()));
		}
		Collection<TableColumn> otherColumns = Q.without(projection, getColumns());
		for(TableColumn c : otherColumns) {
			TableColumn out = new TableColumn(result.getColumns().size(), result);
			out.setDataType(c.getDataType());
			out.setHeader(c.getHeader());
			result.addColumn(out);
			inputColumnToOutputColumn.put(c, out);
		}
		
		// set the table mapping - class
		Pair<String, Double> thisClass = getMapping().getMappedClass();
		Pair<String, Double> otherClass = otherTable.getMapping().getMappedClass();
		if(Q.equals(thisClass, otherClass, false) || (thisClass==null ^ otherClass==null)) {
			if(thisClass==null) {
				thisClass = otherClass;
			}
			result.getMapping().setMappedClass(thisClass);
		}

		// set the table mapping - properties
		for(TableColumn projectedColumn : projection) {
			Pair<String, Double> colMapping = null;
			
			if(getColumns().contains(projectedColumn)) {
				colMapping = getMapping().getMappedProperty(projectedColumn.getColumnIndex());
			} else {
				colMapping = otherTable.getMapping().getMappedProperty(projectedColumn.getColumnIndex());
			}
			if(colMapping!=null) {
				result.getMapping().setMappedProperty(inputColumnToOutputColumn.get(projectedColumn).getColumnIndex(), colMapping);
			}			
		}
		
		// create the join
		for(TableRow r : getRows()) {
			
			// find all rows statisfying the join condition
			Collection<TableRow> matchingRows = null;
			for(Pair<TableColumn, TableColumn> p : joinOn) {
				Object leftValue = r.get(p.getFirst().getColumnIndex());
				
				Collection<TableRow> otherRows = index.get(p.getSecond()).get(leftValue);
				
				if(otherRows==null) {
					matchingRows = null;
					break;
				}
				
				if(matchingRows==null) {
					matchingRows = otherRows;
				} else {
					matchingRows = Q.intersection(matchingRows, otherRows);
				}
			}
			
			// iterate over the matching rows
			if(matchingRows!=null && matchingRows.size()>0) {
				for(TableRow r2 : matchingRows) {
					
					// create a result row
					TableRow out = new TableRow(result.getRows().size(), result);
					Object[] values = new Object[inputColumnToOutputColumn.size()];
					out.set(values);
					result.addRow(out);
					
					// copy all values from the left table
					for(TableColumn c : getColumns()) {
						TableColumn c2 = inputColumnToOutputColumn.get(c);
						if(c2!=null) {
							values[c2.getColumnIndex()] = r.get(c.getColumnIndex());
						}
					}
					
					// copy all values from the right table
					for(TableColumn c : otherTable.getColumns()) {
						TableColumn c2 = inputColumnToOutputColumn.get(c);
						if(c2!=null) {
							values[c2.getColumnIndex()] = r2.get(c.getColumnIndex());
						}
					}
					
					// set the table mapping - instances
					Pair<String, Double> thisRowMapping = getMapping().getMappedInstance(r.getRowNumber());
					Pair<String, Double> otherRowMapping = otherTable.getMapping().getMappedInstance(r2.getRowNumber());
					if(Q.equals(thisRowMapping, otherRowMapping, false) || (thisRowMapping==null ^ otherRowMapping==null)) {
						if(thisRowMapping==null) {
							thisRowMapping = otherClass;
						}
						result.getMapping().setMappedInstance(out.getRowNumber(), thisRowMapping);
					}
					
				}
				
			}
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
		for (Set<TableColumn> det : getSchema().getFunctionalDependencies().keySet()) {
			Set<TableColumn> dep = getSchema().getFunctionalDependencies().get(det);
			Set<TableColumn> newDet = new HashSet<>(det.size());

			for (TableColumn c : det) {
				newDet.add(result.getSchema().get(c.getColumnIndex()));
			}

			Set<TableColumn> newDep = new HashSet<>(dep.size());
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
		KeepFirst, KeepBoth, ReplaceNULLs, CreateList, CreateSet, ReturnConflicts
	}

	/**
	 * 
	 * Removes duplicates from the table. The provided key is used to find the
	 * duplicates. If duplicates are found, the first record matching the key is
	 * kept, all others are removed.
	 * 
	 * @param key
	 */
	public Collection<Pair<TableRow, TableRow>> deduplicate(Collection<TableColumn> key) {
		return deduplicate(key, ConflictHandling.KeepFirst);
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
	public Collection<Pair<TableRow, TableRow>> deduplicate(Collection<TableColumn> key, ConflictHandling conflictHandling) {
		return deduplicate(key, conflictHandling, true);
	}

	/**
	 * 
	 * Removes duplicates from the table. The provided key is used to find the
	 * duplicates. If duplicates are found, the remaining values are checked for
	 * conflicts and the ConflictHandling is applied:
	 * ConflictHandling.KeepFirst: The first record is kept, all others are
	 * removed ConflictHandling.KeepBoth: All conflicting records are kept
	 * ConflictHandling.ReplaceNULLs: Like KeepBoth, but if conflicts are only between a value and a NULL, the NULLs are replaced such that only one record needs to be kept
	 * ConflictHandling.ReturnConflicts: Like KeepBoth, but returns the conflicts instead of the duplicates
	 * 
	 * @param key
	 * @param conflictHandling
	 * @param reorganiseRowNumbers specifies if reorganiseRowNumbers() should be called after deduplication
	 */
	public Collection<Pair<TableRow, TableRow>> deduplicate(Collection<TableColumn> key, ConflictHandling conflictHandling, boolean reorganiseRowNumbers) {
		/***********************************************
		 * De-Duplication
		 ***********************************************/
		Collection<Pair<TableRow, TableRow>> duplicates = new LinkedList<>();
		
		// use the provided key to perform duplicate detection
		// keep a map of (key values)->(first row with these values) for the
		// chosen key
		HashMap<List<Object>, TableRow> seenKeyValues = new HashMap<>();

		// use a linked list during de-duplication, which has O(1) cost for removing entries
		// LinkedList<TableRow> linkedRows = new LinkedList<>(getRows());
		ArrayList<TableRow> deduplicatedRows = new ArrayList<>(getRows().size());

		// iterate the table row by row
		Iterator<TableRow> rowIt = getRows().iterator();
		// Iterator<TableRow> rowIt = linkedRows.iterator();
		while (rowIt.hasNext()) {
			TableRow r = rowIt.next();

			// get the values of the key for the current row
			ArrayList<Object> keyValues = new ArrayList<>(key.size());
			for (TableColumn c : key) {
				keyValues.add(r.get(c.getColumnIndex()));
			}

			boolean keepRow = true;

			// check if the key values have been seen before
			if (seenKeyValues.containsKey(keyValues)) {

				TableRow existing = seenKeyValues.get(keyValues);

				if(conflictHandling != ConflictHandling.ReturnConflicts) {
					duplicates.add(new Pair<>(existing, r));
				}
				
				if (conflictHandling != ConflictHandling.KeepFirst) {

					// check the remaining attributes for equality
					boolean equal = true;
					boolean conflictingNullsOnly = true;
					List<Integer> nullIndices = new LinkedList<>();
					for (TableColumn c : Q.without(getColumns(), key)) {
						Object existingValue = existing.get(c.getColumnIndex());
						Object duplicateValue = r.get(c.getColumnIndex());

						// if (existingValue != null && existingValue.equals(duplicateValue)) {
						if(Q.equals(existingValue, duplicateValue, true)) {				// both values equal or both NULL
							// equal values
						} else if (existingValue == null && duplicateValue != null
								|| existingValue != null && duplicateValue == null) {	// one value NULL
							// conflict with a NULL value
							equal = false;
							nullIndices.add(c.getColumnIndex());
						} else {														// different values
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
							// continue;
							keepRow = true;
						} else if(conflictHandling == ConflictHandling.ReturnConflicts) {
							duplicates.add(new Pair<>(existing, r));
							keepRow = true;
					 	} else if(conflictHandling == ConflictHandling.CreateList || conflictHandling == ConflictHandling.CreateSet) {
							// if handling is set to create list or create set, we merge all values and  assign them to the first record
							
							for (TableColumn c : Q.without(getColumns(), key)) {
								
								Object existingValue = existing.get(c.getColumnIndex());
								Object conflictingValue = r.get(c.getColumnIndex());
								Collection<Object> values = null;
								if(conflictHandling==ConflictHandling.CreateSet) {
									values = new HashSet<>();
								} else {
									values = new LinkedList<>();
								}

								if(existingValue!=null) {
									if(existingValue.getClass().isArray()) {
										values.addAll(Q.toList((Object[])existingValue));
									} else {
										values.add(existingValue);
									}
								}

								if(conflictingValue!=null) {
									if(conflictingValue.getClass().isArray()) {
										values.addAll(Q.toList((Object[])conflictingValue));
									} else {
										values.add(conflictingValue);
									}
								}
								
								if(values.size()<=1) {
									// if the result has only one element, don't treat it as multi-valued
									existing.set(c.getColumnIndex(), Q.firstOrDefault(values));
								} else {
									existing.set(c.getColumnIndex(), values.toArray());
								}
							}

							keepRow = false;
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

							keepRow = false;
						}
					}
				} else {
					keepRow = false;
				}

				if(!keepRow) {
					// remove the duplicate row
					// rowIt.remove();
					
					// and add the table name of the duplicate row to the existing
					// row
					existing.addProvenanceForRow(r);
				}
			} else {
				// if not, add the current key values to the list of seen values
				seenKeyValues.put(keyValues, r);

				// add the row itself as provenance information (so we have all
				// source information if later rows are merged with this one)
				// r.addProvenanceForRow(r);
			}

			if(keepRow) {
				// add the row to the output
				deduplicatedRows.add(r);
			}
		}

		// re-create the array list
		// setRows(new ArrayList<>(linkedRows));
		setRows(deduplicatedRows);
		rows.trimToSize();

		if(reorganiseRowNumbers) {
			reorganiseRowNumbers();
		}
		
		return duplicates;
	}
	
	public Collection<TableRow> findUniquenessViolations(Collection<TableColumn> uniqueColumnCombination) {
		// use the provided key to perform duplicate detection
		// keep a map of (key values)->(first row with these values) for the
		// chosen key
		HashMap<List<Object>, TableRow> seenKeyValues = new HashMap<>();
		Set<TableRow> conflicts = new HashSet<>();
		
		// iterate the table row by row
		Iterator<TableRow> rowIt = getRows().iterator();
		while (rowIt.hasNext()) {
			TableRow r = rowIt.next();

			// get the values of the key for the current row
			ArrayList<Object> keyValues = new ArrayList<>(uniqueColumnCombination.size());
			for (TableColumn c : uniqueColumnCombination) {
				keyValues.add(r.get(c.getColumnIndex()));
			}

			// check if the key values have been seen before
			if (seenKeyValues.containsKey(keyValues)) {

				TableRow existing = seenKeyValues.get(keyValues);

				conflicts.add(existing);
				conflicts.add(r);
			} else {
				seenKeyValues.put(keyValues, r);
			}
		}

		return conflicts;
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
	
	public Map<TableColumn, Integer> getNumberOfValuesPerColumn() {
		Map<TableColumn, Integer> valuesByColumn = new HashMap<>();
		
		for(TableRow r : getRows()) {
			
			for(TableColumn c : getColumns()) {
				
				if(r.get(c.getColumnIndex())!=null) {
					MapUtils.increment(valuesByColumn, c);
				}
				
			}
			
		}

		return valuesByColumn;
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
	
	public Map<TableColumn, Set<Object>> getColumnDomains() {

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
		
		return valuesByColumn;
	}

	public Set<String> getProvenance() {
		
		Set<String> tbls = new HashSet<>();
		
		for(TableColumn c : getColumns()) {
			for(String prov : c.getProvenance()) {
				
				tbls.add(prov.split("~")[0]);
				
			}
		}
		
		return tbls;
	}

	public String formatFunctionalDependencies() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Table #%d %s: {%s}\n", getTableId(), getPath(), StringUtils.join(Q.project(getColumns(), (c)->c.getHeader()), ",")));
		sb.append("*** Functional Dependencies\n");
		for(Collection<TableColumn> det : getSchema().getFunctionalDependencies().keySet()) {
			Collection<TableColumn> dep = getSchema().getFunctionalDependencies().get(det);
			sb.append(String.format("\t{%s} -> {%s}\n", 
					StringUtils.join(Q.project(det, new TableColumn.ColumnHeaderProjection()), ","),
					StringUtils.join(Q.project(dep, new TableColumn.ColumnHeaderProjection()), ",")
					));
		}
		sb.append("*** Candidate Keys\n");
		for(Collection<TableColumn> key : getSchema().getCandidateKeys()) {
			sb.append(String.format("\t{%s}\n", 
					StringUtils.join(Q.project(key, new TableColumn.ColumnHeaderProjection()), ",")
					));
		}
		return sb.toString();
	}
}
