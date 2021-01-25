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
package de.uni_mannheim.informatik.dws.winter.similarity.string;

import junit.framework.TestCase;

/**
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 *
 */
public class JaccardOnNGramsSimilarityTest extends TestCase {

	/**
	 * Test method for {@link JaccardOnNGramsSimilarity#calculate(String, String)}.
	 */
	public void testCalculateStringString() {
		String s1 = "ab";
		String s2 = "abcd";
		String s3 = "cd";

		JaccardOnNGramsSimilarity sim = new JaccardOnNGramsSimilarity(2);

		assertEquals(1.0, sim.calculate(s1, s1));
		assertEquals(1.0, sim.calculate(s2, s2));

		assertEquals(0.5, sim.calculate(s1, s2));
		assertEquals(0.5, sim.calculate(s2, s1));
		
		assertEquals(0.2, sim.calculate(s2, s3));
		assertEquals(0.2, sim.calculate(s3, s2));
		
		assertEquals(0.0, sim.calculate(s1, s3));
		assertEquals(0.0, sim.calculate(s3, s1));
	}

}
