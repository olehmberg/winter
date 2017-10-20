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
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.SimpleKeyValueMapper;
import de.uni_mannheim.informatik.dws.winter.processing.parallel.ParallelProcessableCollection;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class CountAggregatorTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.processing.aggregators.CountAggregator#initialise(java.lang.Object)}.
	 */
	public void testCountAggregator() {
		Processable<Double> values = new ProcessableCollection<>();
		values.add(1.0);
		values.add(2.0);
		values.add(3.0);
		values.add(4.0);
		values.add(5.0);
		
		Processable<Pair<Integer, Integer>> result = values.aggregate(new SimpleKeyValueMapper<>((d)->0,(d)->d), new CountAggregator<>());
		
		Pair<Integer, Integer> p = result.firstOrNull();
		
		assertNotNull(p);
		
		Integer average = p.getSecond();
		
		assertEquals(5, average.intValue());
		
		
		
		values = new ParallelProcessableCollection<>();
		values.add(1.0);
		values.add(2.0);
		values.add(3.0);
		values.add(4.0);
		values.add(5.0);
		
		result = values.aggregate(new SimpleKeyValueMapper<>((d)->0,(d)->d), new CountAggregator<>());
		
		p = result.firstOrNull();
		
		assertNotNull(p);
		
		average = p.getSecond();
		
		assertEquals(5, average.intValue());
	}

}
