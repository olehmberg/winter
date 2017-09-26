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
package de.uni_mannheim.informatik.dws.winter.matching.rules;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorEqual;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class MaxScoreMatchingRuleTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.matching.rules.MaxScoreMatchingRule#apply(de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.processing.Processable)}.
	 * @throws Exception 
	 */
	public void testApply() throws Exception {
		
		Attribute a1 = new Attribute("a1");
		Attribute a2 = new Attribute("a2");
		
		Record r1 = new Record("r1");
		r1.setValue(a1, "1");
		r1.setValue(a2, "a");
		Record r2 = new Record("r2");
		r2.setValue(a1, "1");
		r2.setValue(a2, "b");
		Record r3 = new Record("r3");
		r3.setValue(a1, "2");
		r3.setValue(a2, "a");
		
		LinearCombinationMatchingRule<Record, Attribute> rule1 = new LinearCombinationMatchingRule<>(1.0);
		rule1.addComparator(new RecordComparatorEqual(a1, a1), 1.0);
		
		LinearCombinationMatchingRule<Record, Attribute> rule2 = new LinearCombinationMatchingRule<>(1.0);
		rule2.addComparator(new RecordComparatorEqual(a2, a2), 1.0);
		
		MaxScoreMatchingRule<Record, Attribute> maxRule = new MaxScoreMatchingRule<>(1.0);
		maxRule.addMatchingRule(rule1);
		maxRule.addMatchingRule(rule2);
		
		Correspondence<Record, Attribute> cor = maxRule.apply(r1, r2, null);
		assertNotNull(cor);
		assertEquals(1.0, cor.getSimilarityScore());
		
		cor = maxRule.apply(r1, r3, null);
		assertNotNull(cor);
		assertEquals(1.0, cor.getSimilarityScore());
		
		cor = maxRule.apply(r2, r3, null);
		assertNull(cor);
	}

}
