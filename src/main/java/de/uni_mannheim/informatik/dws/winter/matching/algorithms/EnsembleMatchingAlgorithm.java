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

import java.util.Collection;
import java.util.LinkedList;

import de.uni_mannheim.informatik.dws.winter.matching.aggregators.CorrespondenceAggregator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Ensemble that combines the results of multiple matchers.
 * For each individual matcher, the resulting correspondences and a specific weight can be specified.
 * The ensemble adjusts the similarity scores by multiplying them with the weight and then passes them to an aggregator that creates the combined correspondences.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class EnsembleMatchingAlgorithm<TypeA extends Matchable, TypeB extends Matchable> implements MatchingAlgorithm<TypeA, TypeB> {

	private Processable<Correspondence<TypeA, TypeB>> result;
	private Collection<Pair<Processable<Correspondence<TypeA, TypeB>>, Double>> baseMatcherResults;
	private CorrespondenceAggregator<TypeA, TypeB> aggregator;
	
	
	public EnsembleMatchingAlgorithm(CorrespondenceAggregator<TypeA, TypeB> aggregator) {
		this.baseMatcherResults = new LinkedList<>();
		this.aggregator = aggregator;
	}
	
	public void addBaseMatcherResult(Processable<Correspondence<TypeA, TypeB>> result, double weight) {
		baseMatcherResults.add(new Pair<>(result, weight));
	}
	
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.algorithms.MatchingAlgorithm#run()
	 */
	@Override
	public void run() {
		
		Processable<Correspondence<TypeA, TypeB>> combined = null;
		
		// transform the incoming correspondences by applying the weight
		for(Pair<Processable<Correspondence<TypeA, TypeB>>, Double> pair : baseMatcherResults) {
			
			Processable<Correspondence<TypeA, TypeB>> weighted = pair.getFirst().map((r,c) -> c.next(new Correspondence<>(r.getFirstRecord(), r.getSecondRecord(), r.getSimilarityScore() * pair.getSecond(), r.getCausalCorrespondences())));
			
			if(combined==null) {
				combined = weighted;
			} else {
				combined = combined.append(weighted);
			}
			
		}
		
		// group all correspondences between the same elements and aggregate their scores
		Processable<Pair<Pair<TypeA, TypeA>, Correspondence<TypeA, TypeB>>> aggregated = combined.aggregate((r,c) -> {
			if(r!=null) {
				c.next(new Pair<>(new Pair<>(r.getFirstRecord(), r.getSecondRecord()), r));
			}
		}, aggregator);
		
		Processable<Correspondence<TypeA, TypeB>> result = aggregated.map((p,c)->c.next(p.getSecond()));
		
		setResult(result);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.algorithms.MatchingAlgorithm#getResult()
	 */
	@Override
	public Processable<Correspondence<TypeA, TypeB>> getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(Processable<Correspondence<TypeA, TypeB>> result) {
		this.result = result;
	}

}
