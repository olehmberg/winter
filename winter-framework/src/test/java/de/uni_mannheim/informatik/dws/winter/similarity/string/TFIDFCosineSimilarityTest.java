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

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.blocking.DefaultAttributeValueGenerator;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.generators.DefaultTokenGenerator;
import junit.framework.TestCase;

/**
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 *
 */
public class TFIDFCosineSimilarityTest extends TestCase {

	/**
	 * Test method for {@link TokenizingJaccardSimilarity#calculate(String, String)}.
	 */
	public void testCalculateStringString() {

		HashedDataSet<Record, Attribute> ds1 = new HashedDataSet<>();
		HashedDataSet<Record, Attribute> ds2 = new HashedDataSet<>();

		Attribute a1 = new Attribute("A1");
		Attribute a2 = new Attribute("A2");

		Record r1 = new Record("r1");
		Record r2 = new Record("r2");
		Record r3 = new Record("r3");
		Record r4 = new Record("r4");
		Record r5 = new Record("r5");
		Record r6 = new Record("r6");

		r1.setValue(a1, "a");
		r1.setValue(a2, "b c d");

		r2.setValue(a1, "bc");
		r2.setValue(a2, "de fg");

		r3.setValue(a1, "a b");
		r3.setValue(a2, "d c");

		r4.setValue(a1, "a b c");
		r4.setValue(a2, "de");

		r5.setValue(a1, "");
		r5.setValue(a2, "");

		r6.setValue(a1, "");
		r6.setValue(a2, "");

		ds1.add(r1);
		ds1.add(r2);
		ds1.add(r5);
		ds2.add(r3);
		ds2.add(r4);
		ds2.add(r6);

		ds1.addAttribute(a1);
		ds1.addAttribute(a2);
		ds2.addAttribute(a1);
		ds2.addAttribute(a2);

		String s1 = r1.getValue(a1) + " " + r1.getValue(a2);
		String s2 = r2.getValue(a1) + " " + r2.getValue(a2);
		String s3 = r3.getValue(a1) + " " + r3.getValue(a2);
		String s4 = r4.getValue(a1) + " " + r4.getValue(a2);
		String s5 = r5.getValue(a1) + r5.getValue(a2);
		String s6 = r6.getValue(a1) + r6.getValue(a2);

		TFIDFCosineSimilarity<Record, Attribute, Attribute> sim =
				new TFIDFCosineSimilarity<>(ds1, ds2, new DefaultTokenGenerator(ds1.getSchema()));

		assertTrue(sim.calculate(s1, s1) > 0.99);
		assertTrue(sim.calculate(s2, s2) > 0.99);
		assertTrue(sim.calculate(s3, s3) > 0.99);
		assertTrue(sim.calculate(s4, s4) > 0.99);
		assertEquals(0.0, sim.calculate(s5, s5));
		assertEquals(0.0, sim.calculate(s6, s6));

		assertTrue(sim.calculate(s1, s3) > 0.99);
		assertTrue(sim.calculate(s3, s1) > 0.99);

		assertEquals(0.0, sim.calculate(s2, s3));
		assertEquals(0.0, sim.calculate(s3, s2));

		assertTrue(sim.calculate(s1, s4) > 0.5);
		assertTrue(sim.calculate(s4, s1) > 0.5);

		assertEquals(0.0, sim.calculate(s3, s5));
		assertEquals(0.0, sim.calculate(s1, s6));
	}

}
