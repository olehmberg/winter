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
package de.uni_mannheim.informatik.dws.winter.matching.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Group;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.RecordKeyValueMapper;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class MaximumBipartiteMatchingAlgorithm<TypeA extends Matchable, TypeB extends Matchable> implements MatchingAlgorithm<TypeA, TypeB> {

	private Processable<Correspondence<TypeA, TypeB>> correspondences;
	private Processable<Correspondence<TypeA, TypeB>> result;
	
	private boolean groupByLeftDataSource = false;
	private boolean groupByRightDataSource = false;
	
	/**
	 * 
	 * Specifies if correspondences should first be grouped by the data source ID of the left-hand side of the correspondences.
	 * If true, all data sources on the left-hand side will be processed individually
	 * 
	 * @param groupByLeftDataSource the groupByLeftDataSource to set
	 */
	public void setGroupByLeftDataSource(boolean groupByLeftDataSource) {
		this.groupByLeftDataSource = groupByLeftDataSource;
	}
	/**
	 * Specifies if correspondences should first be grouped by the data source ID of the right-hand side of the correspondences.
	 * If true, all data source on the right-hand side will be processed individually
	 * 
	 * @param groupByRightDataSource the groupByRightDataSource to set
	 */
	public void setGroupByRightDataSource(boolean groupByRightDataSource) {
		this.groupByRightDataSource = groupByRightDataSource;
	}
	
	public MaximumBipartiteMatchingAlgorithm(Processable<Correspondence<TypeA, TypeB>> correspondences) {
		this.correspondences = correspondences;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.algorithms.MatchingAlgorithm#run()
	 */
	@Override
	public void run() {
		
		// group correspondences by data source and then run the maximum matching.
		// if multiple sources are matched at the same time, the maximum matching would only allow one source to be matched to one other source
		// but we want that one element from a certain source (record or attribute) can only be matched to one other element.
		// two elements from different sources can be mapped to the same element in another source.
		
		Processable<Group<Pair<Integer, Integer>, Correspondence<TypeA, TypeB>>> grouped = correspondences.group(new RecordKeyValueMapper<Pair<Integer, Integer>, Correspondence<TypeA,TypeB>, Correspondence<TypeA,TypeB>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecordToKey(Correspondence<TypeA, TypeB> record,
					DataIterator<Pair<Pair<Integer, Integer>, Correspondence<TypeA, TypeB>>> resultCollector) {
				
				int leftGroup = groupByLeftDataSource ? record.getFirstRecord().getDataSourceIdentifier() : 0;
				int rightGroup = groupByRightDataSource ? record.getSecondRecord().getDataSourceIdentifier() : 0;
				
				resultCollector.next(new Pair<Pair<Integer,Integer>, Correspondence<TypeA,TypeB>>(new Pair<>(leftGroup, rightGroup), record));
				
			}

			
		});
		
		result = grouped.map(new RecordMapper<Group<Pair<Integer,Integer>,Correspondence<TypeA,TypeB>>, Correspondence<TypeA,TypeB>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(Group<Pair<Integer, Integer>, Correspondence<TypeA, TypeB>> record,
					DataIterator<Correspondence<TypeA, TypeB>> resultCollector) {
				
				// create the graph and the two partitions
				WeightedGraph<TypeA, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
				Set<TypeA> partition1 = new HashSet<>();
				Set<TypeA> partition2 = new HashSet<>();
				Map<DefaultWeightedEdge, Correspondence<TypeA, TypeB>> edgeToCorrespondence = new HashMap<>();
				
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("Group %d/%d\n", record.getKey().getFirst(), record.getKey().getSecond()));
				
				for(Correspondence<TypeA, TypeB> cor : record.getRecords().get()) {
					partition1.add(cor.getFirstRecord());
					partition2.add(cor.getSecondRecord());
					
					graph.addVertex(cor.getFirstRecord());
					graph.addVertex(cor.getSecondRecord());
					DefaultWeightedEdge edge = graph.addEdge(cor.getFirstRecord(), cor.getSecondRecord());
					graph.setEdgeWeight(edge,(int)( cor.getSimilarityScore() * 1000000)); // MaximumWeightBipartiteMatching only accepts integer weights ...
					edgeToCorrespondence.put(edge, cor);
					
					sb.append(String.format("\t%.6f\t%s <-> %s\n", cor.getSimilarityScore(), cor.getFirstRecord(), cor.getSecondRecord()));
				}
				
				// run the bipartite matching
				MaximumWeightBipartiteMatching<TypeA, DefaultWeightedEdge> matching = new MaximumWeightBipartiteMatching<>(graph, partition1, partition2);
				Matching<DefaultWeightedEdge> matchingResult = matching.computeMatching();
				
				sb.append("* result:\n");
				
				// add the results
				for(DefaultWeightedEdge edge : matchingResult.getEdges()) {
					Correspondence<TypeA, TypeB> cor = edgeToCorrespondence.get(edge);
					resultCollector.next(cor);
					
					sb.append(String.format("\t%.6f\t%s <-> %s\n", cor.getSimilarityScore(), cor.getFirstRecord(), cor.getSecondRecord()));
				}
				
//				System.out.println(sb.toString());
			}
		});

	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.algorithms.MatchingAlgorithm#getResult()
	 */
	@Override
	public Processable<Correspondence<TypeA, TypeB>> getResult() {
		return result;
	}

}
