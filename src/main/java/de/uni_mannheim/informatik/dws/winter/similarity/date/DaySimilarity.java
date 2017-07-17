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
package de.uni_mannheim.informatik.dws.winter.similarity.date;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * {@link SimilarityMeasure}, that calculates the similarity of two dates, based
 * on the number of days between them. The similarity is 0 if the difference
 * exceeds a maximal number of days
 * 
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
public class DaySimilarity extends SimilarityMeasure<LocalDateTime> {

	private static final long serialVersionUID = 1L;
	private int maxDifference;

	/**
	 * Initialize {@link DaySimilarity} with a maximal difference (in days). In
	 * case the difference is larger the maximal difference, the calculated
	 * similarity is 0. In the other cases its 1-(diff/maxDifference).
	 * 
	 * @param maxDifference
	 *            maximal difference between two dates in day.
	 */
	public DaySimilarity(int maxDifference) {
		this.maxDifference = maxDifference;
	}

	@Override
	public double calculate(LocalDateTime first, LocalDateTime second) {
		if (first == null || second == null) {
			return 0.0;
		} else {

			int diff = Math.abs(Math.toIntExact(first.getLong(ChronoField.EPOCH_DAY)-second.getLong(ChronoField.EPOCH_DAY)));

			double norm = Math.min((double) diff / (double) maxDifference, 1.0);

			return 1 - norm;
		}
	}

}
