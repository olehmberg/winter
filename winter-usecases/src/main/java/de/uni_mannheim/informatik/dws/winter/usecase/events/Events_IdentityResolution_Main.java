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
import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlockerWithBlockFiltering;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution.EventBlockingKeyByDecadeGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution.EventBlockingKeyByLabelsTokens;
import de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution.EventLabelComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution.EventURIComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.EventXMLReader;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

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
	
	/*
	 * Trace Options:
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
		// Collect debug results
		matchingRule.setCollectDebugResults(true);
		
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
		
		// Write debug results to file
		matchingRule.writeDebugMatchingResultsToFile("usecase/events/output/debugResultsMatchingRule.csv");
		
		// evaluate your result
		MatchingEvaluator<Event, Attribute> evaluator = new MatchingEvaluator<Event, Attribute>();
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(),
				gsTest);

		// print the evaluation result
		logger.info("DBpedia <-> YAGO");
		logger.info(String.format(
				"Precision: %.4f",perfTest.getPrecision()));
		logger.info(String.format(
				"Recall: %.4f",	perfTest.getRecall()));
		logger.info(String.format(
				"F1: %.4f",perfTest.getF1()));
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
		MatchingEvaluator<Event, Attribute> evaluator = new MatchingEvaluator<Event, Attribute>();
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gs);
//, false);

		// print the evaluation result
		logger.info("DBpedia <-> YAGO");
		logger.info(String.format(
				"Precision: %.4f",perfTest.getPrecision()));
		logger.info(String.format(
				"Recall: %.4f",	perfTest.getRecall()));
		logger.info(String.format(
				"F1: %.4f",perfTest.getF1()));
	return correspondences;
	}

}
