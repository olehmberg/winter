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
package de.uni_mannheim.informatik.dws.winter.similarity.vectorspace;

import java.util.Map;

/**
 * 
 * Defines the operations necessary to calculate the similarity between two vector space representations.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public interface VectorSpaceSimilarity {

	/**
	 * 
	 * Defines how to calculate the score for a single dimension between two vectors
	 * 
	 * @param vector1
	 * 			the value of the first vector for the current dimension
	 * @param vector2
	 * 			the value of the second vector for the current dimension
	 * @return
	 * 			the score
	 */
	double calculateDimensionScore(double vector1, double vector2);
	
	/**
	 * 
	 * Defines how the scores for each dimension are aggregated
	 * 
	 * @param lastScore
	 * 			the current score (i.e., the result of the last call to aggregateDimensionScore)
	 * @param nextScore
	 * 			the score that should be added to the aggregation result (i.e., the result of the last call to calculateDimensionScore)
	 * @return
	 * 			the aggregated score
	 */
	double aggregateDimensionScores(double lastScore, double nextScore);
	
	/**
	 * 
	 * Defines how the final score is normalised
	 * 
	 * @param score
	 * 			the final score
	 * @param vector1
	 * 			the complete first vector
	 * @param vector2
	 * 			the complete second vector
	 * @return
	 */
	double normaliseScore(double score, Map<String, Double> vector1, Map<String, Double> vector2);
}
