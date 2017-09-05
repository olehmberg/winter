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
package de.uni_mannheim.informatik.dws.winter.usecase.events;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlockerWithBlockFiltering;
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
import de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution.EventBlockingKeyByDecadeGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution.EventBlockingKeyByLabelsTokens;
import de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution.EventLabelComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution.EventURIComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.EventXMLReader;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieBlockingKeyByYearGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDateComparator10Years;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorEqual;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;

/**
 * Class containing the standard setup to perform a identity resolution task,
 * reading input data from the event usecase. Based on the movie usecase.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * @author Robert Meusel (robert@dwslab.de)
 * @author Daniel Ringler
 * 
 */
public class Events_IdentityResolution_Main {

	public static void main(String[] args) throws Exception {

		// Ratio for Block Filtering
		double ratio = 0.5;

		// loading data
		HashedDataSet<Event, Attribute> dataDBpedia = new HashedDataSet<>();
		new EventXMLReader().loadFromXML(new File("usecase/events/input/dbpedia_events_sample.xml"), "/events/event", dataDBpedia);

		HashedDataSet<Event, Attribute> dataYAGO = new HashedDataSet<>();
		new EventXMLReader().loadFromXML(new File("usecase/events/input/yago_events_sample.xml"), "/events/event", dataYAGO);

		// create a matching rule
		LinearCombinationMatchingRule<Event, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.7);
		// add comparators
		matchingRule.addComparator(new EventLabelComparatorLevenshtein(), 1);

		// Blocking with block filtering
		StandardRecordBlockerWithBlockFiltering<Event, Attribute> blocker = new StandardRecordBlockerWithBlockFiltering<Event, Attribute>(new EventBlockingKeyByLabelsTokens(), ratio);

		// Initialize Matching Engine
		MatchingEngine<Event, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Event, Attribute>> correspondences = engine.runIdentityResolution(
				dataDBpedia, dataYAGO, null, matchingRule,
				blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/events/output/dbpedia_2_yago_correspondences.csv"), correspondences);

		// print the correspondences to console
		// printCorrespondences(correspondences);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromTSVFile(new File(
				"usecase/events/goldstandard/dbpedia_2_yago_sample.tsv"));

		// evaluate your result
		MatchingEvaluator<Event, Attribute> evaluator = new MatchingEvaluator<Event, Attribute>(true);
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(),
				gsTest);

		// print the evaluation result
		System.out.println("DBpedia <-> YAGO");
		System.out
				.println(String.format(
						"Precision: %.4f\nRecall: %.4f\nF1: %.4f",
						perfTest.getPrecision(), perfTest.getRecall(),
						perfTest.getF1()));
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
		matchingRule.addComparator(new MovieTitleComparatorLevenshtein(), 0.5);
		matchingRule.addComparator(new MovieDateComparator10Years(), 0.5);

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
		System.out.println("Academy Awards <-> Actors");
		System.out
				.println(String.format(
						"Precision: %.4f\nRecall: %.4f\nF1: %.4f",
						perfTest.getPrecision(), perfTest.getRecall(),
						perfTest.getF1()));
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
		System.out.println("Academy Awards <-> Actors");
		System.out
				.println(String.format(
						"Precision: %.4f\nRecall: %.4f\nF1: %.4f",
						perfTest.getPrecision(), perfTest.getRecall(),
						perfTest.getF1()));

		System.out.println("Actors <-> Golden Globes");
		System.out.println(String.format(
				"Precision: %.4f\nRecall: %.4f\nF1: %.4f",
				perf2.getPrecision(), perf2.getRecall(), perf2.getF1()));
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
			System.out.println(String
					.format("%s,%s,|\t\t%.2f\t[%s] %s (%s) <--> [%s] %s (%s)",
							correspondence.getFirstRecord().getIdentifier(),
							correspondence.getSecondRecord().getIdentifier(),
							correspondence.getSimilarityScore(),
							correspondence.getFirstRecord().getIdentifier(),
							correspondence.getFirstRecord().getTitle(),
							correspondence.getFirstRecord().getDate()
									.toString("YYYY-MM-DD"), correspondence
									.getSecondRecord().getIdentifier(),
							correspondence.getSecondRecord().getTitle(),
							correspondence.getSecondRecord().getDate()
									.toString("YYYY-MM-DD")));
		}
	}

	public static Processable<Correspondence<Event, Attribute>> runIdentityResolution(HashedDataSet<Event, Attribute> dataSetD, HashedDataSet<Event, Attribute> dataSetY, char separator) throws Exception {
		// create a matching rule
		LinearCombinationMatchingRule<Event, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.96);
		// add comparators
		matchingRule.addComparator(new EventURIComparatorLevenshtein(), 1);
		//matchingRule.addComparator(new EventLabelComparatorLevenshtein(), 0.8);
		//matchingRule.addComparator(new EventDateComparator(), 0.2);

		// create a blocker (blocking strategy)
		//NoBlocker<Event, DefaultSchemaElement> blocker = new NoBlocker<>();
		//BlockingKeyGenerator<Event> firstLabel = BlockingFunction.getFirstLabel();
     //   MultiBlockingKeyGenerator<Event> tokenizedAttributes = BlockingFunction.getStandardBlockingFunctionAllAttributes();

        //StandardBlocker<Event, DefaultSchemaElement> blocker = new StandardBlocker<Event, DefaultSchemaElement>(firstLabel);
     //   MultiKeyBlocker<Event, Attribute> blocker = new MultiKeyBlocker<>(tokenizedAttributes);
		StandardRecordBlocker<Event, Attribute> blocker = new StandardRecordBlocker<Event, Attribute>(new EventBlockingKeyByDecadeGenerator());

		// Initialize Matching Engine
		MatchingEngine<Event, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Event, Attribute>> correspondences = engine.runIdentityResolution(
				dataSetD, dataSetY, null, matchingRule,
				blocker);
//, true, 0.5);

		// write the correspondences to the output file
		/*engine.writeCorrespondences(
				correspondences.get(),
				new File(
						"WDI/usecase/event/output/dbpedia_2_yago_correspondences_s.csv"));
		*/
		// print the correspondences to console
		// printCorrespondences(correspondences);

		// load the gold standard (test set)
		MatchingGoldStandard gs = new MatchingGoldStandard();
		gs.loadFromTSVFile(new File(
				"usecase/events/goldstandard/sameAs_combined_with_negative_len3.tsv"));

		//		loadFromTSVFile(new File(
				//"WDI/usecase/event/goldstandard/dbpedia_2_yago_s.csv"));
		//		"../data/sameAs_combined_with_negative_len3.tsv")); //dbpedia_2_yago.tsv

		// evaluate your result
		MatchingEvaluator<Event, Attribute> evaluator = new MatchingEvaluator<Event, Attribute>(true);
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs);
//, false);

		// print the evaluation result
		System.out.println("DBpedia <-> YAGO");
		System.out
				.println(String.format(
						"Precision: %.4f\nRecall: %.4f\nF1: %.4f",
						perfTest.getPrecision(), perfTest.getRecall(),
						perfTest.getF1()));
	return correspondences;
	}

}
