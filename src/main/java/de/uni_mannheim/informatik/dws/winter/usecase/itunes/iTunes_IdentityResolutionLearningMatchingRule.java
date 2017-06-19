/** 
 *
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
package de.uni_mannheim.informatik.dws.winter.usecase.itunes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.WekaMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.CSVRecordReader;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorEqual;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorJaccard;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorJaccardWithBrackets;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorJaccardWithoutBrackets;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorLowerCaseEqual;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorLowerCaseJaccard;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorLowerCaseLevenshtein;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorOverlapMultipleAttributes;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.identityresolution.ITunesBlockingKeyByArtistTitleGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.identityresolution.ITunesRuntimeComparatorDeviationSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.model.Song;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.model.iTunesSong;

/**
 * Class containing the standard setup to perform a identity resolution task by using learning matching rules,
 * reading input data from the iTunes use case.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */

public class iTunes_IdentityResolutionLearningMatchingRule {

	public static void main(String[] args) throws Exception {
		// loading data
		Map<String, Attribute> columnMappingITunes = new HashMap<>();

		columnMappingITunes.put("1", iTunesSong.PAGE);
		columnMappingITunes.put("2", iTunesSong.URI0);
		columnMappingITunes.put("3", iTunesSong.URI1);
		columnMappingITunes.put("4", iTunesSong.URI2);
		columnMappingITunes.put("5", iTunesSong.URI3);
		columnMappingITunes.put("6", iTunesSong.POSITION);
		columnMappingITunes.put("7", iTunesSong.NAME);
		columnMappingITunes.put("8", iTunesSong.ARTIST);
		columnMappingITunes.put("9", iTunesSong.TIME);

		// loading data
		Map<String, Attribute> columnMappingSong = new HashMap<>();
		columnMappingSong.put("1", Song.RDFSCHEMA);
		columnMappingSong.put("2", Song.RUNTIME);
		columnMappingSong.put("3", Song.ALBUM);
		columnMappingSong.put("4", Song.ARTIST);
		columnMappingSong.put("5", Song.COMPOSER);
		columnMappingSong.put("6", Song.GENRE);
		columnMappingSong.put("7", Song.LANGUAGE);
		columnMappingSong.put("8", Song.PRODUCER);
		columnMappingSong.put("9", Song.RECORD);
		columnMappingSong.put("10", Song.NAME);
		columnMappingSong.put("11", Song.TRACKNUMBER);
		columnMappingSong.put("12", Song.TYPE);
		columnMappingSong.put("13", Song.WRITER);

		// load data
		DataSet<Record, Attribute> dataITunes = new HashedDataSet<>();
		new CSVRecordReader(0, columnMappingITunes).loadFromCSV(new File("usecase/itunes/input/itunes.csv"),
				dataITunes);
		DataSet<Record, Attribute> dataSong = new HashedDataSet<>();
		new CSVRecordReader(0, columnMappingSong).loadFromCSV(new File("usecase/itunes/input/song.csv"), dataSong);

		// load the gold standard (training set)
		MatchingGoldStandard gsTraining = new MatchingGoldStandard();
		gsTraining.loadFromCSVFile(new File("usecase/itunes/goldstandard/gs_iTunes_train.csv"));

		// create a matching rule + provide classifier, options + Feature
		// Selection
		String options[] = new String[1];
		options[0] = ""; 
		String tree = "J48"; // new instance of tree
		WekaMatchingRule<Record, Attribute> matchingRule = new WekaMatchingRule<>(0.8, tree, options);
		
		// add comparators - Name
		matchingRule.addComparator(new RecordComparatorLevenshtein(Song.ARTIST, iTunesSong.ARTIST));
		matchingRule.addComparator(new RecordComparatorLowerCaseEqual(Song.ARTIST, iTunesSong.ARTIST));
		matchingRule.addComparator(new RecordComparatorJaccard(Song.ARTIST, iTunesSong.ARTIST, 0.3, true));
		matchingRule.addComparator(new RecordComparatorLevenshtein(Song.ARTIST, iTunesSong.ARTIST));
		matchingRule.addComparator(new RecordComparatorLowerCaseLevenshtein(Song.ARTIST, iTunesSong.ARTIST));
		matchingRule.addComparator(new RecordComparatorLowerCaseJaccard(Song.ARTIST, iTunesSong.ARTIST, 0.3, true));
		matchingRule.addComparator(new RecordComparatorJaccardWithoutBrackets(Song.ARTIST, iTunesSong.ARTIST, 0.3, true));
		matchingRule.addComparator(new RecordComparatorJaccardWithBrackets(Song.ARTIST, iTunesSong.ARTIST, 0.3, true));
		
		matchingRule.addComparator(new RecordComparatorLevenshtein(Song.RDFSCHEMA, iTunesSong.NAME));
		matchingRule.addComparator(new RecordComparatorLowerCaseEqual(Song.RDFSCHEMA, iTunesSong.NAME));
		matchingRule.addComparator(new RecordComparatorJaccard(Song.RDFSCHEMA, iTunesSong.NAME, 0.3, true));
		matchingRule.addComparator(new RecordComparatorLevenshtein(Song.RDFSCHEMA, iTunesSong.NAME));
		matchingRule.addComparator(new RecordComparatorLowerCaseLevenshtein(Song.RDFSCHEMA, iTunesSong.NAME));
		matchingRule.addComparator(new RecordComparatorLowerCaseJaccard(Song.RDFSCHEMA, iTunesSong.NAME, 0.3, true));
		matchingRule.addComparator(new RecordComparatorJaccardWithoutBrackets(Song.RDFSCHEMA, iTunesSong.NAME, 0.3, true));
		matchingRule.addComparator(new RecordComparatorJaccardWithBrackets(Song.RDFSCHEMA, iTunesSong.NAME, 0.3, true));
		
		matchingRule.addComparator(new RecordComparatorEqual(Song.TRACKNUMBER, iTunesSong.POSITION));
		
		matchingRule.addComparator(new ITunesRuntimeComparatorDeviationSimilarity(Song.RUNTIME, iTunesSong.TIME));

		
		ArrayList<Attribute> songAttributes = new ArrayList<Attribute>();
		songAttributes.add(Song.ALBUM);
		songAttributes.add(Song.ARTIST);
		ArrayList<Attribute> iTunesAttributes = new ArrayList<Attribute>();
		iTunesAttributes.add(iTunesSong.PAGE);
		matchingRule.addComparator(new RecordComparatorOverlapMultipleAttributes(songAttributes, iTunesAttributes));
		
		ArrayList<Attribute> songAttributes2 = new ArrayList<Attribute>();
		songAttributes2.add(Song.RDFSCHEMA);
		ArrayList<Attribute> iTunesAttributes2 = new ArrayList<Attribute>();
		iTunesAttributes2.add(iTunesSong.ARTIST);
		iTunesAttributes2.add(iTunesSong.NAME);
		matchingRule.addComparator(new RecordComparatorOverlapMultipleAttributes(songAttributes2, iTunesAttributes2));
		
		
		// create a blocker (blocking strategy)
		StandardRecordBlocker<Record, Attribute> blocker = new StandardRecordBlocker<>(new ITunesBlockingKeyByArtistTitleGenerator());

		// learning Matching rule
		RuleLearner<Record, Attribute> learner = new RuleLearner<>();
		learner.learnMatchingRule(dataSong, dataITunes, null, matchingRule, gsTraining);

		// Store Matching Rule
		matchingRule.storeModel(new File("usecase/itunes/matchingRule/itunesMatchingModel.model"));

		// Initialize Matching Engine
		MatchingEngine<Record, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Record, Attribute>> correspondences = engine.runIdentityResolution(dataSong,
				dataITunes, null, matchingRule, blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/itunes/output/itunes_song_correspondences.csv"),
				correspondences);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File("usecase/itunes/goldstandard/gs_iTunes_test.csv"));

		// evaluate your result
		MatchingEvaluator<Record, Attribute> evaluator = new MatchingEvaluator<>(true);
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gsTest);

		// print the evaluation result
		System.out.println("DBPedia Song <-> iTunes");
		System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f", perfTest.getPrecision(),
				perfTest.getRecall(), perfTest.getF1()));

	}
}
