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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableMapping;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableHeaderDetectorFirstRow;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeDetector;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeGuesser;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.WebTablesRowContentDetector;

/**
 * Loads a Web Table in the CSV format.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class CsvTableParser extends TableParser {

	public CsvTableParser() {
		setTypeDetector(new TypeGuesser());
		setTableHeaderDetector(new TableHeaderDetectorFirstRow());
		setStringNormalizer(new DynamicStringNormalizer());
		setRowContentDetector(new WebTablesRowContentDetector());
	}

	public CsvTableParser(TypeDetector pTypeDetector) {
		setTypeDetector(pTypeDetector);
		setTableHeaderDetector(new TableHeaderDetectorFirstRow());
		setStringNormalizer(new DynamicStringNormalizer());
		setRowContentDetector(new WebTablesRowContentDetector());
	}

	@Override
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

	@Override
	public Table parseTable(Reader reader, String fileName) throws IOException {
		// create new table
		Table table = new Table();
		table.setPath(fileName);

		TableMapping tm = new TableMapping();
		boolean typesAlreadyDetected = false;

		// read data
		List<String[]> tableListContent = null;

		try {
			// create reader
			CSVReader csvReader = new CSVReader(reader);

			tableListContent = csvReader.readAll();

			csvReader.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// check whether table content is not empty!
		if (tableListContent == null)
			return null;

		// skip annotations
		// if the current line starts with #, check for valid annotations
		boolean isMetaData = tableListContent.get(0)[0].startsWith("#");

		while (isMetaData) {
			isMetaData = false;

			// check all valid annotations
			for (String s : TableMapping.VALID_ANNOTATIONS) {
				if (tableListContent.get(0)[0].startsWith(s)) {
					isMetaData = true;
					break;
				}
			}

			// if the current line is an annotation, read the next line and
			// start over
			if (isMetaData) {
				// join the values back together and let the metadata parser
				// handle the line
				tm.parseMetadata(StringUtils.join(tableListContent.get(0), ","));

				tableListContent.remove(0);
				isMetaData = tableListContent.get(0)[0].startsWith("#");
			}
		}

		int maxWidth = 0;
		for(String[] line : tableListContent) {
			maxWidth = Math.max(maxWidth, line.length);
		}
		
		// convert content into String[][] format for easier processing.
		String[][] tableContent = new String[tableListContent.size()][];	
		tableListContent.toArray(tableContent);
		
		// make sure all rows have the same length!
		for(int i = 0; i < tableContent.length; i++) {
			if(tableContent[i].length<maxWidth) {
				tableContent[i] = Arrays.copyOf(tableContent[i], maxWidth);
			}
		}

		// close CSV Reader
		int[] emptyRowCount		=	getRowContentDetector().detectEmptyHeaderRows(tableContent, false);
		int[] headerRowCount 	= 	getTableHeaderDetector().detectTableHeader(tableContent, emptyRowCount);

		int colIdx = 0;
		// set the header, if possible
		if (headerRowCount != null) {

			for (String columnName : tableContent[headerRowCount[0]]) {
				TableColumn c = new TableColumn(colIdx, table);

				String header = columnName;
				if (isCleanHeader()) {
					header = this.getStringNormalizer().normaliseHeader(header);
				}
				c.setHeader(header);

				if (tm.getDataType(colIdx) != null) {
					c.setDataType((DataType) tm.getDataType(colIdx));
					typesAlreadyDetected = true;
				} else {
					c.setDataType(DataType.unknown);
				}

				table.addColumn(c);

				colIdx++;
			}

		}
		
		//check for total row
		int[] sumRowCount	= 	getRowContentDetector().detectSumRow(tableContent);
		
		// populate table content
		int[] skipRows = ArrayUtils.addAll(emptyRowCount, headerRowCount);
		skipRows = ArrayUtils.addAll(skipRows, sumRowCount);
		populateTable(tableContent, table, skipRows);

		if (typesAlreadyDetected && isConvertValues()) {
			table.convertValues();
		} else if (isConvertValues()) {
			table.inferSchemaAndConvertValues(this.getTypeDetector());
		} else {
			table.inferSchema(this.getTypeDetector());
		}

		if (!table.hasSubjectColumn()) {
			table.identifySubjectColumn();
		}

		return table;

	}

}
