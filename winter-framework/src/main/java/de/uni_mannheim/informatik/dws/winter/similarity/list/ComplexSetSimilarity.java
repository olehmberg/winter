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

import java.util.Collection;

import de.uni_mannheim.informatik.dws.winter.matrices.SimilarityMatrix;
import de.uni_mannheim.informatik.dws.winter.matrices.SparseSimilarityMatrixFactory;
import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * super class for set-based similarity functions that compare the elements of the sets using an inner similarity function
 * @author Oliver
 *
 * @param <T>
 */
public abstract class ComplexSetSimilarity<T> extends SimilarityMeasure<Collection<T>> {

	private static final long serialVersionUID = 1L;

	private SimilarityMeasure<T> innerSimilarity;
	private double innerSimilarityThreshold;
	
	public SimilarityMeasure<T> getInnerSimilarity() {
		return innerSimilarity;
	}
	public void setInnerSimilarity(SimilarityMeasure<T> innerSimilarity) {
		this.innerSimilarity = innerSimilarity;
	}
	public double getInnerSimilarityThreshold() {
		return innerSimilarityThreshold;
	}
	public void setInnerSimilarityThreshold(double innerSimilarityThreshold) {
		this.innerSimilarityThreshold = innerSimilarityThreshold;
	}
    
    protected abstract Double aggregateSimilarity(SimilarityMatrix<T> matrix);
    
	@Override
	public double calculate(Collection<T> first, Collection<T> second) {
		
		SimilarityMatrix<T> matrix = new SparseSimilarityMatrixFactory().createSimilarityMatrix(first.size(), second.size());
		
		for(T t1 : first) {
			for(T t2 : second) {
				double sim = getInnerSimilarity().calculate(t1, t2);
				if(sim >= getInnerSimilarityThreshold()) {
					matrix.set(t1, t2, sim);
				} else {
					// we have to add the value, otherwise the dimensions of the matrix could be too small and a normalisation in the aggregateSimilarity method would be wrong
					matrix.set(t1, t2, 0.0);
				}
			}
		}
		
		return aggregateSimilarity(matrix);
	}
}
