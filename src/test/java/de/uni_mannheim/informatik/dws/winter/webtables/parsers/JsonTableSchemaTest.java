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
package de.uni_mannheim.informatik.dws.winter.webtables.parsers;

import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class JsonTableSchemaTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema#transposeRelation()}.
	 */
	public void testTransposeRelation() {
		JsonTableSchema s = new JsonTableSchema();
		
		String[][] original = new String[][] {
			new String[] { "r0c0 -> r0c0", "r1c0 -> r0c1", "r2c0 -> r0c3" },
			new String[] { "r0c1 -> r1c0", "r1c1 -> r1c1", "r2c1 -> r1c3"}
		};
		
		s.setRelation(original);
		s.transposeRelation();
		
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 2; col++) {
				System.out.print(original[col][row] + " | ");
			}
			System.out.println();
		}
		System.out.println();
		for(int row = 0; row < 2; row++) {
			for(int col = 0; col < 3; col++) {
				System.out.print(s.getRelation()[col][row] + " | ");
			}
			System.out.println();
		}
	}

}
