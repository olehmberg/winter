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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
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
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieBlockingKeyByYearGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDateComparator10Years;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDirectorComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorEqual;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;

/**
 * Class containing the standard setup to perform a identity resolution task,
 * reading input data from the movie usecase.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
public class Movies_IdentityResolution_Main {
	
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws Exception {
		// loading data
		HashedDataSet<Movie, Attribute> dataAcademyAwards = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/academy_awards.xml"), "/movies/movie", dataAcademyAwards);
		HashedDataSet<Movie, Attribute> dataActors = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/actors.xml"), "/movies/movie", dataActors);

		// create a matching rule
		LinearCombinationMatchingRule<Movie, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.7);
		
		// add comparators
//		matchingRule.addComparator((m1,  m2, c) -> new TokenizingJaccardSimilarity().calculate(m1.getTitle(), m2.getTitle()) , 0.8);
//		matchingRule.addComparator((m1, m2, c) -> new YearSimilarity(10).calculate(m1.getDate(), m2.getDate()), 0.2);

		matchingRule.addComparator(new MovieDirectorComparatorLevenshtein(), 0.2);
		matchingRule.addComparator(new MovieTitleComparatorLevenshtein(), 0.8);
		
		matchingRule.setFilePathResults("usecase/movie/output/resultsMatchingRule.tsv");
		matchingRule.resetResultCounter();
		
		// create a blocker (blocking strategy)
		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<Movie, Attribute>(new MovieBlockingKeyByDecadeGenerator());

		// Initialize Matching Engine
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Movie, Attribute>> correspondences = engine.runIdentityResolution(
				dataAcademyAwards, dataActors, null, matchingRule,
				blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/movie/output/academy_awards_2_actors_correspondences.csv"), correspondences);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_academy_awards_2_actors_v2.csv"));

		// evaluate your result
		MatchingEvaluator<Movie, Attribute> evaluator = new MatchingEvaluator<Movie, Attribute>(true);
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(),
				gsTest);

		// print the evaluation result
		logger.info("Academy Awards <-> Actors");
		logger.info(String.format("Precision: %.4f", perfTest.getPrecision()));
		logger.info(String.format("Recall: %.4f", perfTest.getRecall()));
		logger.info(String.format("F1: %.4f", perfTest.getF1()));
	}

	public static void createDatasetToTrain() throws Exception {
		// loading data
		HashedDataSet<Movie, Attribute> dataAcademyAwards = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/academy_awards.xml"), "/movies/movie", dataAcademyAwards);
		HashedDataSet<Movie, Attribute> dataActors = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/actors.xml"), "/movies/movie", dataActors);

		// load the gold standard (test set)
		// load the gold standard (training set)
		MatchingGoldStandard gsTraining = new MatchingGoldStandard();
		gsTraining.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_academy_awards_2_actors.csv"));

		// create a matching rule
		LinearCombinationMatchingRule<Movie, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.0);
		// add comparators
		matchingRule.addComparator(new MovieTitleComparatorLevenshtein(), 0.8);
		matchingRule.addComparator(new MovieDateComparator10Years(), 0.2);

		// create the data set for learning a matching rule (use this file in
		// RapidMiner)
		RuleLearner<Movie, Attribute> learner = new RuleLearner<>();
		FeatureVectorDataSet features = learner.generateTrainingDataForLearning(dataAcademyAwards, dataActors,
						gsTraining, matchingRule, null);
		new RecordCSVFormatter().writeCSV(
				new File("usecase/movie/output/optimisation/academy_awards_2_actors_features.csv"), features);
	}

	public static void firstMatching() throws Exception {

		// loading data
		HashedDataSet<Movie, Attribute> dataAcademyAwards = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/academy_awards.xml"), "/movies/movie", dataAcademyAwards);
		HashedDataSet<Movie, Attribute> dataActors = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/actors.xml"), "/movies/movie", dataActors);

		// create a matching rule
		LinearCombinationMatchingRule<Movie, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.0);
		// add comparators
		matchingRule.addComparator(new MovieTitleComparatorEqual(), 1);
		matchingRule.addComparator(new MovieDateComparator10Years(), 1);
		// run normalization
		matchingRule.normalizeWeights();

		// create a blocker (blocking strategy)
		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<>(
				new StaticBlockingKeyGenerator<Movie, Attribute>());

		// Initialize Matching Engine
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Movie, Attribute>> correspondences = engine.runIdentityResolution(
				dataAcademyAwards, dataActors, null, matchingRule,
				blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/movie/output/academy_awards_2_actors_correspondences.csv"), correspondences);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_academy_awards_2_actors_test.csv"));

		// evaluate your result
		MatchingEvaluator<Movie, Attribute> evaluator = new MatchingEvaluator<Movie, Attribute>(true);
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(),
				gsTest);

		// print the evaluation result
		logger.info("Academy Awards <-> Actors");
		logger.info(String.format("Precision: %.4f", perfTest.getPrecision()));
		logger.info(String.format("Recall: %.4f", perfTest.getRecall()));
		logger.info(String.format("F1: %.4f", perfTest.getF1()));
	}

	public static void runWhole() throws Exception {
		// define the matching rule
		LinearCombinationMatchingRule<Movie, Attribute> rule = new LinearCombinationMatchingRule<>(
				-1.497, 0.5);
		rule.addComparator(new MovieTitleComparatorLevenshtein(), 1.849);
		rule.addComparator(new MovieDateComparator10Years(), 0.822);

		// create the matching engine
		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<>(
				new MovieBlockingKeyByYearGenerator());
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();

		// load the data sets
		HashedDataSet<Movie, Attribute> ds1 = new HashedDataSet<>();
		HashedDataSet<Movie, Attribute> ds2 = new HashedDataSet<>();
		HashedDataSet<Movie, Attribute> ds3 = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/academy_awards.xml"), "/movies/movie", ds1);
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/actors.xml"), "/movies/movie", ds2);
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/golden_globes.xml"), "/movies/movie", ds3);

		// run the matching
		Processable<Correspondence<Movie, Attribute>> correspondences = engine.runIdentityResolution(ds1,
				ds2, null, rule, blocker);
		Processable<Correspondence<Movie, Attribute>> correspondences2 = engine.runIdentityResolution(ds2,
				ds3, null, rule, blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/movie/output/academy_awards_2_actors_correspondences.csv"), correspondences);
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/movie/output/actors_2_golden_globes_correspondences.csv"), correspondences2);

		printCorrespondences(new ArrayList<>(correspondences2.get()));

		// load the gold standard (training set)
		MatchingGoldStandard gsTraining = new MatchingGoldStandard();
		gsTraining.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_academy_awards_2_actors.csv"));

		// create the data set for learning a matching rule (use this file in
		// RapidMiner)
		RuleLearner<Movie, Attribute> learner = new RuleLearner<>();
		 FeatureVectorDataSet features = learner.generateTrainingDataForLearning(ds1, ds2, gsTraining, rule, null);
		new RecordCSVFormatter().writeCSV(new File("usecase/movie/output/optimisation/academy_awards_2_actors_features.csv"), features);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_academy_awards_2_actors_test.csv"));
		MatchingGoldStandard gs2 = new MatchingGoldStandard();
		gs2.loadFromCSVFile(new File(
				"usecase/movie/goldstandard/gs_actors_2_golden_globes.csv"));

		// evaluate the result
		MatchingEvaluator<Movie, Attribute> evaluator = new MatchingEvaluator<>(true);
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(),
				gsTest);
		Performance perf2 = evaluator.evaluateMatching(correspondences2.get(), gs2);

		// print the evaluation result
		logger.info("Academy Awards <-> Actors");
		logger.info(String.format("Precision: %.4f", perfTest.getPrecision()));
		logger.info(String.format("Recall: %.4f", perfTest.getRecall()));
		logger.info(String.format("F1: %.4f", perfTest.getF1()));

		logger.info("Actors <-> Golden Globes");
		logger.info(String.format("Precision: %.4f", perf2.getPrecision()));
		logger.info(String.format("Recall: %.4f", perf2.getRecall()));
		logger.info(String.format("F1: %.4f", perf2.getF1()));
	}

	private static void printCorrespondences(
			List<Correspondence<Movie, Attribute>> correspondences) {
		// sort the correspondences
		Collections.sort(correspondences,
				new Comparator<Correspondence<Movie, Attribute>>() {

					@Override
					public int compare(Correspondence<Movie, Attribute> o1,
							Correspondence<Movie, Attribute> o2) {
						int score = Double.compare(o1.getSimilarityScore(),
								o2.getSimilarityScore());
						int title = o1.getFirstRecord().getTitle()
								.compareTo(o2.getFirstRecord().getTitle());

						if (score != 0) {
							return -score;
						} else {
							return title;
						}
					}

				});

		// print the correspondences
		for (Correspondence<Movie, Attribute> correspondence : correspondences) {
			logger.info(String
					.format("%s,%s,|\t\t%.2f\t[%s] %s (%s) <--> [%s] %s (%s)",
							correspondence.getFirstRecord().getIdentifier(),
							correspondence.getSecondRecord().getIdentifier(),
							correspondence.getSimilarityScore(),
							correspondence.getFirstRecord().getIdentifier(),
							correspondence.getFirstRecord().getTitle(),
							correspondence.getFirstRecord().getDate()
									.toLocalDate().toString(), correspondence
									.getSecondRecord().getIdentifier(),
							correspondence.getSecondRecord().getTitle(),
							correspondence.getSecondRecord().getDate()
									.toLocalDate().toString()));
		}
	}

}
