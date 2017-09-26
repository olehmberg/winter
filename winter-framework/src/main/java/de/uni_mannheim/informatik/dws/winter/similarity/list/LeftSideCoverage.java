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
import de.uni_mannheim.informatik.dws.winter.matrices.matcher.BestChoiceMatching;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * The first set (left side) is important here. A similarity of 1 is reached if
 * each element of the first set has a corresponding element in the second set
 * (right side) with an inner similarity of 1
 * 
 * @author Oliver
 * 
 * @param <T>
 */
public class LeftSideCoverage<T extends Comparable<T>> extends
        ComplexSetSimilarity<T> {

	private static final long serialVersionUID = 1L;

	@Override
    protected Double aggregateSimilarity(SimilarityMatrix<T> matrix) {
        if(matrix.getFirstDimension().size()==0 || matrix.getSecondDimension().size()==0) {
            return 0.0;
        }
        
        BestChoiceMatching best = new BestChoiceMatching();
        best.setForceOneToOneMapping(true);
        SimilarityMatrix<T> bestMatrix = best.match(matrix);
        Collection<Double> scores = bestMatrix.getRowSums();

        // best only contains matched pairs, so we have to divide by the
        // dimension of the initial matrix to get the correct average
        return Q.sum(scores)
                / (double) matrix.getFirstDimension().size();
    }

}
