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
package de.uni_mannheim.informatik.dws.winter.processing.aggregators;

import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.SimpleKeyValueMapper;
import de.uni_mannheim.informatik.dws.winter.processing.parallel.ParallelProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class SetAggregatorTest extends TestCase {
	
	public void testSetAggregator() {		
		Processable<String> col = new ProcessableCollection<>();
		col.add("a");
		col.add("b");
		col.add("c");
		col.add("b");
		col.add("d");
		
		Processable<Pair<Object, Set<String>>> result = col.aggregate(new SimpleKeyValueMapper<>((s)->null, (s)->s), new SetAggregator<>());
		
		Pair<Object, Set<String>> p = result.firstOrNull();
		
		assertNotNull(p);
		
		Set<String> set = p.getSecond();
		
		assertEquals(Q.toSet("a", "b", "c", "d"), set);
		
		
		col = new ParallelProcessableCollection<>();
		col.add("a");
		col.add("b");
		col.add("c");
		col.add("b");
		col.add("d");
		
		result = col.aggregate(new SimpleKeyValueMapper<>((s)->null, (s)->s), new SetAggregator<>());
		
		p = result.firstOrNull();
		
		assertNotNull(p);
		
		set = p.getSecond();
		
		assertEquals(Q.toSet("a", "b", "c", "d"), set);
	}

}
