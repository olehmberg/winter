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
package de.uni_mannheim.informatik.dws.winter.processing.parallel;

import java.util.Collection;
import java.util.HashSet;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Group;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ParallelProcessableCollectionTest extends TestCase {

	public void testJoin() {
		
		Processable<String> data1 = new ParallelProcessableCollection<>();
		data1.add("a");
		data1.add("b");
		data1.add("c");
		data1.add("e");
		Processable<String> data2 = new ParallelProcessableCollection<>();
		data2.add("a");
		data2.add("b");
		data2.add("c");
		data2.add("d");
		
		Processable<Pair<String, String>> result = data1.join(data2, (s)->s);
		
		assertTrue(result instanceof ParallelProcessableCollection);
		
		Collection<Object> values = result.map((p,r)->r.next(p.getFirst())).get();
		
		assertEquals(Q.toSet("a", "b", "c"), new HashSet<>(values));
		
	}
	
	public void testMap() {

		Processable<String> data1 = new ParallelProcessableCollection<>();
		data1.add("a");
		data1.add("b");
		data1.add("c");
		
		data1 = data1.map((String record, DataIterator<String> resultCollector) -> resultCollector.next(record + record));
		
		assertEquals(Q.toSet("aa","bb","cc"), new HashSet<>(data1.get()));
		
	}
	
	public void testGroup() {
		Processable<String> data1 = new ParallelProcessableCollection<>();
		data1.add("a");
		data1.add("b");
		data1.add("c");
		
		Processable<Group<String, String>> grouped = data1
				.group((String record, DataIterator<Pair<String, String>> resultCollector) -> resultCollector.next(new Pair<>(record, record)));
		
		assertEquals(3, grouped.size());
		for(Group<String, String> group : grouped.get()) {
			String element = group.getRecords().firstOrNull();
			assertEquals(group.getKey(), element);
		}
	}
}
