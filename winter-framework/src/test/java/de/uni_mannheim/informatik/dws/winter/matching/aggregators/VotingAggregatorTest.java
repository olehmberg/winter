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
package de.uni_mannheim.informatik.dws.winter.matching.aggregators;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.SimpleKeyValueMapper;
import de.uni_mannheim.informatik.dws.winter.processing.parallel.ParallelProcessableCollection;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class VotingAggregatorTest extends TestCase {

	public void testVotingAggregator() {
		
		Attribute a1 = new Attribute("a1");
		Attribute a2 = new Attribute("a2");
		
		Processable<Correspondence<Attribute, Matchable>> data = new ProcessableCollection<>();
		Processable<Correspondence<Matchable, Matchable>> causes1 = new ProcessableCollection<>();
		causes1.add(new Correspondence<Matchable, Matchable>(null, null, 1.0));
		causes1.add(new Correspondence<Matchable, Matchable>(null, null, 1.0));
		causes1.add(new Correspondence<Matchable, Matchable>(null, null, 1.0));
		data.add(new Correspondence<Attribute, Matchable>(a1, a2, 0.1, causes1));
		
		Processable<Correspondence<Matchable, Matchable>> causes2 = new ProcessableCollection<>();
		causes2.add(new Correspondence<Matchable, Matchable>(null, null, 1.0));
		data.add(new Correspondence<Attribute, Matchable>(a1, a2, 0.5, causes2));
		
		Processable<Pair<Pair<Attribute, Attribute>, Correspondence<Attribute, Matchable>>> result = data.aggregate(new SimpleKeyValueMapper<>((c)->new Pair<>(c.getFirstRecord(),c.getSecondRecord()), (c)->c), new VotingAggregator<>(true,0.0));
		
		Pair<Pair<Attribute, Attribute>, Correspondence<Attribute, Matchable>> p = result.firstOrNull();
		assertNotNull(p);
		
		assertEquals(2, (int)(p.getSecond().getSimilarityScore()*10));
		
		
		
		data = new ParallelProcessableCollection<>();
		data.add(new Correspondence<Attribute, Matchable>(a1, a2, 0.1, causes1));
		data.add(new Correspondence<Attribute, Matchable>(a1, a2, 0.5, causes2));
		
		result = data.aggregate(new SimpleKeyValueMapper<>((c)->new Pair<>(c.getFirstRecord(),c.getSecondRecord()), (c)->c), new VotingAggregator<>(true,0.0));
		
		p = result.firstOrNull();
		assertNotNull(p);
		
		assertEquals(2, (int)(p.getSecond().getSimilarityScore()*10));
	}
	
}
