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

package de.uni_mannheim.informatik.dws.winter.matching.blocking;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.SortedNeighbourhoodBlocker;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieBlockingKeyByYearGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;

public class SortedNeighbourhoodBlockerTest extends TestCase {
	
	private static final Logger logger = LogManager.getLogger();
	
	private DataSet<Movie, Attribute> generateDS1() {
		DataSet<Movie, Attribute> ds = new HashedDataSet<>();
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		        .appendPattern("yyyy-MM-dd")
		        .parseDefaulting(ChronoField.CLOCK_HOUR_OF_DAY, 0)
		        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		        .toFormatter(Locale.ENGLISH);
		
		Movie m1 = new Movie("1", "DS1");
		m1.setDate(LocalDateTime.parse("1980-10-10", formatter));
		ds.add(m1);
		Movie m2 = new Movie("2", "DS1");
		m2.setDate(LocalDateTime.parse("1990-10-10", formatter));
		ds.add(m2);
		Movie m3 = new Movie("3", "DS1");
		m3.setDate(LocalDateTime.parse("1991-10-10", formatter));
		ds.add(m3);
		return ds;
	}

	private DataSet<Movie, Attribute> generateDS2() {
		DataSet<Movie, Attribute> ds = new HashedDataSet<>();
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		        .appendPattern("yyyy-MM-dd")
		        .parseDefaulting(ChronoField.CLOCK_HOUR_OF_DAY, 0)
		        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		        .toFormatter(Locale.ENGLISH);
		
		Movie m1 = new Movie("4", "DS2");
		m1.setDate(LocalDateTime.parse("1983-10-10", formatter));
		ds.add(m1);
		Movie m2 = new Movie("5", "DS2");
		m2.setDate(LocalDateTime.parse("1984-10-10", formatter));
		ds.add(m2);
		Movie m3 = new Movie("6", "DS2");
		m3.setDate(LocalDateTime.parse("1995-10-10", formatter));
		ds.add(m3);
		return ds;
	}

	public void testGeneratePairs() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		DataSet<Movie, Attribute> ds = generateDS1();

		DataSet<Movie, Attribute> ds2 = generateDS2();

		SortedNeighbourhoodBlocker<Movie, Attribute, Attribute> blocker = new SortedNeighbourhoodBlocker<>(
				new MovieBlockingKeyByYearGenerator(), 3);

		MatchingGoldStandard gs = new MatchingGoldStandard();
		gs.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_academy_awards_2_actors.csv"));
		
		Processable<Correspondence<Movie, Attribute>> pairs = blocker.runBlocking(ds, ds2, null);

		logger.info("Pairs: " + pairs.size());
		logger.info("Reduction Rate: " + blocker.getReductionRatio());

		for (Correspondence<Movie, Attribute> p : pairs.get()) {
			logger.info(p.getFirstRecord().getIdentifier() + " | "
					+ p.getSecondRecord().getIdentifier());
		}
		assertEquals(4, pairs.size());
	}

}
