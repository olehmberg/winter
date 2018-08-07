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

import org.apache.logging.log4j.Logger;

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
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorOverlapMultipleAttributes;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.identityresolution.ITunesBlockingKeyByArtistTitleGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.identityresolution.ITunesRuntimeComparatorDeviationSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.identityresolution.RecordComparatorJaccardWithBrackets;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.model.Song;
import de.uni_mannheim.informatik.dws.winter.usecase.itunes.model.iTunesSong;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * Class containing the standard setup to perform a identity resolution task by using learning matching rules,
 * reading input data from the iTunes use case.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */

public class iTunes_IdentityResolutionLearningMatchingRule {
	
	/*
	 * Trace Options:
	 * 		default: 	level INFO	- console
	 * 		trace:		level TRACE - console
	 * 		infoFile:	level INFO	- console/file
	 * 		traceFile:	level TRACE	- console/file
	 * 		
	 */
	private static final Logger logger = WinterLogManager.getLogger();
	//private static final Logger logger = WinterLogManager.getLogger("traceFile");
	
	public static void main(String[] args) throws Exception {
		// loading data
		Map<String, Attribute> columnMappingITunes = new HashMap<>();

		columnMappingITunes.put("page", iTunesSong.PAGE);
		columnMappingITunes.put("uri0", iTunesSong.URI0);
		columnMappingITunes.put("uri1", iTunesSong.URI1);
		columnMappingITunes.put("uri2", iTunesSong.URI2);
		columnMappingITunes.put("uri3", iTunesSong.URI3);
		columnMappingITunes.put("position", iTunesSong.POSITION);
		columnMappingITunes.put("name", iTunesSong.NAME);
		columnMappingITunes.put("artist", iTunesSong.ARTIST);
		columnMappingITunes.put("time", iTunesSong.TIME);

		// loading data
		Map<String, Attribute> columnMappingSong = new HashMap<>();
		columnMappingSong.put("rdf-schema#label", Song.RDFSCHEMA);
		columnMappingSong.put("runtime", Song.RUNTIME);
		columnMappingSong.put("album_label", Song.ALBUM);
		columnMappingSong.put("artist_label", Song.ARTIST);
		columnMappingSong.put("composer_label", Song.COMPOSER);
		columnMappingSong.put("genre_label", Song.GENRE);
		columnMappingSong.put("language_label", Song.LANGUAGE);
		columnMappingSong.put("producer_label", Song.PRODUCER);
		columnMappingSong.put("recordLabel_label", Song.RECORD);
		columnMappingSong.put("title", Song.NAME);
		columnMappingSong.put("trackNumber", Song.TRACKNUMBER);
		columnMappingSong.put("type_label", Song.TYPE);
		columnMappingSong.put("writer_label", Song.WRITER);

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
		// Collect debug results
		matchingRule.setCollectDebugResults(true);
		
		// add comparators - Name
		matchingRule.addComparator(new RecordComparatorLevenshtein(Song.ARTIST, iTunesSong.ARTIST));
		RecordComparatorEqual artistLowerCaseEqual = new RecordComparatorEqual(Song.ARTIST, iTunesSong.ARTIST);
		artistLowerCaseEqual.setLowerCase(true);
		matchingRule.addComparator(artistLowerCaseEqual);
		matchingRule.addComparator(new RecordComparatorJaccard(Song.ARTIST, iTunesSong.ARTIST, 0.3, true));
		matchingRule.addComparator(new RecordComparatorLevenshtein(Song.ARTIST, iTunesSong.ARTIST));
		RecordComparatorLevenshtein artistLowerCaseLevenshtein = new RecordComparatorLevenshtein(Song.ARTIST, iTunesSong.ARTIST);
		artistLowerCaseLevenshtein.setLowerCase(true);
		matchingRule.addComparator(artistLowerCaseLevenshtein);
		RecordComparatorJaccard lowerCaseArtistJaccard = new RecordComparatorJaccard(Song.ARTIST, iTunesSong.ARTIST, 0.3, true);
		lowerCaseArtistJaccard.setLowerCase(true);
		matchingRule.addComparator(lowerCaseArtistJaccard);
		RecordComparatorJaccard artistNoBrackets = new RecordComparatorJaccard(Song.ARTIST, iTunesSong.ARTIST, 0.3, true);
		artistNoBrackets.setRemoveBrackets(true);
		matchingRule.addComparator(artistNoBrackets);
		matchingRule.addComparator(new RecordComparatorJaccardWithBrackets(Song.ARTIST, iTunesSong.ARTIST, 0.3, true));
		
		matchingRule.addComparator(new RecordComparatorLevenshtein(Song.RDFSCHEMA, iTunesSong.NAME));
		RecordComparatorEqual labelNameLowerCaseEqual = new RecordComparatorEqual(Song.RDFSCHEMA, iTunesSong.NAME);
		labelNameLowerCaseEqual.setLowerCase(true);
		matchingRule.addComparator(labelNameLowerCaseEqual);
		matchingRule.addComparator(new RecordComparatorJaccard(Song.RDFSCHEMA, iTunesSong.NAME, 0.3, true));
		matchingRule.addComparator(new RecordComparatorLevenshtein(Song.RDFSCHEMA, iTunesSong.NAME));
		RecordComparatorLevenshtein labelNameLowerCaseLevenshtein = new RecordComparatorLevenshtein(Song.RDFSCHEMA, iTunesSong.NAME);
		labelNameLowerCaseLevenshtein.setLowerCase(true);
		matchingRule.addComparator(labelNameLowerCaseLevenshtein);
		RecordComparatorJaccard lowerCaseLabelJaccard = new RecordComparatorJaccard(Song.RDFSCHEMA, iTunesSong.NAME, 0.3, true);
		lowerCaseLabelJaccard.setLowerCase(true);
		matchingRule.addComparator(lowerCaseLabelJaccard);
		RecordComparatorJaccard labelNoBrackets = new RecordComparatorJaccard(Song.RDFSCHEMA, iTunesSong.NAME, 0.3, true);
		labelNoBrackets.setRemoveBrackets(true);
		matchingRule.addComparator(labelNoBrackets);
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
		blocker.setMeasureBlockSizes(true);
		
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
		
		// Write Debug Results to file
		blocker.writeDebugBlockingResultsToFile("usecase/itunes/output/debugResultsBlocking.csv");
		matchingRule.writeDebugMatchingResultsToFile("usecase/itunes/output/debugResultsWekaMatchingRule.csv");
		
		// evaluate your result
		MatchingEvaluator<Record, Attribute> evaluator = new MatchingEvaluator<>();
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(), gsTest);

		// print the evaluation result
		logger.info("DBPedia Song <-> iTunes");
		logger.info(String.format(
				"Precision: %.4f",perfTest.getPrecision()));
		logger.info(String.format(
				"Recall: %.4f",	perfTest.getRecall()));
		logger.info(String.format(
				"F1: %.4f",perfTest.getF1()));

	}
}
