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
import java.util.HashMap;
import java.util.Map;

/**
 * Model of a partitioning of a graph.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Partitioning<TNode> {

	private Map<TNode, Integer> nodes;
	private Graph<TNode, ?> graph;
	
	public Partitioning(Graph<TNode, ?> graph) {
		nodes = new HashMap<>();
		this.graph = graph;
	}
	
	public void setPartition(TNode node, int partition) {
		nodes.put(node, partition);
	}
	
	public void writePajekFormat(File f) throws IOException {
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		
		w.write(String.format("*Vertices %d\n", graph.getGraphNodes().size()));
		for(Node<TNode> n : graph.getGraphNodes()) {
			Integer partition = nodes.get(n.getData());
			
			if(partition==null) {
				partition = 0;
			}
			
			w.write(String.format("%d\n", partition));
		}
		
		w.close();
	}
}
