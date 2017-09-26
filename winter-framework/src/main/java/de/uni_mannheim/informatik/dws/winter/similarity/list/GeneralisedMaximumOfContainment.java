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
package de.uni_mannheim.informatik.dws.winter.similarity.list;

import de.uni_mannheim.informatik.dws.winter.matrices.SimilarityMatrix;
import de.uni_mannheim.informatik.dws.winter.matrices.matcher.BestChoiceMatching;
import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class GeneralisedMaximumOfContainment<T extends Comparable<? super T>> extends ComplexSetSimilarity<T> {

	private static final long serialVersionUID = 1L;

	public GeneralisedMaximumOfContainment(SimilarityMeasure<T> innerSimilarity, double innerSimilarityThreshold) {
		setInnerSimilarity(innerSimilarity);
		setInnerSimilarityThreshold(innerSimilarityThreshold);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.similarity.list.ComplexSetSimilarity#aggregateSimilarity(de.uni_mannheim.informatik.dws.winter.matrices.SimilarityMatrix)
	 */
	@Override
	protected Double aggregateSimilarity(SimilarityMatrix<T> matrix) {
		
		double firstSize = matrix.getFirstDimension().size();
		double secondSize = matrix.getSecondDimension().size();
		
		BestChoiceMatching bcm = new BestChoiceMatching();
		bcm.setForceOneToOneMapping(true);
		matrix = bcm.match(matrix);
		
		double intersection = matrix.getSum();
		
		return Math.max(intersection/firstSize, intersection/secondSize);
		
		
//		int intersection = 0;
//		
//		for(DataType value : matrix.getFirstDimension()) {
//			if(matrix.getMatchesAboveThreshold(value, 0.0).size()>0) {
//				intersection++;
//			}
//		}
//		
//		return Math.max(intersection/(double)matrix.getFirstDimension().size(), intersection/(double)matrix.getSecondDimension().size());
	}

}
