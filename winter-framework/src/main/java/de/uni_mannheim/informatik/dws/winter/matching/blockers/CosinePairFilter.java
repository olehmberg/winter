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
package de.uni_mannheim.informatik.dws.winter.matching.blockers;

import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class CosinePairFilter<CorrespondenceType extends Matchable, CausalType extends Matchable> implements VectorisedPairFilter<CorrespondenceType, CausalType> {

	private double threshold = 0.0;
	
	public CosinePairFilter(double threshold) {
		this.threshold = threshold;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.blockers.VectorisedBlockFilter#createFinalPair(de.uni_mannheim.informatik.dws.winter.model.Correspondence, java.util.Set, java.util.Set)
	 */
	@Override
	public Correspondence<CorrespondenceType, CausalType> createFinalPair(
			Correspondence<CorrespondenceType, CausalType> pair, Set<String> leftBlockingVector,
			Set<String> rightBlockingVector) {

		double scalarProduct = pair.getSimilarityScore();
		
		double normalisation = Math.sqrt(leftBlockingVector.size()) * Math.sqrt(rightBlockingVector.size());
		
		pair.setsimilarityScore(scalarProduct / normalisation);
		
		if(pair.getSimilarityScore()>=threshold) {
			return pair;
		} else {
			return null;
		}
	}

}
