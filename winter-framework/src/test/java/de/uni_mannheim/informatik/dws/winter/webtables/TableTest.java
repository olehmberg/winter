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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.junit.Test;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.Table.ConflictHandling;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
 public class TableTest extends TestCase {
	 
	private static final Logger logger = WinterLogManager.getLogger();
	 
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
	@Test
	public void testInsertColumn() {
		Table t = new Table();
		TableColumn a = new TableColumn(0, t);
		a.setHeader("a");
		t.addColumn(a);
		TableColumn b = new TableColumn(1, t);
		b.setHeader("b");
		t.addColumn(b);

		t.getMapping().setMappedProperty(0, new Pair<>("a", 1.0));
		t.getMapping().setMappedProperty(1, new Pair<>("b", 1.0));

		TableColumn c = new TableColumn(1, t);
		c.setHeader("c");
		t.insertColumn(1,c);

		assertEquals("a", t.getSchema().get(0).getHeader());
		assertEquals("c", t.getSchema().get(1).getHeader());
		assertEquals("b", t.getSchema().get(2).getHeader());

		assertEquals("a", t.getMapping().getMappedProperty(0).getFirst());
		assertNull(t.getMapping().getMappedProperty(1));
		assertEquals("b", t.getMapping().getMappedProperty(2).getFirst());
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.Table#removeColumn(de.uni_mannheim.informatik.dws.winter.webtables.TableColumn)}.
	 */
	@Test
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
	
	@Test
	public void testDeduplicate() {
		Table t = new Table();
		t.setPath("table1");
		TableColumn c1 = new TableColumn(0, t);
		c1.setHeader("A");
		TableColumn c2 = new TableColumn(1, t);
		c2.setHeader("B");
		TableColumn c3 = new TableColumn(2, t);
		c3.setHeader("C");
		t.addColumn(c1);
		t.addColumn(c2);
		t.addColumn(c3);
		TableRow r1 = new TableRow(0, t);
		r1.set(new Object[] {"a", "a", "a"});
		TableRow r2 = new TableRow(1, t);
		r2.set(new Object[] {"a", "a", "b"});
		t.addRow(r1);
		t.addRow(r2);

		t.deduplicate(Q.toList(c1), ConflictHandling.KeepFirst);
		assertEquals(1,t.getSize());
		assertEquals("a", t.get(0).get(0));
		assertEquals("a", t.get(0).get(1));
		assertEquals("a", t.get(0).get(2));

		t.clear();
		r1 = new TableRow(0, t);
		r1.set(new Object[] {"a", "a", "a"});
		r2 = new TableRow(1, t);
		r2.set(new Object[] {"a", "a", "b"});
		t.addRow(r1);
		t.addRow(r2);
		t.deduplicate(Q.toList(c1), ConflictHandling.KeepBoth);
		assertEquals(2,t.getSize());
		assertEquals("a", t.get(0).get(0));
		assertEquals("a", t.get(0).get(1));
		assertEquals("a", t.get(0).get(2));
		assertEquals("a", t.get(1).get(0));
		assertEquals("a", t.get(1).get(1));
		assertEquals("b", t.get(1).get(2));

		t.clear();
		r1 = new TableRow(0, t);
		r1.set(new Object[] {"a", "a", null});
		r2 = new TableRow(1, t);
		r2.set(new Object[] {"a", null, "b"});
		t.addRow(r1);
		t.addRow(r2);
		t.deduplicate(Q.toList(c1), ConflictHandling.ReplaceNULLs);
		assertEquals(1,t.getSize());
		assertEquals("a", t.get(0).get(0));
		assertEquals("a", t.get(0).get(1));
		assertEquals("b", t.get(0).get(2));

		t.clear();
		r1 = new TableRow(0, t);
		r1.set(new Object[] {"a", "a", "a"});
		r2 = new TableRow(1, t);
		r2.set(new Object[] {"a", "a", "a"});
		t.addRow(r1);
		t.addRow(r2);
		t.deduplicate(Q.toList(c1), ConflictHandling.ReplaceNULLs);
		assertEquals(1,t.getSize());
		assertEquals("a", t.get(0).get(0));
		assertEquals("a", t.get(0).get(1));
		assertEquals("a", t.get(0).get(2));

		t.clear();
		r1 = new TableRow(0, t);
		r1.set(new Object[] {"a", "a", "a"});
		r2 = new TableRow(1, t);
		r2.set(new Object[] {"a", "a", "b"});
		t.addRow(r1);
		t.addRow(r2);
		t.deduplicate(Q.toList(c1), ConflictHandling.CreateList);
		assertEquals(1,t.getSize());
		assertEquals("a", t.get(0).get(0));
		assertTrue(Arrays.equals(new Object[] { "a", "a" }, (Object[])t.get(0).get(1)));
		assertTrue(Arrays.equals(new Object[] { "a", "b" }, (Object[])t.get(0).get(2)));
	
		t.clear();
		r1 = new TableRow(0, t);
		r1.set(new Object[] {"a", "a", "a"});
		r2 = new TableRow(1, t);
		r2.set(new Object[] {"a", "a", "b"});
		t.addRow(r1);
		t.addRow(r2);
		t.deduplicate(Q.toList(c1), ConflictHandling.CreateSet);
		assertEquals(1,t.getSize());
		assertEquals("a", t.get(0).get(0));
		assertEquals("a", t.get(0).get(1));
		assertTrue(Arrays.equals(new Object[] { "a", "b" }, (Object[])t.get(0).get(2)));
	}

	@Test
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
		t1.getMapping().setMappedClass(new Pair<>("A",1.0));
		t1.getMapping().setMappedProperty(0, new Pair<>("A", 1.0));
		t1.getMapping().setMappedProperty(2, new Pair<>("C", 1.0));
		t1.getMapping().setMappedInstance(0, new Pair<>("a",1.0));
		t1.getMapping().setMappedInstance(1, new Pair<>("b",1.0));
		t1.getMapping().setMappedInstance(2, new Pair<>("c1",1.0));
		
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
		t2.getMapping().setMappedClass(new Pair<>("A", 1.0));
		t2.getMapping().setMappedProperty(0, new Pair<>("C", 1.0));
		t2.getMapping().setMappedProperty(1, new Pair<>("D", 1.0));
		t2.getMapping().setMappedInstance(0, new Pair<>("a", 1.0));
		t2.getMapping().setMappedInstance(2, new Pair<>("c2", 1.0));
		
		Collection<Pair<TableColumn, TableColumn>> joinOn = new LinkedList<>();
		joinOn.add(new Pair<>(t1c3, t2c1));
		
		Table joined = t1.join(t2, joinOn, Q.toList(t1c1,t1c2,t1c3,t2c2,t2c3));
		
		for(TableRow r : joined.getRows()) {
		
			logger.info(r.format(10));
			
			switch (r.get(0).toString()) {
			case "a":
				assertEquals("1", r.get(4));
				assertEquals("a", joined.getMapping().getMappedInstance(r.getRowNumber()).getFirst());
				break;
			case "b":
				assertEquals("2", r.get(4));
				assertEquals("b", joined.getMapping().getMappedInstance(r.getRowNumber()).getFirst());
				break;
			case "c":
				assertEquals("3", r.get(4));
				assertNull(joined.getMapping().getMappedInstance(r.getRowNumber()));
				break;
			default:
				break;
			}
			
		}
		
		assertEquals("A", joined.getMapping().getMappedClass().getFirst());
		
		for(TableColumn c : joined.getColumns()) {
			
			switch (c.getIdentifier()) {
			case "A":
				assertEquals("A", joined.getMapping().getMappedProperty(c.getColumnIndex()));
				break;
			case "C":
				assertEquals("C", joined.getMapping().getMappedProperty(c.getColumnIndex()));
				break;
			case "D":
				assertEquals("D", joined.getMapping().getMappedProperty(c.getColumnIndex()));
				break;
			default:
				break;
			}
			
		}
		
		t2.getMapping().setMappedClass(new Pair<>("B", 1.0));
		t2.getMapping().setMappedProperty(0, new Pair<>("C2", 1.0));
		
		joined = t1.join(t2, joinOn, Q.toList(t1c1,t1c2,t1c3,t2c2,t2c3));
		
		assertNull(joined.getMapping().getMappedClass());
		for(TableColumn c : joined.getColumns()) {
			
			switch (c.getIdentifier()) {
			case "C":
				assertNull(joined.getMapping().getMappedProperty(c.getColumnIndex()));
				break;
			default:
				break;
			}
			
		}
	}

}
