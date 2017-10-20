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

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.SimpleKeyValueMapper;
import de.uni_mannheim.informatik.dws.winter.processing.parallel.ParallelProcessableCollection;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class PerformanceAggregatorTest extends TestCase {

	public void testPerformanceAggregator() {
		
		Processable<Performance> data = new ProcessableCollection<>();
		data.add(new Performance(1, 2, 2));
		data.add(new Performance(2, 3, 4));
		
		Processable<Pair<Object, Performance>> result = data.aggregate(new SimpleKeyValueMapper<>((p1)->null, (p1)->p1), new PerformanceAggregator<>());
		
		Pair<Object, Performance> p = result.firstOrNull();
		assertNotNull(p);
		
		Performance perf = p.getSecond();
		
		assertEquals(3, perf.getNumberOfCorrectlyPredicted());
		assertEquals(5, perf.getNumberOfPredicted());
		assertEquals(6, perf.getNumberOfCorrectTotal());
		
		
		
		
		data = new ParallelProcessableCollection<>();
		data.add(new Performance(1, 2, 2));
		data.add(new Performance(2, 3, 4));
		
		result = data.aggregate(new SimpleKeyValueMapper<>((p1)->null, (p1)->p1), new PerformanceAggregator<>());
		
		p = result.firstOrNull();
		assertNotNull(p);
		
		perf = p.getSecond();
		
		assertEquals(3, perf.getNumberOfCorrectlyPredicted());
		assertEquals(5, perf.getNumberOfPredicted());
		assertEquals(6, perf.getNumberOfCorrectTotal());
	}
	
}
