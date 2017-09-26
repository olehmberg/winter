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

package de.uni_mannheim.informatik.dws.winter.similarity.numeric;

import de.uni_mannheim.informatik.dws.winter.similarity.numeric.PercentageSimilarity;
import junit.framework.TestCase;

public class PercentageSimilarityTest extends TestCase {

	public void testCalculateDoubleDouble() {
		PercentageSimilarity sim = new PercentageSimilarity(0.33);
		
		assertEquals(0.394, Math.round(sim.calculate(2000.0, 2500.0) * 1000.0) / 1000.0);
		assertEquals(0.992, Math.round(sim.calculate(200000.0, 200500.0) * 1000.0) / 1000.0);
	}

}
