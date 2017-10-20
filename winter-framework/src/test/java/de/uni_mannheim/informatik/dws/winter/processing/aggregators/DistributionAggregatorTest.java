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
import de.uni_mannheim.informatik.dws.winter.utils.Distribution;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class DistributionAggregatorTest extends TestCase {

	public void testDistributionAggregator() {
		Processable<String> data = new ProcessableCollection<>();
		data.add("a");
		data.add("a");
		data.add("a");
		data.add("b");
		data.add("b");
		data.add("c");
		
		Processable<Pair<Object, Distribution<String>>> result = data.aggregate(new SimpleKeyValueMapper<>((s)->null, (s)->s), new DistributionAggregator<Object, String, String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getInnerKey(String record) {
				return record;
			}
		});
		
		Pair<Object, Distribution<String>> p = result.firstOrNull();
		
		assertNotNull(p);
		
		Distribution<String> dist = p.getSecond();
		
		assertEquals(3, dist.getFrequency("a"));
		assertEquals(2, dist.getFrequency("b"));
		assertEquals(1, dist.getFrequency("c"));
		
		
		data = new ParallelProcessableCollection<>();
		data.add("a");
		data.add("a");
		data.add("a");
		data.add("b");
		data.add("b");
		data.add("c");
		
		result = data.aggregate(new SimpleKeyValueMapper<>((s)->null, (s)->s), new DistributionAggregator<Object, String, String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getInnerKey(String record) {
				return record;
			}
		});
		
		p = result.firstOrNull();
		
		assertNotNull(p);
		
		dist = p.getSecond();
		
		assertEquals(3, dist.getFrequency("a"));
		assertEquals(2, dist.getFrequency("b"));
		assertEquals(1, dist.getFrequency("c"));
	}
	
}
