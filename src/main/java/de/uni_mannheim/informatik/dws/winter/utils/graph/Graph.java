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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * Model of a graph.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Graph<TNode, TEdge> {

	private Map<TNode,Node<TNode>> nodes;
	private Set<Edge<TNode, TEdge>> edges;
	
	/**
	 * 
	 */
	public Graph() {
		nodes = new HashMap<>();
		edges = new HashSet<>();
	}
	
	public List<TNode> getDataNodes() {
		return new ArrayList<>(Q.project(Q.sort(nodes.values(), new Node.NodeIdComparator<TNode>()), new Node.NodeDataProjection<TNode>()));
	}
	
	public List<Node<TNode>> getGraphNodes() {
		return Q.sort(nodes.values(), new Node.NodeIdComparator<TNode>());
	}
	
	public void addNode(TNode n) {
		Node<TNode> node1 = nodes.get(n);
		if(node1==null) {
			node1 = new Node<>(n, nodes.size()+1);
		}
		nodes.put(n, node1);
	}
	
	public void addEdge(TNode n1, TNode n2, TEdge data, double edgeWeight) {
		Node<TNode> node1 = nodes.get(n1);
		if(node1==null) {
			node1 = new Node<>(n1, nodes.size()+1);
		}
		nodes.put(n1, node1);
		
		Node<TNode> node2 = nodes.get(n2);
		if(node2==null) {
			node2 = new Node<>(n2, nodes.size()+1);
		}
		nodes.put(n2, node2);
		edges.add(new Edge<>(node1, node2, data, edgeWeight));
	}
	
	public void writePajekFormat(File f) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		
		w.write(String.format("*Vertices %d\n", nodes.size()));
		for(Node<TNode> n : Q.sort(nodes.values(), new Node.NodeIdComparator<TNode>())) {
			w.write(String.format("%d \"%s\"\n", n.getId(), n.getData().toString()));
		}
		
		w.write(String.format("*Edges %d\n", edges.size()));
		for(Edge<TNode, TEdge> e : Q.sort(edges, new Edge.EdgeByNodeIdComparator<TNode, TEdge>())) {
			List<Node<TNode>> ordered = Q.sort(e.getNodes(), new Node.NodeIdComparator<TNode>());
			if(ordered.size()>1) {
				Node<TNode> n1 = ordered.get(0);
				Node<TNode> n2 = ordered.get(1);
				
				w.write(String.format("%d %d %s l \"%s\"\n", n1.getId(), n2.getId(), Double.toString(e.getWeight()), e.getData().toString()));
			}
		}
		
		w.close();
	}
	
}
