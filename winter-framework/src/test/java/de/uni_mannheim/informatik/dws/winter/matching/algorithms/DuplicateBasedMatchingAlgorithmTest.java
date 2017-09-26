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
package de.uni_mannheim.informatik.dws.winter.matching.algorithms;

import de.uni_mannheim.informatik.dws.winter.matching.aggregators.TopKVotesAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.aggregators.VotingAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.DuplicateBasedMatchingAlgorithm;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.NoSchemaBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.VotingMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class DuplicateBasedMatchingAlgorithmTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.matching.algorithms.DuplicateBasedMatchingAlgorithm#run()}.
	 */
	public void testRun() {
		
		HashedDataSet<Record, Attribute> dataset1 = new HashedDataSet<>();
		HashedDataSet<Record, Attribute> dataset2 = new HashedDataSet<>();
		
		Attribute a1 = new Attribute("a1");
		Attribute a2 = new Attribute("a2");
		dataset1.addAttribute(a1);
		dataset1.addAttribute(a2);
		
		Attribute b1 = new Attribute("b1");
		Attribute b2 = new Attribute("b2");
		dataset2.addAttribute(b1);
		dataset2.addAttribute(b2);
		
		Record r1 = new Record("r1", "");
		r1.setValue(a1, "a b");
		r1.setValue(a2, "b");
//		Record r2 = new Record("r2", "");
//		r2.setValue(a2, "b c");
		dataset1.add(r1);
//		dataset1.add(r2);
		
		Record s1 = new Record("s1", "");
		s1.setValue(b1, "b");
		s1.setValue(b2, "b c");
//		Record s2 = new Record("s2", "");
//		s2.setValue(b1, "a b");
		dataset2.add(s1);
//		dataset2.add(s2);
		
		ProcessableCollection<Correspondence<Record, Attribute>> duplicates = new ProcessableCollection<>();
		duplicates.add(new Correspondence<Record, Attribute>(r1, s1, 1.0, null));
		
		VotingMatchingRule<Attribute, Record> rule = new VotingMatchingRule<Attribute, Record>(0.4) {
			
			private static final long serialVersionUID = 1L;
			TokenizingJaccardSimilarity sim = new TokenizingJaccardSimilarity();
			
			@Override
			public double compare(Attribute record1, Attribute record2, Correspondence<Record, Matchable> schemaCorrespondences) {
				String v1 = schemaCorrespondences.getFirstRecord().getValue(record1);
				String v2 = schemaCorrespondences.getSecondRecord().getValue(record2);
				
				double d = sim.calculate(v1, v2);
				
				System.out.println(String.format("Vote: %s<->%s %f: %s<->%s", record1, record2, d, v1, v2));
				
				return d;
			}
		};
		
		
		
		DuplicateBasedMatchingAlgorithm<Record, Attribute> algo = new DuplicateBasedMatchingAlgorithm<>(
				dataset1.getSchema(), 
				dataset2.getSchema(), 
				Correspondence.toMatchable(duplicates), 
				rule,
				new TopKVotesAggregator<>(1),
				new VotingAggregator<>(true, 0.6), 
				new NoSchemaBlocker<>());
		
		algo.run();
		
		Processable<Correspondence<Attribute, Record>> result = algo.getResult();
		
		for(Correspondence<Attribute, Record> cor : result.get()) {
			System.out.println(String.format("%s<->%s %f", cor.getFirstRecord().getIdentifier(), cor.getSecondRecord().getIdentifier(), cor.getSimilarityScore()));
			
			for(Correspondence<Record, Matchable> cause : cor.getCausalCorrespondences().get()) {
				System.out.println(String.format("\t%s<->%s %f", cause.getFirstRecord().getValue(cor.getFirstRecord()), cause.getSecondRecord().getValue(cor.getSecondRecord()), cause.getSimilarityScore()));
			}
		}
	}

}
