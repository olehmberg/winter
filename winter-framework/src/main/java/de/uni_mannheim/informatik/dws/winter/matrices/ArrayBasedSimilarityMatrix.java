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

package de.uni_mannheim.informatik.dws.winter.matrices;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Similarity matrix that uses an array as storage
 * @author Oliver
 *
 * @param <T>	the type of the matrix' dimensions
 */
public class ArrayBasedSimilarityMatrix<T> extends SimilarityMatrix<T> {

    private ArrayList<T> firstDimension;
    private ArrayList<T> secondDimension;
    private Double[][] similarities;

    public ArrayBasedSimilarityMatrix(int firstDimensionSize,
            int secondDimensionSize) {
        firstDimension = new ArrayList<T>(firstDimensionSize);
        secondDimension = new ArrayList<T>(secondDimensionSize);
        similarities = new Double[firstDimensionSize][secondDimensionSize];
    }

    @Override
    public Double get(T first, T second) {

        int idx1 = firstDimension.indexOf(first);
        int idx2 = secondDimension.indexOf(second);

        if (idx1 != -1 && idx2 != -1) {
            return similarities[idx1][idx2];
        } else {
            return null;
        }
    }

    @Override
    public void set(T first, T second, Double similarity) {
        int idx1 = -1, idx2 = 1;

        idx1 = firstDimension.indexOf(first);

        if (idx1 == -1) {
            synchronized (firstDimension) {
                // here we add only the value that indexes the dimension, we do not change the size of the similarity matrix!
                firstDimension.add(first);
                idx1 = firstDimension.size() - 1;
            }
        }

        idx2 = secondDimension.indexOf(second);

        if (idx2 == -1) {
            synchronized (secondDimension) {
                secondDimension.add(second);
                idx2 = secondDimension.size() - 1;
            }
        }

        similarities[idx1][idx2] = similarity;
    }

    @Override
    public Collection<T> getFirstDimension() {
        return firstDimension;
    }

    @Override
    public Collection<T> getSecondDimension() {
        return secondDimension;
    }

    @Override
    public Collection<T> getMatches(T first) {
        return getMatchesAboveThreshold(first, Double.NEGATIVE_INFINITY);
    }

    @Override
    public Collection<T> getMatchesAboveThreshold(T first,
            double similarityThreshold) {

        int idx1 = firstDimension.indexOf(first);

        if (idx1 == -1) {
            return null;
        } else {
            Collection<T> result = new ArrayList<T>(secondDimension.size());

            for (int i = 0; i < secondDimension.size(); i++) {
                if (similarities[idx1][i]!=null && similarities[idx1][i] > similarityThreshold) {
                    result.add(secondDimension.get(i));
                }
            }

            return result;
        }
    }

    @Override
    protected SimilarityMatrix<T> createEmptyCopy() {
        return new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(getFirstDimension().size(), getSecondDimension().size());
    }
}
