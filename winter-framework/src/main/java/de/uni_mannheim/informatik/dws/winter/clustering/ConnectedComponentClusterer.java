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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.Triple;

/**
 * 
 * A Clusterer that returns the weakly connected components in the graph formed by the input data.
 *
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ConnectedComponentClusterer<T> extends GraphBasedClusteringAlgorithm<T> {

	HashMap<T, Set<T>> clusterAssignment = new HashMap<>();

	@Override
	public Map<Collection<T>, T> cluster(
			Collection<Triple<T, T, Double>> similarityGraph) {

		clusterAssignment = new HashMap<>();

		// iterate over all edges
		for(Triple<T, T, Double> edge : similarityGraph) {
			
			addEdge(edge);

		}
		
		return createResult();
	}

	public void addEdge(Triple<T, T, Double> edge) {
		// get the clusters to which the nodes belong
		Set<T> first = clusterAssignment.get(edge.getFirst());
		Set<T> second = clusterAssignment.get(edge.getSecond());
		
		if(first==null && second==null) {
			// none of the nodes belongs to an existing cluster, create a new cluster
			Set<T> clu = new HashSet<>();
			clu.add(edge.getFirst());
			clu.add(edge.getSecond());
			
			clusterAssignment.put(edge.getFirst(), clu);
			clusterAssignment.put(edge.getSecond(), clu);
		} else if(first!=null && second==null) {
			// first node already belongs to a cluster, add second node
			first.add(edge.getSecond());
			
			clusterAssignment.put(edge.getSecond(), first);
		} else if(first==null && second!=null) {
			// second node already belongs to a cluster, add first node
			second.add(edge.getFirst());
			
			clusterAssignment.put(edge.getFirst(), second);
		} else {
			// both nodes belong to a cluster
			if(!first.equals(second)) {
				// if those are two different clusters, merge them
				for(T node : second) {
					clusterAssignment.put(node, first);
				}
				first.addAll(second);
			}
		}
	}
	
	public boolean isEdgeAlreadyInCluster(T firstNode, T secondNode) {
		// get the clusters to which the nodes belong
		Set<T> first = clusterAssignment.get(firstNode);
		Set<T> second = clusterAssignment.get(secondNode);
		
		return first!=null && second!=null && first.equals(second);
	}
	
	public Map<Collection<T>, T> createResult() {
		// format result
		Map<Collection<T>, T> result = new HashMap<>();
		for(Collection<T> cluster : clusterAssignment.values()) {
			result.put(cluster, null);
		}
		return result;
	}
}
