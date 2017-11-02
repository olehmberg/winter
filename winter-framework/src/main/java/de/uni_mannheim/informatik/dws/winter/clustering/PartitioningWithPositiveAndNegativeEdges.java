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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils2;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * 
 * A partitioning clustering algorithm that considers positive and negative scores between the elements.
 * Edges are undirected.
 * 
 * Implemented according to Algorithm 3 in:
 * 
 * Wang, Yue, and Yeye He. "Synthesizing mapping relationships using table corpus." Proceedings of the 2017 ACM International Conference on Management of Data. ACM, 2017.
 * 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class PartitioningWithPositiveAndNegativeEdges<T> extends GraphBasedClusteringAlgorithm<T> {

	// algorithm (basically the same as in table synthesis for partitioning):
	// - start with graph of tables with positive edges (similarity) and negative edges (violations)
	// - choose the strongest positive edge to merge two partitions, such that there is no negative edge between them
	// - merge the partitions
	// - update all edges
	// - repeat until no more partitions can be merged
	
	private HashMap<T, Set<T>> clusterAssignment = new HashMap<>();
	private Set<T> nodes = new HashSet<>();
	private Collection<Triple<T, T, Double>> positiveEdges;
	private Collection<Triple<T, T, Double>> negativeEdges;
	private double negativeThreshold;
	
	private boolean log = false;
	public void setLog(boolean log) {
		this.log = log;
	}
	
	public PartitioningWithPositiveAndNegativeEdges(double negativeThreshold) {
		this.negativeThreshold = negativeThreshold;
		positiveEdges = new LinkedList<>();
		negativeEdges = new LinkedList<>();
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.clustering.GraphBasedClusteringAlgorithm#cluster(java.util.Collection)
	 */
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
		
		if(edge.getThird()>0) {
			positiveEdges.add(edge);
		} else if(edge.getThird()<0) {
			negativeEdges.add(edge);
		}
		
		nodes.add(edge.getFirst());
		nodes.add(edge.getSecond());
	}
	
	public boolean isEdgeAlreadyInCluster(T firstNode, T secondNode) {
		// get the clusters to which the nodes belong
		Set<T> first = clusterAssignment.get(firstNode);
		Set<T> second = clusterAssignment.get(secondNode);
		
		return first!=null && second!=null && first.equals(second);
	}
	
	public Map<Collection<T>, T> createResult() {
		
		// initialise partitions
		for(T node : nodes) {
			clusterAssignment.put(node, Q.toSet(node));
		}
		
		// initialise edges between partitions
		Map<Set<T>, Map<Set<T>, Pair<Double, Double>>> clusterEdges = new HashMap<>();
		
		for(Triple<T, T, Double> e : positiveEdges) {
			Pair<Double,Double> scores = new Pair<Double, Double>(e.getThird(), 0.0);
			
			// add edges in both directions
			Map<Set<T>, Pair<Double, Double>> map2 = MapUtils.get(clusterEdges, clusterAssignment.get(e.getFirst()), new HashMap<>());
			map2.put(clusterAssignment.get(e.getSecond()), scores);
			
			map2 = MapUtils.get(clusterEdges, clusterAssignment.get(e.getSecond()), new HashMap<>());
			map2.put(clusterAssignment.get(e.getFirst()), scores);
			
		}
		for(Triple<T, T, Double> e : negativeEdges) {
			
			Map<Set<T>, Pair<Double, Double>> map2 = MapUtils.get(clusterEdges, clusterAssignment.get(e.getFirst()), new HashMap<>());
			Pair<Double,Double> scores = MapUtils.get(map2, clusterAssignment.get(e.getSecond()), new Pair<>(0.0,0.0));
			scores = new Pair<Double, Double>(scores.getFirst(), e.getThird());			
			map2.put(clusterAssignment.get(e.getSecond()), scores);
			
			map2 = MapUtils.get(clusterEdges, clusterAssignment.get(e.getSecond()), new HashMap<>());
			map2.put(clusterAssignment.get(e.getFirst()), scores);
		}
		
		if(log) {
			printGraph(clusterEdges);
		}
		
		while(true) {
			
			// find the edge with the highest positive weight, such that the negative weight is higher than the threshold
			Triple<Set<T>, Set<T>, Pair<Double, Double>> bestEdge = getMaxPositiveEdgeSatisfyingThreshold(clusterEdges);
			
			// stop if no edge can be found that satisfies the negative threshold
			if(bestEdge==null) {
				break;
			}
			
			if(log) {
				System.out.println(String.format("merge {%s} and {%s}", StringUtils.join(bestEdge.getFirst(), ","), StringUtils.join(bestEdge.getSecond(), ",")));
			}
			
			// merge the partitions that are connected by the selected edge
			Set<T> mergedPartition = Q.union(bestEdge.getFirst(), bestEdge.getSecond());
			
			// add the merged partition to the set of partitions
			for(T n : mergedPartition) {
				clusterAssignment.put(n, mergedPartition);
			}
			
			// add edges for the merged partition and update all edge weights
			updateEdges(clusterEdges, bestEdge, mergedPartition);
			
			// remove the partitions that are connected by the selected edge and all their edges from the graph
			clusterEdges.remove(bestEdge.getFirst());
			clusterEdges.remove(bestEdge.getSecond());
			
			if(log) {
				printGraph(clusterEdges);
			}
		}
		
		// format result
		Map<Collection<T>, T> result = new HashMap<>();
		for(Collection<T> cluster : clusterAssignment.values()) {
			result.put(cluster, null);
		}
		return result;
	}
	
	private void printGraph(Map<Set<T>, Map<Set<T>, Pair<Double, Double>>> clusterEdges) {
		System.out.println("***********************************************");
		for(Set<T> n1 : clusterEdges.keySet()) {
			
			Map<Set<T>, Pair<Double, Double>> map2 = clusterEdges.get(n1);
			
			for(Set<T> n2 : map2.keySet()) {
				
				Pair<Double, Double> score = map2.get(n2);
				
				System.out.println(String.format("{%s}->{%s}: (%.2f,%.2f)", StringUtils.join(n1, ","), StringUtils.join(n2, ","), score.getFirst(), score.getSecond()));
				
			}
			
		}
	}
	
	private void updateEdges(Map<Set<T>, Map<Set<T>, Pair<Double, Double>>> clusterEdges, Triple<Set<T>, Set<T>, Pair<Double, Double>> selectedEdge, Set<T> mergedPartition) {
		Map<Set<T>, Pair<Double, Double>> firstPartitionLinks = clusterEdges.get(selectedEdge.getFirst());
		Map<Set<T>, Pair<Double, Double>> secondPartitionLinks = clusterEdges.get(selectedEdge.getSecond());
		
		// update all nodes connected to the first of the two merged partitions
		for(Set<T> n2 : firstPartitionLinks.keySet()) {
			if(n2!=selectedEdge.getSecond()) {
				
				Pair<Double, Double> e1 = firstPartitionLinks.get(n2);
				Pair<Double, Double> e2 = secondPartitionLinks.get(n2);
				
				// n2 is connected to the first of the merged partitions, see if its also connected to the second
				 if(e2!=null) {

					 // combine the weights
					 double positiveWeight = e1.getFirst() + e2.getFirst();
					 double negativeWeight = Math.min(e1.getSecond(), e2.getSecond());
					 
					 e1 = new Pair<Double, Double>(positiveWeight, negativeWeight);
				 }
				 
				 // create the new edge
				 MapUtils2.put(clusterEdges, mergedPartition, n2, e1);
				 MapUtils2.put(clusterEdges, n2, mergedPartition, e1);
					 
				 // remove the links from n2 to the merged partitions
				 MapUtils2.remove(clusterEdges, n2, selectedEdge.getFirst());
				 MapUtils2.remove(clusterEdges, n2, selectedEdge.getSecond());
				 
			}
		}
		
		// update all nodes connected to the second of the two merged partitions
		for(Set<T> n2 : secondPartitionLinks.keySet()) {
			if(n2!=selectedEdge.getFirst()) {
				
				// we already updated the nodes connected to both partitions, so here we only consider those which are only connected to the second partition
				Pair<Double, Double> e1 = firstPartitionLinks.get(n2);
				Pair<Double, Double> e2 = secondPartitionLinks.get(n2);
				
				if(e1==null) {
					 // create the new edge
					 MapUtils2.put(clusterEdges, mergedPartition, n2, e2);
					 MapUtils2.put(clusterEdges, n2, mergedPartition, e2);
						 
					 // remove the links from n2 to the merged partitions
					 MapUtils2.remove(clusterEdges, n2, selectedEdge.getSecond());
				}
			}
		}
	}
	
	private Triple<Set<T>, Set<T>, Pair<Double, Double>> getMaxPositiveEdgeSatisfyingThreshold(Map<Set<T>, Map<Set<T>, Pair<Double, Double>>> clusterEdges) {
		
		Pair<Double, Double> maxScore = new Pair<Double, Double>(Double.MIN_VALUE, 0.0);
		Set<T> maxN1 = null;
		Set<T> maxN2 = null;
		
		for(Set<T> n1 : clusterEdges.keySet()) {
			Map<Set<T>, Pair<Double, Double>> map2 = clusterEdges.get(n1);
			
			for(Set<T> n2 : map2.keySet()) {
				Pair<Double, Double> scores = map2.get(n2);
				
				if(scores.getFirst()>maxScore.getFirst() && scores.getSecond()>=negativeThreshold) {
					maxScore = scores;
					maxN1 = n1;
					maxN2 = n2;
				}
			}
		}
		
		if(maxN1!=null) {
			return new Triple<>(maxN1,maxN2,maxScore);
		} else {
			return null;
		}
		
	}
}
