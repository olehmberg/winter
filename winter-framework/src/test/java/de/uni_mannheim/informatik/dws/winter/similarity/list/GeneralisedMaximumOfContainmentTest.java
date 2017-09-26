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
package de.uni_mannheim.informatik.dws.winter.similarity.list;

import java.util.Collection;

import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class GeneralisedMaximumOfContainmentTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.similarity.list.ComplexSetSimilarity#calculate(java.util.Collection, java.util.Collection)}.
	 */
	public void testCalculateCollectionOfTCollectionOfT() {
		
		Collection<String> c1 = Q.toList("a1", "bbb2", "ccccc3", "ddddddd4");
		Collection<String> c2 = Q.toList("a1");
		Collection<String> c3 = Q.toList("aY");
		Collection<String> c4 = Q.toList("cccccX", "dddddddX", "eeeeeeeeeX", "fffffffffffX");
		
		GeneralisedMaximumOfContainment<String> sim = new GeneralisedMaximumOfContainment<>(new LevenshteinSimilarity(), 0.5);
		
		assertEquals(1.0, sim.calculate(c1, c2));
		assertEquals(1.0, sim.calculate(c2, c1));
		
		assertEquals(0.5, sim.calculate(c1, c3));
		assertEquals(0.5, sim.calculate(c3, c1));
		
		assertEquals(0.0, sim.calculate(c2, c4));
		assertEquals(0.0, sim.calculate(c4, c2));
		
	}

}
