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

import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.*;
import junit.framework.TestCase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;

public class LinearCombinationMatchingRuleTest extends TestCase {

	public void testApply() throws Exception {
		Movie movie1 = new Movie("movie1", "test");
		Movie movie2 = new Movie("movie2", "test");
		Movie movie3 = new Movie("movie3", "test");
		Movie movie4 = new Movie("movie4", "test");
		
		movie1.setTitle("Star Wars IV");
		movie2.setTitle("Star Wars V");
		movie3.setTitle("Star Wars IV");
		movie4.setTitle("Star Wars IV");
		
		movie1.setDirector("George Lucas");
		movie2.setDirector("Irvin Kershner");
		movie3.setDirector("Irvin Kershner");
		
		
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		        .appendPattern("yyyy-MM-dd")
		        .parseDefaulting(ChronoField.CLOCK_HOUR_OF_DAY, 0)
		        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		        .toFormatter(Locale.ENGLISH);
		movie1.setDate(LocalDateTime.parse("1977-05-25", formatter));
		movie2.setDate(LocalDateTime.parse("1980-05-21", formatter));
		movie3.setDate(LocalDateTime.parse("1977-05-25", formatter));
		movie4.setDate(LocalDateTime.parse("1977-05-25", formatter));
		
		Processable<Correspondence<Movie, Attribute>> correspondences;
		
		LinearCombinationMatchingRule<Movie, Attribute> rule1 = new LinearCombinationMatchingRule<>(0.0, 1.0);
		rule1.addComparator(new MovieTitleComparatorLevenshtein(), 1.0);
//		assertNotNull(rule1.apply(movie1, movie3, null));
		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie1, movie3, 1.0, null));
		correspondences = correspondences.map(rule1);
		assertEquals(1, correspondences.size());
//		assertNull(rule1.apply(movie1, movie2, null));
		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie1, movie2, 1.0, null));
		correspondences = correspondences.map(rule1);
		assertEquals(0, correspondences.size());
		
		LinearCombinationMatchingRule<Movie, Attribute> rule2 = new LinearCombinationMatchingRule<>(0.0, 0.9);
		rule2.addComparator(new MovieTitleComparatorLevenshtein(), 0.1);
		rule2.addComparator(new MovieDirectorComparatorLevenshtein(), 0.9);
//		assertNotNull(rule2.apply(movie2, movie3, null));
		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie2, movie3, 1.0, null));
		correspondences = correspondences.map(rule2);
		assertEquals(1, correspondences.size());
//		assertNull(rule2.apply(movie1, movie2, null));
		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie1, movie2, 1.0, null));
		correspondences = correspondences.map(rule2);
		assertEquals(0, correspondences.size());
		
		LinearCombinationMatchingRule<Movie, Attribute> rule3 = new LinearCombinationMatchingRule<>(0.0, 0.8);
		rule3.addComparator(new MovieTitleComparatorLevenshtein(), 0.1);
		rule3.addComparator(new MovieDirectorComparatorLevenshtein(), 0.1);
		rule3.addComparator(new MovieDateComparator10Years(), 0.8);
//		assertNotNull(rule3.apply(movie1, movie3, null));
		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie1, movie3, 1.0, null));
		correspondences = correspondences.map(rule3);
		assertEquals(1, correspondences.size());
//		assertNull(rule3.apply(movie2, movie3, null));
		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie2, movie3, 1.0, null));
		correspondences = correspondences.map(rule3);
		assertEquals(0, correspondences.size());

		LinearCombinationMatchingRule<Movie, Attribute> rule4 = new LinearCombinationMatchingRule<>(0.0, 0.8);
		rule4.activateDebugReport("test/output.csv", 1000);
		rule4.addComparator(new MovieTitleComparatorLevenshtein(), 0.1);
		rule4.addComparator(new MovieDateComparator10Years(), 0.8);

		MovieDirectorComparatorLevenshtein movieDirectorComparatorLevenshtein = new MovieDirectorComparatorLevenshtein();
		rule4.addComparator(movieDirectorComparatorLevenshtein, 0.1);

		MovieDirectorComparatorMissingValue movieDirectorComparatorMissingValue = new MovieDirectorComparatorMissingValue(0.15);
		movieDirectorComparatorMissingValue.addPenalisedComparator(movieDirectorComparatorLevenshtein);
		rule4.addComparator(movieDirectorComparatorMissingValue, 0.0);


//		assertNotNull(rule3.apply(movie1, movie3, null));
		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie1, movie3, 1.0, null));
		correspondences = correspondences.map(rule4);
		assertEquals(1, correspondences.size());
//		assertNull(rule3.apply(movie2, movie3, null));
		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie2, movie3, 1.0, null));
		correspondences = correspondences.map(rule4);
		assertEquals(0, correspondences.size());

		correspondences = new ProcessableCollection<>();
		correspondences.add(new Correspondence<>(movie1, movie4, 1.0, null));
		correspondences = correspondences.map(rule4);
		assertEquals(1, correspondences.size());
		assertEquals(0.85, correspondences.get().iterator().next().getSimilarityScore());

		// Check debug log entries
		FusibleHashedDataSet<Record, Attribute> comperatorLog = rule4.getComparatorLog();
		assertEquals(3, comperatorLog.size());
		Attribute totalSimilarity = comperatorLog.getAttribute("TotalSimilarity");
		Attribute missingValueSimilarity = comperatorLog.getAttribute("[3] MovieDirectorComparatorMissingValue similarity");

		Record movie1Movie4 = comperatorLog.getRecord("movie1-movie4");
		assertEquals("0.85", movie1Movie4.getValue(totalSimilarity));
		assertEquals("1.0", movie1Movie4.getValue(missingValueSimilarity));

		Record movie1Movie3 = comperatorLog.getRecord("movie1-movie3");
		assertEquals("0.0", movie1Movie3.getValue(missingValueSimilarity));
	}

}
