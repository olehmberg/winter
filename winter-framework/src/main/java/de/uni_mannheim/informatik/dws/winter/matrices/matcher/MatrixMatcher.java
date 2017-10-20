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

package de.uni_mannheim.informatik.dws.winter.matrices.matcher;

import de.uni_mannheim.informatik.dws.winter.matrices.SimilarityMatrixFactory;
import de.uni_mannheim.informatik.dws.winter.matrices.SparseSimilarityMatrixFactory;

/**
 * super class for all second-line matchers (those that take similarity matrices as input and output another similarity matrix)
 * @author Oliver
 *
 */
public abstract class MatrixMatcher {

	SimilarityMatrixFactory similarityMatrixFactory;
	/**
	 * @return returns the similarity matrix factory that is used to create the similarity matrix containing the matching result
	 */
	public SimilarityMatrixFactory getSimilarityMatrixFactory() {
		return similarityMatrixFactory;
	}
	/**
	 * sets the similarity matrix factory that is used to create the similarity matrix containing the matching result
	 * 
	 * @param factory 	the factory that will be used to create new matrices
	 */
	public void setSimilarityMatrixFactory(SimilarityMatrixFactory factory) {
		similarityMatrixFactory = factory;
	}
	
	public MatrixMatcher()
	{
		setSimilarityMatrixFactory(new SparseSimilarityMatrixFactory());
	}
	
}
