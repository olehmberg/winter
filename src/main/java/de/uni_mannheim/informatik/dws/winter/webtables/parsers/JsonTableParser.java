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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import com.google.gson.Gson;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableContext;
import de.uni_mannheim.informatik.dws.winter.webtables.TableMapping;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeDetector;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeGuesser;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.WebTablesRowContentDetector;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema.Dependency;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema.HeaderPosition;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema.TableOrientation;

/**
 * Loads a Web Table in the JSON format.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class JsonTableParser extends TableParser {

	public JsonTableParser() {
		setTypeDetector(new TypeGuesser());
		setStringNormalizer(new DynamicStringNormalizer());
		setRowContentDetector(new WebTablesRowContentDetector());
	}

	public JsonTableParser(TypeDetector pTypeDetector) {
		setTypeDetector(pTypeDetector);
		setStringNormalizer(new DynamicStringNormalizer());
		setRowContentDetector(new WebTablesRowContentDetector());
	}

	private boolean inferSchema = true;

	/**
	 * @return the inferSchema
	 */
	public boolean isInferSchema() {
		return inferSchema;
	}

	/**
	 * @param inferSchema
	 *            the inferSchema to set
	 */
	public void setInferSchema(boolean inferSchema) {
		this.inferSchema = inferSchema;
	}

	@Override
	public Table parseTable(File file) {
		FileReader fr;
		Table t = null;
		try {
			fr = new FileReader(file);

			t = parseTable(fr, file.getName());

			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return t;
	}

	@Override
	public Table parseTable(Reader reader, String fileName) throws IOException {
		Gson gson = new Gson();

		String json = IOUtils.toString(reader);

		// get the data from the JSON source
		JsonTableSchema data = gson.fromJson(json, JsonTableSchema.class);

		TableMapping mapping = null;

		// check if any data was parsed ... if the file used the schema with
		// mappings, data will not have any contents
		// but as no exception is thrown, we have to check attributes of data
		// for null ...
		if (data.getRelation() == null) {

			JsonTableWithMappingSchema moreData = gson.fromJson(json, JsonTableWithMappingSchema.class);

			data = moreData.getTable();
			mapping = moreData.getMapping().toTableMapping();
		}

		return parseTable(data, fileName, mapping);
	}

	public Table parseTable(JsonTableSchema data, String fileName, TableMapping mapping) {
		if (data.getTableType() != JsonTableSchema.TableType.RELATION) {
			return null;
		}

		if (data.getTableOrientation() == JsonTableSchema.TableOrientation.VERTICAL) {
			// flip table
			data.transposeRelation();
			data.setTableOrientation(TableOrientation.HORIZONTAL);

			// String[] headers = new String[data.getRelation().length];
			// for(int col = 0; col < data.getRelation().length; col++) {
			// headers[col] = data.getRelation()[col][0];
			// }
			data.setHasHeader(true);
			data.setHeaderPosition(HeaderPosition.FIRST_ROW);
			data.setHeaderRowIndex(0);
		}

		// create a new table
		Table t = new Table();
		t.setPath(fileName);
		t.setSubjectColumnIndex(data.getKeyColumnIndex());
		t.setTableId(data.getTableId());

		TableContext ctx = new TableContext();
		ctx.setTableNum(data.getTableNum());
		ctx.setUrl(data.getUrl());
		ctx.setPageTitle(data.getPageTitle());
		ctx.setTableTitle(data.getTitle());
		ctx.setTextBeforeTable(data.getTextBeforeTable());
		ctx.setTextAfterTable(data.getTextAfterTable());
		ctx.setTimestampBeforeTable(data.getTableContextTimeStampBeforeTable());
		ctx.setTimestampAfterTable(data.getTableContextTimeStampAfterTable());
		ctx.setLastModified(data.getLastModified());
		t.setContext(ctx);

		// detect header, if TableHeaderDetector is set
		int[] headerRowIndex;
		int[] emptyRowCount =	getRowContentDetector().detectEmptyHeaderRows(data.getRelation(), true);	
		if (getTableHeaderDetector() != null) {		
			headerRowIndex = getTableHeaderDetector().detectTableHeader(data.getRelation(), emptyRowCount);
			if (headerRowIndex != null)
				data.setHeaderRowIndex(headerRowIndex[0]);
			else
				data.setHasHeader(false);
		} else {
			headerRowIndex = new int[1];
			headerRowIndex[0] = data.getHeaderRowIndex();
			if(emptyRowCount != null && ArrayUtils.contains(emptyRowCount, headerRowIndex[0])){
				data.setHeaderRowIndex(emptyRowCount.length);
				headerRowIndex[0] = emptyRowCount.length;
			}
		}

		// create the table columns
		parseColumnData(data, t);

		parseDependencies(data, t);
		parseCandidateKeys(data, t);

		// create the rows, transpose the data first to convert from
		// column-based to row-based representation
		data.transposeRelation();
		
		//check for total row
		int[] sumRowCount	= 	getRowContentDetector().detectSumRow(data.getRelation());

		// populate table Content
		int[] skipRows = ArrayUtils.addAll(emptyRowCount, headerRowIndex);
		skipRows = ArrayUtils.addAll(skipRows, sumRowCount);
		populateTable(data.getRelation(), t, skipRows);

		parseProvenance(data, t);

		if (isConvertValues()) {
			t.inferSchemaAndConvertValues(this.getTypeDetector());
		} else if (isInferSchema()) {
			t.inferSchema(this.getTypeDetector());
		}

		if (mapping != null) {
			t.setMapping(mapping);
		}

		if (isInferSchema() && !t.hasSubjectColumn()) {
			t.identifySubjectColumn();
		}

		return t;
	}

	protected void parseProvenance(JsonTableSchema data, Table table) {
		if (data.getRowProvenance() != null) {
			for (int i = 0; i < data.getRowProvenance().length; i++) {
				String[] prov = data.getRowProvenance()[i];
				table.get(i).setProvenance(new ArrayList<>(Arrays.asList(prov)));
			}
		}
		if (data.getColumnProvenance() != null) {
			for (int i = 0; i < data.getColumnProvenance().length; i++) {
				String[] prov = data.getColumnProvenance()[i];
				table.getSchema().get(i).setProvenance(new ArrayList<>(Arrays.asList(prov)));
			}
		}
	}

	protected void parseCandidateKeys(JsonTableSchema data, Table table) {
		if (data.getCandidateKeys() != null && data.getCandidateKeys().length > 0) {
			Collection<Set<TableColumn>> candidateKeys = new ArrayList<>(data.getCandidateKeys().length);
			for (Integer[] key : data.getCandidateKeys()) {
				Set<TableColumn> cols = new HashSet<>(key.length);
				for (Integer idx : key) {
					cols.add(table.getSchema().get(idx));
				}
				candidateKeys.add(cols);
			}
			table.getSchema().setCandidateKeys(candidateKeys);
		}
	}

	protected void parseDependencies(JsonTableSchema data, Table table) {
		if (data.getFunctionalDependencies() != null && data.getFunctionalDependencies().length > 0) {
			Map<Collection<TableColumn>, Collection<TableColumn>> dependencies = new HashMap<>();
			for (Dependency fd : data.getFunctionalDependencies()) {
				if (fd.getDeterminant() != null) {
					ArrayList<TableColumn> det = new ArrayList<>(fd.getDeterminant().length);
					for (Integer i : fd.getDeterminant()) {
						det.add(table.getSchema().get(i));
					}
					ArrayList<TableColumn> dep = new ArrayList<>(fd.getDeterminant().length);
					for (Integer i : fd.getDependant()) {
						dep.add(table.getSchema().get(i));
					}
					dependencies.put(det, dep);
				}
			}
			table.getSchema().setFunctionalDependencies(dependencies);
		}
	}

	protected void parseColumnData(JsonTableSchema data, Table table) {
		String[] columnNames = data.getColumnHeaders();

		try {
			for (int colIdx = 0; colIdx < data.getRelation().length; colIdx++) {
				String columnName = null;

				if (columnNames != null && columnNames.length > colIdx) {
					columnName = columnNames[colIdx];
				} else {
					columnName = "";
				}

				TableColumn c = new TableColumn(colIdx, table);
				c.setDataType(DataType.unknown);

				// set the header
				String header = columnName;
				if (isCleanHeader()) {
					header = this.getStringNormalizer().normaliseHeader(columnName);
				}
				c.setHeader(header);

				table.addColumn(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
