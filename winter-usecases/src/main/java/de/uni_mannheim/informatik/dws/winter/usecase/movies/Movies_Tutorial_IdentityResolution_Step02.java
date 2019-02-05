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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.NoBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.StaticBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.FeatureVectorDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.RecordCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieBlockingKeyByDecadeGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieBlockingKeyByTitleGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieBlockingKeyByYearGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDateComparator10Years;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDateComparator2Years;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDirectorComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorEqual;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorJaccard;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * Class containing the standard setup to perform a identity resolution task,
 * reading input data from the movie usecase.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
public class Movies_Tutorial_IdentityResolution_Step02 {
	
	/*
	 * Logging Options:
	 * 		default: 	level INFO	- console
	 * 		trace:		level TRACE     - console
	 * 		infoFile:	level INFO	- console/file
	 * 		traceFile:	level TRACE	- console/file
	 *  
	 * To set the log level to trace and write the log to winter.log and console, 
	 * activate the "traceFile" logger as follows:
	 *     private static final Logger logger = WinterLogManager.activateLogger("traceFile");
	 *
	 */

	private static final Logger logger = WinterLogManager.activateLogger("trace");

	public static void main(String[] args) throws Exception {
		// loading data
		HashedDataSet<Movie, Attribute> dataAcademyAwards = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/academy_awards.xml"), "/movies/movie", dataAcademyAwards);
		HashedDataSet<Movie, Attribute> dataActors = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/actors.xml"), "/movies/movie", dataActors);
				
		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_academy_awards_2_actors_test.csv"));

		// create a matching rule
		LinearCombinationMatchingRule<Movie, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.7);
		
		//Write debug results to file
		matchingRule.activateDebugReport("usecase/movie/output/debugResultsMatchingRule.csv", -1);

		// add comparators
		matchingRule.addComparator(new MovieDateComparator2Years(), 0.5);
		matchingRule.addComparator(new MovieTitleComparatorJaccard(), 0.5);
		
		// Initialize Matching Engine
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();

		// create a blocker (blocking strategy)
		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<Movie, Attribute>(new MovieBlockingKeyByTitleGenerator());

		// Execute the matching
		Processable<Correspondence<Movie, Attribute>> correspondences = engine.runIdentityResolution(
				dataAcademyAwards, dataActors, null, matchingRule, blocker);
		
		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/movie/output/academy_awards_2_actors_correspondences.csv"), correspondences);

		// evaluate your result
		MatchingEvaluator<Movie, Attribute> evaluator = new MatchingEvaluator<Movie, Attribute>();
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(),
				gsTest);

		// print the evaluation result
		logger.info("Academy Awards <-> Actors");
		logger.info(String.format("Precision: %.4f", perfTest.getPrecision()));
		logger.info(String.format("Recall: %.4f", perfTest.getRecall()));
		logger.info(String.format("F1: %.4f", perfTest.getF1()));
	}



}
