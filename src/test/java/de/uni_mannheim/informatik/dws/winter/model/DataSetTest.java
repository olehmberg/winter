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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.joda.time.DateTime;
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

	public void testLoadFromXML() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		HashedDataSet<Movie, Attribute> ds = new HashedDataSet<>();
		
		File sourceFile = new File("usecase/movie/input/actors.xml");
		
		new MovieXMLReader().loadFromXML(sourceFile, "/movies/movie", ds);
		
		HashMap<String, Movie> movies = new HashMap<>();
		for(Movie movie : ds.get()) {
			System.out.println(String.format("[%s] %s", movie.getIdentifier(), movie.getTitle()));
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
	
		Movie testMovie = movies.get("actors_1");
		assertEquals("7th Heaven", testMovie.getTitle());
		assertEquals(DateTime.parse("1929-01-01"), testMovie.getDate());
		Actor testActor = testMovie.getActors().get(0);
		assertEquals("Janet Gaynor", testActor.getName());
		assertEquals(DateTime.parse("1906-01-01"), testActor.getBirthday());
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
			DateTime dt = DateTime.parse(m.getValue(Movie.DATE));
			assertEquals(movie.getDate(), dt);
			
		}
	}

}
