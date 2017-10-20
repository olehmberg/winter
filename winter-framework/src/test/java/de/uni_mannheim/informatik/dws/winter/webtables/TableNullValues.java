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

import java.io.File;

import de.uni_mannheim.informatik.dws.winter.webtables.detectors.WebTablesRowContentDetector;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableParser;
import junit.framework.TestCase;

/**
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */
public class TableNullValues extends TestCase {

	public void testDetectNullValues() {
		
		CsvTableParser csvParser = new CsvTableParser();
		Table table = csvParser.parseTable(new File("src/test/resource/testTable.csv"));
		

		assertEquals("column1", table.getSchema().get(0).getHeader());
		assertEquals("row 1 col 1", table.get(0).get(0));
		assertEquals(null, table.get(0).get(1));
		assertEquals("row 2 col 1", table.get(1).get(0));
		assertEquals(null, table.get(1).get(1));
		assertEquals(null, table.get(2).get(2));
		assertEquals(3, table.getSize());
		
		JsonTableParser jsonParser = new JsonTableParser();
		jsonParser.setRowContentDetector(new WebTablesRowContentDetector());
		Table table2 = jsonParser.parseTable(new File("src/test/resource/testTable.json"));

		assertEquals("player", table2.getSchema().get(2).getHeader());
	}

	}
