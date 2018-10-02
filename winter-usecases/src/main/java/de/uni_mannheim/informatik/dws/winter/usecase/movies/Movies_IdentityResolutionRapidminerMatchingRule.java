/** 
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.dws.winter.usecase.movies;

import java.io.File;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.WekaMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieBlockingKeyByDecadeGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDateComparator10Years;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDateComparator2Years;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDirectorComparatorJaccard;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDirectorComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDirectorComparatorLowerCaseJaccard;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorEqual;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorJaccard;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * Class containing the standard setup to perform a identity resolution task by
 * learning a matching rule and reading input data from the movie usecase.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class Movies_IdentityResolutionRapidminerMatchingRule {
	
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

	private static final Logger logger = WinterLogManager.activateLogger("default");
	
	public static void main(String[] args) throws Exception {
		// loading data
		HashedDataSet<Movie, Attribute> dataAcademyAwards = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/academy_awards.xml"), "/movies/movie",
				dataAcademyAwards);
		HashedDataSet<Movie, Attribute> dataActors = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/actors.xml"), "/movies/movie", dataActors);

		// load the gold standard (test set)
		// load the gold standard (training set)
		MatchingGoldStandard gsTraining = new MatchingGoldStandard();
		gsTraining.loadFromCSVFile(new File("usecase/movie/goldstandard/gs_academy_awards_2_actors_training.csv"));

		// create a matching rule without(!) classifier, options + Feature

		WekaMatchingRule<Movie, Attribute> matchingRule = new WekaMatchingRule<>(0.5);
		// Collect debug results
		matchingRule.activateDebugReport("usecase/movie/output/debugResultsMatchingRule.csv", 1000, gsTraining);

		// add comparators
		matchingRule.addComparator(new MovieTitleComparatorEqual());
		matchingRule.addComparator(new MovieDateComparator2Years());
		matchingRule.addComparator(new MovieDateComparator10Years());
		matchingRule.addComparator(new MovieDirectorComparatorJaccard());
		matchingRule.addComparator(new MovieDirectorComparatorLevenshtein());
		matchingRule.addComparator(new MovieDirectorComparatorLowerCaseJaccard());
		matchingRule.addComparator(new MovieTitleComparatorLevenshtein());
		matchingRule.addComparator(new MovieTitleComparatorJaccard());

		// create a blocker (blocking strategy)
		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<Movie, Attribute>(new MovieBlockingKeyByDecadeGenerator());
		//Write debug results to file:
		blocker.collectBlockSizeData("usecase/movie/output/debugResultsBlocking.csv", 100);
		
		// Export training data
		matchingRule.exportTrainingData(dataAcademyAwards, dataActors, gsTraining, new File("usecase/movie/Rapidminer/data/academy_awards_2_actors_features.csv"));
		
		// import matching rule model
		matchingRule.readModel(new File("usecase/movie/Rapidminer/models/matchingModel.pmml"));

		// Initialize Matching Engine
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Movie, Attribute>> correspondences = engine.runIdentityResolution(dataAcademyAwards,
				dataActors, null, matchingRule, blocker);
		
		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(
				new File("usecase/movie/output/academy_awards_2_actors_correspondences.csv"), correspondences);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File("usecase/movie/goldstandard/gs_academy_awards_2_actors_test.csv"));

		// evaluate your result
		MatchingEvaluator<Movie, Attribute> evaluator = new MatchingEvaluator<Movie, Attribute>();
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gsTest);
		
		// Export test data
		matchingRule.exportTrainingData(dataAcademyAwards, dataActors, gsTest, new File("usecase/movie/Rapidminer/data/academy_awards_2_actors_features_test.csv"));
		
		
		//evaluate learned classifier
		logger.info(matchingRule.getClassifier().toString());
				
		// print the evaluation result
		logger.info("Academy Awards <-> Actors");
		logger.info(String.format("Precision: %.4f", perfTest.getPrecision()));
		logger.info(String.format("Recall: %.4f", perfTest.getRecall()));
		logger.info(String.format("F1: %.4f", perfTest.getF1()));
	}
}
