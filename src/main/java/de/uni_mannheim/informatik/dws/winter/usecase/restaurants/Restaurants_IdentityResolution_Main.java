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
package de.uni_mannheim.informatik.dws.winter.usecase.restaurants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.RecordBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.SimpleCorrespondence;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.XMLRecordReader;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorJaccard;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.DatasetIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.restaurants.model.Restaurant;


/**
 * Class containing the standard setup to perform a identity resolution task,
 * reading input data from the Restaurant usecase.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class Restaurants_IdentityResolution_Main {

	public static void main(String[] args) throws Exception {
		// loading data
		Map<String, Attribute> nodeMapping = new HashMap<>();
		nodeMapping.put("Name", Restaurant.NAME);
		nodeMapping.put("Address", Restaurant.ADDRESS);
		nodeMapping.put("City", Restaurant.CITY);
		nodeMapping.put("Phone", Restaurant.PHONE);
		nodeMapping.put("Style", Restaurant.STYLE);
		
		DataSet<Record, Attribute> dataZagats = new HashedDataSet<>();
		new XMLRecordReader("id", nodeMapping).loadFromXML(new File("usecase/restaurant/input/zagats.xml"), "/restaurants/restaurant", dataZagats);
		HashedDataSet<Record, Attribute> dataFodors = new HashedDataSet<>();
		new XMLRecordReader("id", nodeMapping).loadFromXML(new File("usecase/restaurant/input/fodors.xml"), "/restaurants/restaurant", dataFodors);
				
		// create a matching rule
		LinearCombinationMatchingRule<Record, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.9);
		// add comparators
		//matchingRule.addComparator((m1,  m2, c) -> new TokenizingJaccardSimilarity().calculate(m1.getTitle(), m2.getTitle()) , 0.8);
		//matchingRule.addComparator((m1, m2, c) -> new YearSimilarity(10).calculate(m1.getDate(), m2.getDate()), 0.2);
		matchingRule.addComparator(new RecordComparatorLevenshtein(Restaurant.ADDRESS, Restaurant.ADDRESS), 0.6);
		matchingRule.addComparator(new RecordComparatorJaccard(Restaurant.STYLE, Restaurant.STYLE), 0.4);
		
		// create a blocker (blocking strategy)
		StandardRecordBlocker<Record, Attribute> blocker = new StandardRecordBlocker<>(new RecordBlockingKeyGenerator<Record, Attribute>(){

			/**
			 * {@link BlockingKeyGenerator} for {@link Restaurant}s, which generates a blocking
			 * key based on the city in which a restaurant is located. E.g. Los Angeles
			 * 
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void generateBlockingKeys(Record record,
					Processable<SimpleCorrespondence<Attribute>> correspondences,
					DatasetIterator<Pair<String, Record>> resultCollector) {
				resultCollector.next(new Pair<>((record.getValue(Restaurant.CITY)), record));
				
			}
		});

		// Initialize Matching Engine
		MatchingEngine<Record, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Record, Attribute>> correspondences = engine.runIdentityResolution(
				dataFodors, dataZagats, null, matchingRule,
				blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/Restaurant/output/fodors_zagats_correspondences.csv"), correspondences);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"usecase/restaurant/goldstandard/gs_restaurant.csv"));

		// evaluate your result
		MatchingEvaluator<Record, Attribute> evaluator = new MatchingEvaluator<>(true);
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(),
				gsTest);
		
		printCorrespondences(new ArrayList<>(correspondences.get()), gsTest);

		// print the evaluation result
		System.out.println("Fodors <-> Zagats");
		System.out
				.println(String.format(
						"Precision: %.4f\nRecall: %.4f\nF1: %.4f",
						perfTest.getPrecision(), perfTest.getRecall(),
						perfTest.getF1()));
	}
	
	private static void printCorrespondences(
			 List<Correspondence<Record, Attribute>> correspondences, MatchingGoldStandard goldStandard) {
		// sort the correspondences
		List<Correspondence<Record, Attribute>> missingCorrespondences = new ArrayList<Correspondence<Record, Attribute>>();
		for (Correspondence<Record, Attribute> correspondence : correspondences) {
			if (!goldStandard.containsPositive(correspondence.getFirstRecord(),
					correspondence.getSecondRecord()) && !goldStandard.containsNegative(correspondence.getFirstRecord(),
					correspondence.getSecondRecord())){
				missingCorrespondences.add(correspondence);
			}
		}
		// print the correspondences
		for (Correspondence<Record, Attribute> missingCorrespondence : missingCorrespondences) {
			System.out.println(String
					.format("%s,%s,false",
							missingCorrespondence.getFirstRecord().getIdentifier(),
							missingCorrespondence.getSecondRecord().getIdentifier()));
		}
	}
	
}
