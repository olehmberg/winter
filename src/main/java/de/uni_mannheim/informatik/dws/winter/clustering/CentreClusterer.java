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
package de.uni_mannheim.informatik.dws.winter.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;

/**
 * Implements the CENTER clustering algorithm, which finds star-shaped clusters
 * in an undirected, weighted graph. The algorithm was first presented by
 * Haveliwala et al. in Scalable Techniques for Clustering the Web
 * (http://ilpubs.stanford.edu:8090/445/) The algorithm first sorts all node
 * pairs by their weight. Starting with the strongest pair the algorithm checks
 * if on of both nodes was already assigned to a cluster, if none of the nodes
 * was assigned, a new cluster is created with the first node as center. In case
 * one node is already part of a cluster, the algorithm checks if this node is
 * the center. In this case the other node is added to the cluster. The output
 * are a rather larger number of star-shaped clusters, which have a maximal
 * diameter of 2.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class CentreClusterer<T> extends GraphBasedClusteringAlgorithm<T> {

	/**
	 * applies the clustering algorithm and returns a map of centre -&gt; cluster
	 */
	public Map<Collection<T>,T> cluster(
			Collection<Triple<T, T, Double>> similarityGraph) {

		ArrayList<Triple<T, T, Double>> sorted = new ArrayList<>(
				similarityGraph);

		Collections.sort(sorted, new Comparator<Triple<T, T, Double>>() {

			@Override
			public int compare(Triple<T, T, Double> o1, Triple<T, T, Double> o2) {
				return -Double.compare(o1.getThird(), o2.getThird());
			}

		});

		HashSet<T> assignedNodes = new HashSet<>();
		Map<T, Collection<T>> clusters = new HashMap<>();

		for (Triple<T, T, Double> trip : sorted) {

			if (!assignedNodes.contains(trip.getFirst())
					&& !assignedNodes.contains(trip.getSecond())) {

				// create a new cluster
				Collection<T> clu = new LinkedList<>();
				clu.add(trip.getFirst());
				clu.add(trip.getSecond());
				clusters.put(trip.getFirst(), clu);

				assignedNodes.add(trip.getFirst());
				assignedNodes.add(trip.getSecond());
			} else {

				if (!assignedNodes.contains(trip.getFirst())) {
					// only first is unassigned, add it to the cluster if second
					// is a centre
					Collection<T> clu = clusters.get(trip.getSecond());

					if (clu != null) {
						clu.add(trip.getFirst());
						assignedNodes.add(trip.getFirst());
					}
				} else if (!assignedNodes.contains(trip.getSecond())) {
					// only second is unassigned, add it to the cluster if first
					// is a centre
					Collection<T> clu = clusters.get(trip.getFirst());

					if (clu != null) {
						clu.add(trip.getSecond());
						assignedNodes.add(trip.getSecond());
					}
				}

			}
		}

		return MapUtils.invert(clusters);
	}

}
