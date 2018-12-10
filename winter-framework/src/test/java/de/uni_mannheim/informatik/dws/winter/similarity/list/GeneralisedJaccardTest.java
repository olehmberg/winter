package de.uni_mannheim.informatik.dws.winter.similarity.list;

import java.util.Collection;

import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import junit.framework.TestCase;

/**
 * @author Charles Lanahan (charles.lanahan@trustscience.com)
 */
public class GeneralisedJaccardTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.similarity.list.GeneralizedJaccard#calculate(java.util.Collection, java.util.Collection)}.
	 */
	public void testCalculateCollectionOfTCollectionOfT() {
		
		Collection<String> c1 = Q.toList("a1", "bbb2", "ccccc3", "ddddddd4");
		Collection<String> c2 = Q.toList("a1");
		Collection<String> c3 = Q.toList("aY");
		Collection<String> c4 = Q.toList("cccccX", "dddddddX", "eeeeeeeeeX", "fffffffffffX");
		
		GeneralisedJaccard<String> sim = new GeneralisedJaccard<>(new LevenshteinSimilarity(),
                0.5);
		
		assertEquals(0.25, sim.calculate(c1, c2), 0.01);
		assertEquals(0.25, sim.calculate(c2, c1), 0.01);
		
		assertEquals(0.111, sim.calculate(c1, c3), 0.01);
		assertEquals(0.111, sim.calculate(c3, c1), 0.01);
		
		assertEquals(0.0, sim.calculate(c2, c4), 0.01);
		assertEquals(0.0, sim.calculate(c4, c2), 0.01);
	}
}
