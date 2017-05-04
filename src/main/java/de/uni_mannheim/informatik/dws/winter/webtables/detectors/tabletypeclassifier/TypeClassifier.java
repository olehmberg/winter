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

package de.uni_mannheim.informatik.dws.winter.webtables.detectors.tabletypeclassifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ColumnType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TypeDetector;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.time.TimeAnnotator;

/**
 * Entrance Class for ColumnDetectionLibrary. Implements TypeDetector from
 * Normalisation.
 * 
 * @author Sanikumar Zope
 * @author Alexander Brinkmann
 *
 */

public class TypeClassifier implements TypeDetector {

	private static AnnotationPipeline pipeline;
	private Classifier classifier;
	private FeatureSet featureSet;

	public TypeClassifier() {
		pipeline = new AnnotationPipeline();
		classifier = new Classifier();
		featureSet = new FeatureSet(new MaxentTagger(
				"de.uni_mannheim.informatik.dws.winter.webtables.detectors.tabletypeclassifier\\english-left3words-distsim.tagger"));
		initialize();
	}

	/**
	 * Initializes the tokenizer to detect date columns.
	 */
	public void initialize() {
		Properties props = new Properties();
		pipeline.addAnnotator(new TokenizerAnnotator(false) {

			@Override
			public Tokenizer<CoreLabel> getTokenizer(Reader r) {
				// TODO Auto-generated method stub
				return new PTBTokenizer<CoreLabel>(r, new CoreLabelTokenFactory(), "");

			}

		});
		pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		pipeline.addAnnotator(new POSTaggerAnnotator(false));
		pipeline.addAnnotator(new TimeAnnotator("sutime", props));
	}

	/**
	 * Executes the ColumnTypeDetection for a two dimensional String Table.
	 * 
	 * @param tableArray
	 *            holds the two dimensional String Table.
	 * @return result of column detection
	 * @throws FileNotFoundException
	 * @throws IOException
	 */

	public HashMap<Integer, String> execute(String[][] tableArray)
			throws FileNotFoundException, IOException {

		HashMap<Integer, String> columnANDdatatype = new HashMap<Integer, String>();

		// 'transposearray' method is optional.
		// If you have row based array then you need to call this method,
		// otherwise not
		String[][] transposedTableArray = transposeArray(tableArray);
		int columnCount = 0;
		for (String[] column : transposedTableArray) {
			ColumnType columnType = null;
			String columnLabel = Integer.toString(columnCount);

			columnType = detectTypeForColumn(column, columnLabel);
			columnANDdatatype.put(columnCount, columnType.getType().toString());

			columnCount++;
		}

		return columnANDdatatype;
	}

	/**
	 * Based on the given Column entries. The calculation of the column features
	 * for the type detection is triggered.
	 * 
	 * @param col
	 *            holds the column for, which the features will be generated.
	 * @return returns the generated features.
	 */

	public List<String> calculateFeatures(String[] col) {

		List<String> features = new ArrayList<String>();

		featureSet.createFeatures(col, pipeline);

		features.add(String
				.valueOf(featureSet.getPercentageofAlphabeticCharacters()));
		features.add(String
				.valueOf(featureSet.getPercentageofPunctuationCharacters()));
		features.add(String.valueOf(featureSet.getCellContentPattern()));
		features.add(String.valueOf(
				featureSet.isContainPunctuationCharactersinHeaderCell()));
		features.add(String.valueOf(featureSet.getPOSPatternofHeaderCell()));
		features.add(String.valueOf(featureSet.getAverageCharacterLenghth()));
		features.add(String.valueOf(featureSet.isIsDateorTime()));
		features.add(String.valueOf(featureSet.isIsBooleanValue()));

		return features;
	}

	/**
	 * Triggers the prediction of datatypes from the features calculated in
	 * 'calculateFeatures(String[])'.
	 * 
	 * @param calculatedFeatures
	 *            holds the features, which were calculated for the column.
	 * @return returns the predicted Datatype.
	 * @throws IOException
	 */

	public DataType predictDatatype(List<String> calculatedFeatures)
			throws IOException {

		DataType type = classifier.classify(calculatedFeatures);

		return type;
	}

	/**
	 * Changes a String table from a column based to a row based format.
	 * 
	 * @param array
	 *            - Table in column based format
	 * @return Table in row based format.
	 */

	public String[][] transposeArray(String[][] array) {
		String[][] array_new = new String[array[0].length][array.length];
		for (int i = 0; i < array[0].length; i++) {
			for (int j = 0; j < array.length; j++) {
				array_new[i][j] = array[j][i];
			}
		}
		return array_new;
	}

	/**
	 * Interface method to reach the ColumnDetectionLibrary.
	 */
	public ColumnType detectTypeForColumn(Object[] attributeValues,
			String attributeLabel) {

		List<String> features = new ArrayList<String>();

		features = calculateFeatures((String[]) attributeValues);
		DataType type = null;
		try {
			type = predictDatatype(features);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ColumnType resColumnType = new ColumnType(type, null);
		return resColumnType;
	}

}
