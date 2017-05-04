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
package de.uni_mannheim.informatik.dws.winter.utils.graph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * Model of an edge in a graph.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Edge<TNode, TEdge> {

	public static class EdgeByNodeIdComparator<TNode, TEdge> implements Comparator<Edge<TNode, TEdge>> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Edge<TNode, TEdge> o1, Edge<TNode, TEdge> o2) {
			Node<TNode> min1 = Q.min(o1.nodes, new Node.NodeIdProjection<TNode>());
			Node<TNode> min2 = Q.min(o2.nodes, new Node.NodeIdProjection<TNode>());
			
			int firstNode = Integer.compare(min1.getId(), min2.getId());
			
			if(firstNode!=0) {
				return firstNode;
			} else {
			
				Node<TNode> max1 = Q.max(o1.nodes, new Node.NodeIdProjection<TNode>());
				Node<TNode> max2 = Q.max(o2.nodes, new Node.NodeIdProjection<TNode>());
				
				return Integer.compare(max1.getId(), max2.getId());
				
			}
		}
		
	}
	
	private Set<Node<TNode>> nodes;
	private TEdge data;
	private double weight = 1.0;
	
	/**
	 * 
	 */
	public Edge(Node<TNode> n1, Node<TNode> n2, TEdge data, double weight) {
		nodes = new HashSet<>();
		nodes.add(n1);
		nodes.add(n2);
		this.data = data;
		this.weight = weight;
	}
	
	/**
	 * @return the nodes
	 */
	public Set<Node<TNode>> getNodes() {
		return nodes;
	}
	
	/**
	 * @return the data
	 */
	public TEdge getData() {
		return data;
	}

	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Edge) {
			@SuppressWarnings("rawtypes")
			Edge e = (Edge)obj;
					
			return nodes.equals(e.nodes);
				
		} else {
			return super.equals(obj);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return nodes.hashCode(); 
	}
}
