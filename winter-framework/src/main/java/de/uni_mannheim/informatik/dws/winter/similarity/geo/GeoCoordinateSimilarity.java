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
package de.uni_mannheim.informatik.dws.winter.similarity.geo;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * {@link SimilarityMeasure}, that calculates the similarity of two geo coordinates,
 * based on the distance in km between them. The similarity is 0 if the distance
 * exceeds the maximum distance.
 * 
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 * 
 */
public class GeoCoordinateSimilarity extends SimilarityMeasure<Pair<Double,Double>> {

	private static final long serialVersionUID = 1L;
	private double maxDistance;

	/**
	 * Initialize {@link GeoCoordinateSimilarity} with a maximal difference (in km). In
	 * case the difference is larger the maximal difference, the calculated
	 * similarity is 0. In the other cases its 1-(diff/maxDifference).
	 *
	 * @param maxDistance
	 *            maximal distance between two geo coordinates in km.
	 */
	public GeoCoordinateSimilarity(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	@Override
	public double calculate(Pair<Double, Double> first, Pair<Double, Double> second) {
		if (first == null || second == null) {
			return 0.0;
		} else {

			// initialize values
			double earthRadius = 6371; // in kilometers
			double lat1 = first.getFirst();
			double lat2 = second.getFirst();
			double lng1 = first.getSecond();
			double lng2 = second.getSecond();

			// calculate the distance
			double dLat = Math.toRadians(lat2 - lat1);
			double dLng = Math.toRadians(lng2 - lng1);

			double sindLat = Math.sin(dLat / 2);
			double sindLng = Math.sin(dLng / 2);

			double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
					* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			double dist = earthRadius * c;

			// normalize the distance
			double norm = Math.min(dist / maxDistance, 1.0);

			return 1-norm;
		}
	}
}
