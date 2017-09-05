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
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang.ArrayUtils;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.RowContentDetector;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableHeaderDetector;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeDetector;

/**
 * Load a Web Table in the CSV and JSON format.
 * 
 * @author Alexander Brinkmann
 *
 */

public abstract class TableParser {

	// normalizes the table header
	private boolean cleanHeader = true;

	/**
	 * Holds typeDetector for data type detection. It is possible to implement
	 * your own TypeDetector by using the corresponding Interface.
	 */
	private TypeDetector typeDetector;

	/**
	 * Holds tableHeaderDetector to Detect a table header. It is possible to
	 * implement your own header detector by using the corresponding Interface.
	 */
	private TableHeaderDetector tableHeaderDetector;
	
	/**
	 * Holds the stringNormalizer used to normalizes input strings. It is possible to
	 * implement your own string normalizer by using the corresponding Interface.
	 */
	private StringNormalizer stringNormalizer;
	
	/**
	 * Holds the rowContentDetector used to analyze the table rows before populating the table. It is possible to
	 * implement your own row content detector by using the corresponding Interface.
	 */
	private RowContentDetector rowContentDetector;
	
	private boolean convertValues = true;

	public boolean isConvertValues() {
		return convertValues;
	}

	public void setConvertValues(boolean convertValues) {
		this.convertValues = convertValues;
	}

	public boolean isCleanHeader() {
		return cleanHeader;
	}

	public void setCleanHeader(boolean cleanHeader) {
		this.cleanHeader = cleanHeader;
	}

	public TypeDetector getTypeDetector() {
		return typeDetector;
	}

	public void setTypeDetector(TypeDetector typeDetector) {
		this.typeDetector = typeDetector;
	}

	public TableHeaderDetector getTableHeaderDetector() {
		return tableHeaderDetector;
	}

	public void setTableHeaderDetector(TableHeaderDetector tableHeaderDetector) {
		this.tableHeaderDetector = tableHeaderDetector;
	}
	
	public StringNormalizer getStringNormalizer() {
		return stringNormalizer;
	}

	public void setStringNormalizer(StringNormalizer stringNormalizer) {
		this.stringNormalizer = stringNormalizer;
	}
	
	public RowContentDetector getRowContentDetector() {
		return rowContentDetector;
	}

	public void setRowContentDetector(RowContentDetector rowContentDetector) {
		this.rowContentDetector = rowContentDetector;
	}
	
	/**
	 * Writes the content from a String[][] into the provided table.
	 * 
	 * @param tContent
	 *            has the content, which has to be populated
	 * @param t
	 *            has the table, which is populated
	 * @param skipRows
	 *            holds the indexes for header, empty and sum rows. These rows are skipped,
	 *            because they do not belong to the content. Likewise these rows
	 *            do not increase the row id, thus table row id and parsed table
	 *            row id maid slightly differ depending on the header.
	 */

	public void populateTable(String[][] tContent, Table t, int[] skipRows) {
		int tableRowIndex = 0;
		for (int rowIdx = 0; rowIdx < tContent.length; rowIdx++) {
			if (!ArrayUtils.contains(skipRows, rowIdx)) {
				String[] rowData = tContent[rowIdx];
				Object[] values = new Object[tContent[rowIdx].length];
				for (int i = 0; i < rowData.length && i < values.length; i++) {
					if (rowData[i] != null && !rowData[i].trim().isEmpty()) {
						values[i] = stringNormalizer.normaliseValue(rowData[i], false);

						if (((String) values[i]).equalsIgnoreCase(StringNormalizer.nullValue)) {
							values[i] = null;
						} else {
							values[i] = values[i];
						}
					}
				}
				
				// make sure the row number is the row's position in the table.
				TableRow r = new TableRow(tableRowIndex, t);    				
				tableRowIndex++;
				r.set(values);
				t.addRow(r);
			}
		}
	}
	
	public abstract Table parseTable(File file);
	public abstract Table parseTable(Reader reader, String fileName) throws IOException;


}
