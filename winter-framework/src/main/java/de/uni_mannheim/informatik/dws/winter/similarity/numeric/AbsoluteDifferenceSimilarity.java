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
package de.uni_mannheim.informatik.dws.winter.similarity.numeric;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * {@link SimilarityMeasure} that calculates a numeric similarity based on the
 * absolute difference between the two numbers
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class AbsoluteDifferenceSimilarity extends SimilarityMeasure<Double> {

	private static final long serialVersionUID = 1L;
	private double diff_max = 0.0;

	/**
	 * Creates a new instance of the similarity measure
	 * 
	 * @param max_absolute_difference
	 *            the max absolute difference between two values. Higher
	 *            differences lead to a similarity value of 0.0.
	 */
	public AbsoluteDifferenceSimilarity(double max_absolute_difference) {
		this.diff_max = max_absolute_difference;
	}

	@Override
	public double calculate(Double first, Double second) {
		if (first == null || second == null) {
			return 0.0;
		} else {
			double diff = Math.abs(first - second);

			if (diff < diff_max) {
				return 1 - diff / diff_max;
			} else {
				return 0.0;
			}
		}
	}

}
