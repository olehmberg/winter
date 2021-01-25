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
public class LevenshteinEditDistanceTest extends TestCase {

	/**
	 * Test method for {@link LevenshteinEditDistance#calculate(String, String)}.
	 */
	public void testCalculateStringString() {
		String s1 = "a b";
		String s2 = "a b c d";
		String s3 = "c d e f";

		LevenshteinEditDistance sim = new LevenshteinEditDistance();

		assertEquals(0.0, sim.calculate(s1, s1));
		assertEquals(0.0, sim.calculate(s2, s2));
		assertEquals(0.0, sim.calculate(s3, s3));

		assertEquals(4.0, sim.calculate(s1, s2));
		assertEquals(4.0, sim.calculate(s2, s1));

		assertEquals(4.0, sim.calculate(s2, s3));
		assertEquals(4.0, sim.calculate(s3, s2));

		assertEquals(6.0, sim.calculate(s1, s3));
		assertEquals(6.0, sim.calculate(s3, s1));
	}

}
