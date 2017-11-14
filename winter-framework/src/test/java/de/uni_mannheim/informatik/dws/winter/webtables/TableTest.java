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

import java.util.Collection;
import java.util.LinkedList;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
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
	
	public void testJoin() throws Exception {
		Table t1 = new Table();
		t1.setPath("table1");
		TableColumn t1c1 = new TableColumn(0, t1);
		t1c1.setHeader("A");
		TableColumn t1c2 = new TableColumn(1, t1);
		t1c2.setHeader("B");
		TableColumn t1c3 = new TableColumn(2, t1);
		t1c3.setHeader("C");
		t1.addColumn(t1c1);
		t1.addColumn(t1c2);
		t1.addColumn(t1c3);
		TableRow t1r1 = new TableRow(0, t1);
		t1r1.set(new Object[] {"a", "a", "a"});
		TableRow t1r2 = new TableRow(1, t1);
		t1r2.set(new Object[] {"b", "b", "b"});
		TableRow t1r3 = new TableRow(2, t1);
		t1r3.set(new Object[] {"c", "c", "c"});
		TableRow t1r4 = new TableRow(3, t1);
		t1r4.set(new Object[] {"d", "d", "d"});
		TableRow t1r5 = new TableRow(4, t1);
		t1.addRow(t1r1);
		t1.addRow(t1r2);
		t1.addRow(t1r3);
		t1.addRow(t1r4);
		t1.addRow(t1r5);
		
		Table t2 = new Table();
		t2.setPath("table2");
		t2.setTableId(1);
		TableColumn t2c1 = new TableColumn(0, t2);
		t2c1.setHeader("C");
		TableColumn t2c2 = new TableColumn(1, t2);
		t2c2.setHeader("D");
		TableColumn t2c3 = new TableColumn(2, t2);
		t2c3.setHeader("E");
		t2.addColumn(t2c1);
		t2.addColumn(t2c2);
		t2.addColumn(t2c3);
		TableRow t2r1 = new TableRow(0, t2);
		t2r1.set(new Object[] {"a", "1", "1"});
		TableRow t2r2 = new TableRow(1, t2);
		t2r2.set(new Object[] {"b", "2", "2"});
		TableRow t2r3 = new TableRow(2, t2);
		t2r3.set(new Object[] {"c", "3", "3"});
		TableRow t2r4 = new TableRow(3, t2);
		t2.addRow(t2r1);
		t2.addRow(t2r2);
		t2.addRow(t2r3);
		t2.addRow(t2r4);
		
		Collection<Pair<TableColumn, TableColumn>> joinOn = new LinkedList<>();
		joinOn.add(new Pair<>(t1c3, t2c1));
		
		Table joined = t1.join(t2, joinOn, Q.toList(t1c1,t1c2,t1c3,t2c2,t2c3));
		
		for(TableRow r : joined.getRows()) {
		
			System.out.println(r.format(10));
			
			switch (r.get(0).toString()) {
			case "a":
				assertEquals("1", r.get(4));
				break;
			case "b":
				assertEquals("2", r.get(4));
				break;
			case "c":
				assertEquals("3", r.get(4));
				break;
			default:
				break;
			}
			
		}
	}

}
