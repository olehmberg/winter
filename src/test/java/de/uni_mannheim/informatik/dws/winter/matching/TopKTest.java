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
package de.uni_mannheim.informatik.dws.winter.matching;

import de.uni_mannheim.informatik.dws.winter.matching.aggregators.TopKCorrespondencesAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.AggregateByFirstRecordRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import junit.framework.TestCase;

public class TopKTest extends TestCase {
	
	public void testTopK() {
		
		Processable<Correspondence<Record, Attribute>> correspondences = new ProcessableCollection<>();
		Record r1 = new Record("1", "");
		Record r2 = new Record("2", "");
		Record r3 = new Record("3", "");
		
		Correspondence<Record, Attribute> c1 = new Correspondence<Record, Attribute>(r1, r2, 1.0, null);
		Correspondence<Record, Attribute> c2 = new Correspondence<Record, Attribute>(r1, r3, 0.5, null);
		
		correspondences.add(c1);
		correspondences.add(c2);
		
		Processable<Pair<Pair<Record, Record>, Processable<Correspondence<Record, Attribute>>>> aggregated = correspondences.aggregateRecords(new AggregateByFirstRecordRule<>(0.0), new TopKCorrespondencesAggregator<>(1));
		
		correspondences = aggregated.transform((p,c)->
		{
			for(Correspondence<Record, Attribute> cor : p.getSecond().get()) {
				
				System.out.println(String.format("%s<->%s", cor.getFirstRecord().getIdentifier(), cor.getSecondRecord().getIdentifier()));
				
				c.next(cor);
			}
		});
		
		
		
		assertTrue(correspondences.size()==1);
		assertTrue(correspondences.get().iterator().next().getSecondRecord().getIdentifier().equals(r2.getIdentifier()));
	}

	public void testTopKWithThreshold() {
		
		Processable<Correspondence<Record, Attribute>> correspondences = new ProcessableCollection<>();
		Record r1 = new Record("1", "");
		Record r2 = new Record("2", "");
		Record r3 = new Record("3", "");
		
		Correspondence<Record, Attribute> c1 = new Correspondence<Record, Attribute>(r1, r2, 1.0, null);
		Correspondence<Record, Attribute> c2 = new Correspondence<Record, Attribute>(r1, r3, 0.5, null);
		
		correspondences.add(c1);
		correspondences.add(c2);
		
		Processable<Pair<Pair<Record, Record>, Processable<Correspondence<Record, Attribute>>>> aggregated = correspondences.aggregateRecords(new AggregateByFirstRecordRule<>(0.6), new TopKCorrespondencesAggregator<>(1));
		
		correspondences = aggregated.transform((p,c)->
		{
			for(Correspondence<Record, Attribute> cor : p.getSecond().get()) {
				
				System.out.println(String.format("%s<->%s", cor.getFirstRecord().getIdentifier(), cor.getSecondRecord().getIdentifier()));
				
				c.next(cor);
			}
		});
		
		
		
		assertTrue(correspondences.size()==1);
		assertTrue(correspondences.get().iterator().next().getSecondRecord().getIdentifier().equals(r2.getIdentifier()));
	}
}
