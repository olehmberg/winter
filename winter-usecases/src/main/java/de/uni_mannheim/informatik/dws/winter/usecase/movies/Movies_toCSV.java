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
package de.uni_mannheim.informatik.dws.winter.usecase.movies;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;

/**
 * Converts the XML datasets to CSV.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Movies_toCSV {

	public static void main(String[] args) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		// loading data
		HashedDataSet<Movie, Attribute> data1 = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/movie_list.xml"), "/movies/movie", data1);
		HashedDataSet<Movie, Attribute> data2 = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/greatest_scifi.xml"), "/movies/movie", data2);
		
		// write data
		new MovieCSVFormatter().writeCSV(new File("usecase/movie/input/movie_list.csv"), data1, null);
		new MovieCSVFormatter().writeCSV(new File("usecase/movie/input/greatest_scifi.csv"), data2, null);
	}
	
}
