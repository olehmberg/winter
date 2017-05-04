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

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableTest extends TestCase {

	private Table getTestTable() {
		
		Table table = new Table();
		
		TableColumn c = new TableColumn(0, table);
		c.setHeader("Column 1");
		table.addColumn(c);
		c = new TableColumn(1, table);
		c.setHeader("Column 2");
		table.addColumn(c);
		c = new TableColumn(2, table);
		c.setHeader("Column 3");
		table.addColumn(c);
		
		TableRow r = new TableRow(0, table);
		r.set(new String[] { "row 1 col 1", "row 1 col 2", "row 1 col 3" });
		table.addRow(r);
		r = new TableRow(1, table);
		r.set(new String[] { "row 2 col 1", "row 2 col 2", "row 2 col 3" });
		table.addRow(r);
		r = new TableRow(2, table);
		r.set(new String[] { "row 3 col 1", "row 3 col 2", "row 3 col 3" });
		table.addRow(r);
		
		return table;
	}
	
	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#insertColumn(int, de.uni_mannheim.informatik.dws.winter.webtables.TableColumn)}.
	 */
	public void testInsertColumn() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#removeColumn(de.uni_mannheim.informatik.dws.winter.webtables.TableColumn)}.
	 */
	public void testRemoveColumn() {
		Table table = getTestTable();
		
		TableColumn col = table.getSchema().get(1);
		table.removeColumn(col);
		
		assertEquals(2, table.getColumns().size());
		assertEquals("Column 1", table.getSchema().get(0).getHeader());
		assertEquals("Column 3", table.getSchema().get(1).getHeader());
		assertEquals("row 1 col 1", table.get(0).get(0));
		assertEquals("row 1 col 3", table.get(0).get(1));
		assertEquals("row 2 col 1", table.get(1).get(0));
		assertEquals("row 2 col 3", table.get(1).get(1));
		assertEquals("row 3 col 1", table.get(2).get(0));
		assertEquals("row 3 col 3", table.get(2).get(1));
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#inferSchemaAndConvertValues()}.
	 */
	public void testInferSchemaAndConvertValues() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#inferSchema()}.
	 */
	public void testInferSchema() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#convertValues()}.
	 */
	public void testConvertValues() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#identifySubjectColumn()}.
	 */
	public void testIdentifyKey() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#append(de.uni_mannheim.informatik.dws.winter.webtables.Table)}.
	 */
	public void testAppend() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#clearProvenance()}.
	 */
	public void testClearProvenance() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#reorganiseRowNumbers()}.
	 */
	public void testReorganiseRowNumbers() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#project(java.util.Collection)}.
	 */
	public void testProject() {
//		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#copySchema()}.
	 */
	public void testCopySchema() {
//		fail("Not yet implemented");
	}

}
