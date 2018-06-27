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

package de.uni_mannheim.informatik.dws.winter.model;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.XMLRecordReader;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Actor;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;
import junit.framework.TestCase;

public class DataSetTest extends TestCase {
	
	private static final Logger logger = LogManager.getLogger();

	public void testLoadFromXML() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		HashedDataSet<Movie, Attribute> ds = new HashedDataSet<>();
		
		File sourceFile = new File("usecase/movie/input/actors.xml");
		
		new MovieXMLReader().loadFromXML(sourceFile, "/movies/movie", ds);
		
		HashMap<String, Movie> movies = new HashMap<>();
		for(Movie movie : ds.get()) {
			logger.info(String.format("[%s] %s", movie.getIdentifier(), movie.getTitle()));
			movies.put(movie.getIdentifier(), movie);
		}
		
		assertEquals(151, ds.get().size());
		
/* example entry
	<movie>
		<id>actors_1</id>
		<title>7th Heaven</title>
		<actors>
			<actor>
				<name>Janet Gaynor</name>
				<birthday>1906-01-01</birthday>
				<birthplace>Pennsylvania</birthplace>
			</actor>
		</actors>
		<date>1929-01-01</date>
	</movie>
 */
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		        .appendPattern("yyyy-MM-dd")
		        .parseDefaulting(ChronoField.CLOCK_HOUR_OF_DAY, 0)
		        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		        .toFormatter(Locale.ENGLISH);
		
		Movie testMovie = movies.get("actors_1");
		assertEquals("7th Heaven", testMovie.getTitle());
		assertEquals(LocalDateTime.parse("1929-01-01", formatter), testMovie.getDate());
		Actor testActor = testMovie.getActors().get(0);
		assertEquals("Janet Gaynor", testActor.getName());
		assertEquals(LocalDateTime.parse("1906-01-01", formatter), testActor.getBirthday());
		assertEquals("Pennsylvania", testActor.getBirthplace());
		
		HashedDataSet<Record, Attribute> ds2 = new HashedDataSet<>();
		
		Map<String, Attribute> nodeMapping = new HashMap<>();
		nodeMapping.put("title", Movie.TITLE);
		nodeMapping.put("date", Movie.DATE);
		new XMLRecordReader("id", nodeMapping).loadFromXML(sourceFile, "/movies/movie", ds2);
		
		assertEquals(151, ds2.get().size());
		
		for(Record m : ds2.get()) {
			String id = m.getIdentifier();
			
			Movie movie = movies.get(id);
			
			assertEquals(movie.getTitle(), m.getValue(Movie.TITLE));
			LocalDateTime dt = LocalDateTime.parse(m.getValue(Movie.DATE), formatter);
			assertEquals(movie.getDate(), dt);
			
		}
	}

}
