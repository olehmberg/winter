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
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class VectorSpaceJaccardSimilarity implements VectorSpaceSimilarity {

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.blockers.VectorSpaceSimilarity#calculateDimensionScore(double, double)
	 */
	@Override
	public double calculateDimensionScore(double vector1, double vector2) {
		return Math.min(vector1, vector2);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.blockers.VectorSpaceSimilarity#aggregateDimensionScores(double, double)
	 */
	@Override
	public double aggregateDimensionScores(double lastScore, double nextScore) {
		return lastScore + nextScore;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.blockers.VectorSpaceSimilarity#normaliseScore(double, java.util.Map, java.util.Map)
	 */
	@Override
	public double normaliseScore(double score, Map<String, Double> vector1, Map<String, Double> vector2) {
		
		Set<String> allDimensions = Q.union(vector1.keySet(), vector2.keySet());
		
		double normaliseWith = 0.0;
		
		for(String dimension : allDimensions) {
			Double score1 = vector1.get(dimension);
			Double score2 = vector2.get(dimension);
			
			if(score1==null) score1 = 0.0;
			if(score2==null) score2 = 0.0;
			
			normaliseWith += Math.max(score1, score2);
			
		}
		
		return score / normaliseWith;
	}

}
