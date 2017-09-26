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
 * Calculates a numeric similarity based on the percental difference between the two numbers
 * @author Oliver
 *
 */
public class PercentageSimilarity extends SimilarityMeasure<Double> {

	private static final long serialVersionUID = 1L;
	private double max_percentage = 0.0;
	
	/**
	 * Creates a new instance of the similarity measure
	 * @param max_percental_difference the max percental difference between two values. Higher differences lead to a similarity value of 0.0.
	 */
	public PercentageSimilarity(double max_percental_difference) {
		this.max_percentage = max_percental_difference;
	}
	
	@Override
	public double calculate(Double first, Double second) {
		if(first==null || second==null) {
			return 0.0;
		} else {
			double pc = Math.abs(first-second)/Math.max(first, second);
			
			if(pc < max_percentage) {
				return 1 - pc/max_percentage;
			} else {
				return 0.0;
			}
		}
	}

	
	
}
