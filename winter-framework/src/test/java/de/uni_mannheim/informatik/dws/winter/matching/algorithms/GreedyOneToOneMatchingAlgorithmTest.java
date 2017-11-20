package de.uni_mannheim.informatik.dws.winter.matching.algorithms;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import junit.framework.TestCase;

public class GreedyOneToOneMatchingAlgorithmTest extends TestCase {

	public void testRun() {
		 // Input: a-b (0.9), a-c (0.8), d-b (0.8), d-c (0.1)
		 // Output: a-b (0.9), d-c (0.1)
		
		Correspondence<Record, Matchable> ab = new Correspondence<Record, Matchable>(new Record("a"), new Record("b"), 0.9);
		Correspondence<Record, Matchable> ac = new Correspondence<Record, Matchable>(new Record("a"), new Record("c"), 0.8);
		Correspondence<Record, Matchable> db = new Correspondence<Record, Matchable>(new Record("d"), new Record("b"), 0.8);
		Correspondence<Record, Matchable> dc = new Correspondence<Record, Matchable>(new Record("d"), new Record("c"), 0.1);
		
		ProcessableCollection<Correspondence<Record, Matchable>> cors = new ProcessableCollection<>();
		cors.add(ab);
		cors.add(ac);
		cors.add(db);
		cors.add(dc);
		
		GreedyOneToOneMatchingAlgorithm<Record, Matchable> greedy = new GreedyOneToOneMatchingAlgorithm<>(cors);
		greedy.run();
		Processable<Correspondence<Record, Matchable>> result = greedy.getResult();
		
		assertEquals(2, result.size());
		
		for(Correspondence<Record, Matchable> cor : result.get()) {
			if(cor.getFirstRecord().getIdentifier().equals("a")) {
				assertEquals("b", cor.getSecondRecord().getIdentifier());
			}
			if(cor.getFirstRecord().getIdentifier().equals("d")) {
				assertEquals("c", cor.getSecondRecord().getIdentifier());
			}
		}
	}

}
