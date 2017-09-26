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
package de.uni_mannheim.informatik.dws.winter.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Group;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.aggregators.CountAggregator;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ProcessableCollectionTest extends TestCase {

	protected static final int NUM_TEST_RECORDS = 5;
	protected static final int NUM_UNIQUE_TEST_RECORDS = 7;
	protected static final int NUM_OVERLAPPING_TEST_RECORDS = 3;
	protected static final String[] TEST_RECORD_IDS = { "1", "2", "3", "4", "5" };
	protected static final String[] TEST_RECORD_IDS2 = { TEST_RECORD_IDS[2], TEST_RECORD_IDS[3], TEST_RECORD_IDS[4], "6", "7" };
	protected static final String[] TEST_OVERLAPPING_RECORD_IDS = { TEST_RECORD_IDS[2], TEST_RECORD_IDS[3], TEST_RECORD_IDS[4]};
	
	protected ProcessableCollection<Record> getTestData() {
		ProcessableCollection<Record> data = new ProcessableCollection<>();
		data.add(new Record(TEST_RECORD_IDS[0], ""));
		data.add(new Record(TEST_RECORD_IDS[1], ""));
		data.add(new Record(TEST_RECORD_IDS[2], ""));
		data.add(new Record(TEST_RECORD_IDS[3], ""));
		data.add(new Record(TEST_RECORD_IDS[4], ""));
		return data;
	}
	
	protected ProcessableCollection<Record> getTestData2() {
		ProcessableCollection<Record> data = new ProcessableCollection<>();
		data.add(new Record(TEST_RECORD_IDS2[0], ""));
		data.add(new Record(TEST_RECORD_IDS2[1], ""));
		data.add(new Record(TEST_RECORD_IDS2[2], ""));
		data.add(new Record(TEST_RECORD_IDS2[3], ""));
		data.add(new Record(TEST_RECORD_IDS2[4], ""));
		return data;
	}
	
	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#ProcessableCollection()}.
	 */
	public void testProcessableCollection() {
		new ProcessableCollection<>();
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#ProcessableCollection(java.util.Collection)}.
	 */
	public void testProcessableCollectionCollectionOfRecordType() {
		Collection<Record> col = new ArrayList<>(5);
		col.add(new Record("1",""));
		col.add(new Record("2",""));
		col.add(new Record("3",""));
		new ProcessableCollection<>(col);
	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#add(java.lang.Object)}.
//	 */
//	public void testAdd() {
//		assertEquals(NUM_TEST_RECORDS+1, getTestData().add(new Record("6", "")).size());
//	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#get()}.
	 */
	public void testGet() {
		Collection<Record> col = getTestData().get();
		
		assertEquals(NUM_TEST_RECORDS, col.size());
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#size()}.
	 */
	public void testSize() {
		assertEquals(NUM_TEST_RECORDS, getTestData().size());
	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#merge(de.uni_mannheim.informatik.wdi.model.Result)}.
//	 */
//	public void testMerge() {
//		Processable<Record> merged = getTestData().merge(getTestData2());
//		
//		assertEquals(NUM_TEST_RECORDS*2, merged.size());
//	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#remove(java.lang.Object)}.
//	 */
//	public void testRemoveRecordType() {
//		Processable<Record> data = getTestData();
//		List<Record> list = new ArrayList<>(data.get());
//		
//		data = data.remove(list.get(0));
//		
//		assertEquals(NUM_TEST_RECORDS-1, data.size());
//	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#remove(java.util.Collection)}.
//	 */
//	public void testRemoveCollectionOfRecordType() {
//		Processable<Record> data = getTestData();
//		List<Record> list = new ArrayList<>(data.get());
//		List<Record> toRemove = new ArrayList<>();
//		toRemove.add(list.get(0));
//		toRemove.add(list.get(1));
//		
//		data = data.remove(toRemove);
//		
//		assertEquals(NUM_TEST_RECORDS-toRemove.size(), data.size());
//	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#deduplicate()}.
//	 */
//	public void testDeduplicate() {
//		Processable<Record> data = getTestData().merge(getTestData2()).deduplicate();
//		
//		assertEquals(NUM_OVERLAPPING_TEST_RECORDS, data.size());
//	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#createProcessable(java.lang.Object)}.
	 */
	public void testCreateResult() {
		new ProcessableCollection<>().createProcessable(new Record("", ""));
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#assignUniqueRecordIds(de.uni_mannheim.informatik.dws.winter.processing.Function)}.
	 */
	public void testAssignUniqueRecordIds() {
		Attribute uniqueIdAttribute = new Attribute("id");
		Processable<Record> data = getTestData().assignUniqueRecordIds(
				(p)-> {
					p.getSecond().setValue(uniqueIdAttribute, Long.toString(p.getFirst()));
					return p.getSecond();
				});
		
		Set<String> ids = new HashSet<>();
		for(Record r : data.get()) {
			ids.add(r.getValue(uniqueIdAttribute));
		}
		
		assertEquals(data.size(), ids.size());
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#foreach(de.uni_mannheim.informatik.dws.winter.processing.DataIterator)}.
	 */
	public void testIterateDataset() {
		final Collection<Record> iterated = new LinkedList<>();
		
		getTestData().foreach(new DataIterator<Record>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void next(Record record) {
				iterated.add(record);
			}
			
			@Override
			public void initialise() {
			}
			
			@Override
			public void finalise() {
			}
		});
		
		assertEquals(NUM_TEST_RECORDS, iterated.size());
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#map(de.uni_mannheim.informatik.dws.winter.processing.RecordMapper)}.
	 */
	public void testTransform() {
		Processable<String> transformed = getTestData().map((r,c)->c.next(r.getIdentifier()));
		
		Set<String> expected = new HashSet<String>(Arrays.asList(TEST_RECORD_IDS));
		Set<String> actual = new HashSet<>(transformed.get());
		
		assertEquals(expected, actual);
		
	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#hashRecords(de.uni_mannheim.informatik.wdi.model.WrappedCollection, de.uni_mannheim.informatik.wdi.processing.Function)}.
//	 */
//	public void testHashRecords() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection#symmetricJoin(de.uni_mannheim.informatik.dws.winter.processing.Function)}.
	 */
	public void testSymmetricJoinWrappedCollectionOfRecordTypeFunctionOfKeyTypeRecordType() {
		getTestData().symmetricJoin((r)->r.getIdentifier());
	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#symmetricJoin(de.uni_mannheim.informatik.wdi.model.WrappedCollection, de.uni_mannheim.informatik.wdi.processing.Function, de.uni_mannheim.informatik.wdi.processing.ResultSetCollector)}.
//	 */
//	public void testSymmetricJoinWrappedCollectionOfRecordTypeFunctionOfKeyTypeRecordTypeResultSetCollectorOfPairOfRecordTypeRecordType() {
//		fail("Not yet implemented");
//	}

	public void testJoinWrappedCollectionOfRecordTypeWrappedCollectionOfRecordTypeFunctionOfKeyTypeRecordType() {
		Processable<Pair<Record, Record>> result = getTestData().join(getTestData2(), (r)->r.getIdentifier());
		
		assertEquals(NUM_OVERLAPPING_TEST_RECORDS, result.size());
	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#join(de.uni_mannheim.informatik.wdi.model.WrappedCollection, de.uni_mannheim.informatik.wdi.model.WrappedCollection, de.uni_mannheim.informatik.wdi.processing.Function, de.uni_mannheim.informatik.wdi.processing.Function)}.
//	 */
//	public void testJoinWrappedCollectionOfRecordType1WrappedCollectionOfRecordType2FunctionOfKeyTypeRecordType1FunctionOfKeyTypeRecordType2() {
//		fail("Not yet implemented");
//	}


	public void testLeftJoin() {
		Processable<Pair<Record, Record>> result = getTestData().leftJoin(getTestData2(), (r)->r.getIdentifier());
		
		assertEquals(NUM_TEST_RECORDS, result.size());
	}


	public void testGroupRecords() {
		Processable<Group<Object, Record>> grouped = getTestData().group((r,c)->c.next(new Pair<>(r.getDataSourceIdentifier(),r)));
		
		assertEquals(1, grouped.size());
		
		Group<Object, Record> group = grouped.get().iterator().next();
		
		assertEquals(NUM_TEST_RECORDS, group.getRecords().size());
		
	}


	public void testAggregateRecords() {
		Processable<Pair<Object, Integer>> aggregated = getTestData().aggregate((r,c)->c.next(new Pair<>(r.getDataSourceIdentifier(), r)), new CountAggregator<>());
		
		assertEquals(1, aggregated.size());
		
		Pair<Object, Integer> value = aggregated.get().iterator().next();
		
		assertEquals(NUM_TEST_RECORDS, value.getSecond().intValue());
	}

	public void testSortWrappedCollectionOfElementTypeFunctionOfKeyTypeElementType() {
		Processable<Record> sorted = getTestData().sort((r)->r.getIdentifier(), false);
		
		assertEquals(TEST_RECORD_IDS[4], sorted.get().iterator().next().getIdentifier());
	}

//	/**
//	 * Test method for {@link de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#sort(de.uni_mannheim.informatik.wdi.model.WrappedCollection, de.uni_mannheim.informatik.wdi.processing.Function, boolean)}.
//	 */
//	public void testSortWrappedCollectionOfElementTypeFunctionOfKeyTypeElementTypeBoolean() {
//		fail("Not yet implemented");
//	}


	public void testFilter() {
		Processable<Record> filtered = getTestData().where((r)->Integer.parseInt(r.getIdentifier())>3);
		
		assertEquals(2, filtered.size());
	}


	public void testCoGroup() {
		Processable<Pair<Iterable<Record>,Iterable<Record>>> grouped = getTestData().coGroup(getTestData2(), (r)->r.getIdentifier(), (r)->r.getIdentifier(), (r,c)->c.next(r));
		
		assertEquals(NUM_OVERLAPPING_TEST_RECORDS, grouped.size());
	}

	public void testAppend() {
		assertEquals(NUM_TEST_RECORDS*2, getTestData().append(getTestData2()).size());
	}

	public void testDistinct() {
		assertEquals(NUM_UNIQUE_TEST_RECORDS, getTestData().append(getTestData2()).distinct().size());
	}

	public void testTake() {
		assertEquals(2, getTestData().take(2).size());
	}

}
