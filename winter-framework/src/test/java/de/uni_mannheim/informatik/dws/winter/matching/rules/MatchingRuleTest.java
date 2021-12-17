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

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import junit.framework.TestCase;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-manheim.de)
 *
 */
public class MatchingRuleTest extends TestCase {

	/**
	 * Test method for {@link MatchingRule#activateDebugReport(String, int)}.
	 *
	 */
	private static final Logger logger = WinterLogManager.getLogger();

	public void testActivateDebugReport() {

		// create a matching rule
		LinearCombinationMatchingRule<Record, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.7);

		// Test activation with valid path
		matchingRule.activateDebugReport("path/to/output/debugResultsMatchingRule.csv", 1000);

		// Test activation with invalid path
		matchingRule.activateDebugReport("usecase/movie/output/debugResultsMatchingRule.no_csv", 1000);
		

	}

}
