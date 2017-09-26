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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.StaticBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;

public class StandardBlockerTest extends TestCase {

	public void testGeneratePairs() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		HashedDataSet<Movie, Attribute> ds = new HashedDataSet<>();
		File sourceFile1 = new File("usecase/movie/input/actors.xml");
		new MovieXMLReader().loadFromXML(sourceFile1, "/movies/movie", ds);

		HashedDataSet<Movie, Attribute> ds2 = new HashedDataSet<>();
		File sourceFile2 = new File("usecase/movie/input/academy_awards.xml");
		new MovieXMLReader().loadFromXML(sourceFile2, "/movies/movie", ds2);

		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<>(
				new StaticBlockingKeyGenerator<Movie, Attribute>());

		MatchingGoldStandard gs = new MatchingGoldStandard();
		gs.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_academy_awards_2_actors.csv"));

		Processable<Correspondence<Movie, Attribute>> pairs = blocker.runBlocking(ds, ds2, null);
		
		List<Correspondence<Movie, Attribute>> correspondences = new ArrayList<>(
				pairs.size());

		// transform pairs into correspondences
		for (Correspondence<Movie, Attribute> p : pairs.get()) {
			correspondences.add(new Correspondence<Movie, Attribute>(p.getFirstRecord(), p
					.getSecondRecord(), 1.0, p.getCausalCorrespondences()));
		}

		// check if all examples from the gold standard were in the pairs
		MatchingEvaluator<Movie, Attribute> eval = new MatchingEvaluator<>(true);

		Performance perf = eval.evaluateMatching(correspondences, gs);

		assertEquals(1.0, perf.getRecall());
	}

}
