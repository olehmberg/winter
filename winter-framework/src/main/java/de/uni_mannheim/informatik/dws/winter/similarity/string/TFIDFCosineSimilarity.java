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
package de.uni_mannheim.informatik.dws.winter.similarity.string;


import de.uni_mannheim.informatik.dws.winter.model.*;
import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;
import de.uni_mannheim.informatik.dws.winter.similarity.string.generator.TFIDFGenerator;
import de.uni_mannheim.informatik.dws.winter.similarity.string.generator.TokenGenerator;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceCosineSimilarity;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link SimilarityMeasure}, that calculates the TF-IDF similarity between two
 * strings.
 * 
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 * 
 */
public class TFIDFCosineSimilarity<RecordType extends Matchable, SchemaElementType extends Matchable, CorrespondenceType extends Matchable> extends SimilarityMeasure<String> {

	private static final long serialVersionUID = 1L;
	private VectorSpaceCosineSimilarity vectorSpaceCosineSimilarity = new VectorSpaceCosineSimilarity();
	private TFIDFGenerator<RecordType,SchemaElementType,CorrespondenceType> tfidfGenerator;
	private TokenGenerator<RecordType, CorrespondenceType> tokenizer;

	public TFIDFCosineSimilarity(DataSet<RecordType, SchemaElementType> datasetLeft,
								 DataSet<RecordType, SchemaElementType> datasetRight,
								 TokenGenerator<RecordType, CorrespondenceType> tokenizer){

		this.tokenizer = tokenizer;
		this.tfidfGenerator = new TFIDFGenerator<>(tokenizer);
		this.tfidfGenerator.initializeIDFScores(datasetLeft, datasetRight);
	}

	/**
	 * Calculates similarity between the first and second string.
	 * @param first
	 * 			the first string (can be null)
	 * @param second
	 * 			the second string (can be null)
	 * @return the similarity score between the first and second string
	 */
	@Override
	public double calculate(String first, String second) {

		if(first.isEmpty() || second.isEmpty()){
			return 0.0;
		}


		String[] tokens1 = this.tokenizer.tokenizeString(first);
		String[] tokens2 = this.tokenizer.tokenizeString(second);

		Set<String> uniqueTokens = new HashSet<>();
		Collections.addAll(uniqueTokens, tokens1);
		Collections.addAll(uniqueTokens, tokens2);

		HashMap<String, Integer> termFrequencies = new HashMap<>();
		termFrequencies = this.tfidfGenerator.calculateTermFrequencies(tokens1, termFrequencies);
		termFrequencies = this.tfidfGenerator.calculateTermFrequencies(tokens2, termFrequencies);

		HashMap<String, Double> tfIDF1 = this.tfidfGenerator.calculateTFIDFScores(tokens1, termFrequencies);
		HashMap<String, Double> tfIDF2 = this.tfidfGenerator.calculateTFIDFScores(tokens2, termFrequencies);

		double score = 0;

		for(String token : uniqueTokens){
			double score1 = 0;
			double score2 = 0;
			if(tfIDF1.containsKey(token) && tfIDF2.containsKey(token)){
				score1 = tfIDF1.get(token);
				score2 = tfIDF2.get(token);
			}

			double valueScore = this.vectorSpaceCosineSimilarity.calculateDimensionScore(score1,score2);
			score = this.vectorSpaceCosineSimilarity.aggregateDimensionScores(score, valueScore);
		}
		score = this.vectorSpaceCosineSimilarity.normaliseScore(score, tfIDF1, tfIDF2);

		return score;

	}

}
