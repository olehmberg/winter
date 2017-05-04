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

package de.uni_mannheim.informatik.dws.winter.matching;

import java.util.LinkedList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import junit.framework.TestCase;

public class MatchingEvaluatorTest extends TestCase {

	public void testEvaluateMatching() {
		MatchingEvaluator<Movie, Attribute> evaluator = new MatchingEvaluator<>();
		List<Correspondence<Movie, Attribute>> correspondences = new LinkedList<>();
		MatchingGoldStandard gold = new MatchingGoldStandard();
		
		Movie movie1 = new Movie("movie1", "test");
		Movie movie2 = new Movie("movie2", "test");
		Movie movie3 = new Movie("movie3", "test2");
		Movie movie4 = new Movie("movie4", "test2");

		correspondences.add(new Correspondence<Movie, Attribute>(movie1, movie3, 1.0, null));
		correspondences.add(new Correspondence<Movie, Attribute>(movie1, movie2, 1.0, null));
		
		gold.addPositiveExample(new Pair<String, String>(movie3.getIdentifier(), movie1.getIdentifier()));
		gold.addPositiveExample(new Pair<String, String>(movie2.getIdentifier(), movie4.getIdentifier()));
		gold.addNegativeExample(new Pair<String, String>(movie1.getIdentifier(), movie2.getIdentifier()));
		gold.addNegativeExample(new Pair<String, String>(movie3.getIdentifier(), movie4.getIdentifier()));
		
		Performance p = evaluator.evaluateMatching(correspondences, gold);
		
		assertEquals(0.5, p.getPrecision());
		assertEquals(0.5, p.getRecall());
	}

}
