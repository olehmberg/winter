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
package de.uni_mannheim.informatik.dws.winter.matching.aggregators;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;

/**
 * 
 * An aggregation function that corresponds to voting by correspondences. Supports majority voting and weighted voting.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class VotingAggregator<TypeA extends Matchable, TypeB extends Matchable> 
	extends CorrespondenceAggregator<TypeA, TypeB> 
{

	private static final long serialVersionUID = 1L;

	private boolean useWeightedVoting = true;
	private double normaliseWith = 0.0;

	
	public VotingAggregator(boolean useWeightedVoting, double finalThreshold) {
		super(finalThreshold);
		this.useWeightedVoting = useWeightedVoting;
	}
	
	public VotingAggregator(boolean useWeightedVoting, double normaliseWith, double finalThreshold) {
		super(finalThreshold);
		this.useWeightedVoting = useWeightedVoting;
		this.normaliseWith = normaliseWith;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.aggregators.CorrespondenceAggregator#getSimilarityScore(de.uni_mannheim.informatik.dws.winter.model.Correspondence)
	 */
	@Override
	protected double getSimilarityScore(Correspondence<TypeA, TypeB> cor) {
		if(useWeightedVoting) {
			return cor.getSimilarityScore();
		} else {
			if(cor.getSimilarityScore()>0) {
				return 1.0;
			} else {
				return 0.0;
			}
		}
	}

	@Override
	public Pair<Correspondence<TypeA, TypeB>, Object> aggregate(Correspondence<TypeA, TypeB> previousResult,
			Correspondence<TypeA, TypeB> record, Object state) {
		
		// multiply the similarity score with each vote (causal correspondences)
		int numVotes = record.getCausalCorrespondences() == null || record.getCausalCorrespondences().size()==0 ? 1 : record.getCausalCorrespondences().size();
		
		if(previousResult==null) {
			Correspondence<TypeA, TypeB> result = new Correspondence<>(record.getFirstRecord(), record.getSecondRecord(),getSimilarityScore(record) * numVotes,record.getCausalCorrespondences().copy());
//			record.setsimilarityScore(getSimilarityScore(record) * numVotes);
//			return record;
			return stateless(result);
		} else {
			previousResult.setsimilarityScore(previousResult.getSimilarityScore() + (getSimilarityScore(record) * numVotes));
			previousResult.setCausalCorrespondences(previousResult.getCausalCorrespondences().append(record.getCausalCorrespondences()));
			return stateless(previousResult);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#createFinalValue(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public Correspondence<TypeA, TypeB> createFinalValue(Pair<TypeA, TypeA> keyValue,
			Correspondence<TypeA, TypeB> result, Object state) {
		
		if(normaliseWith!=0.0) { 
			result.setsimilarityScore(result.getSimilarityScore() / normaliseWith);
		}
		else {
			result.setsimilarityScore(result.getSimilarityScore() / (double)result.getCausalCorrespondences().size());
		}
		
		return super.createFinalValue(keyValue, result, state);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.aggregators.CorrespondenceAggregator#merge(de.uni_mannheim.informatik.dws.winter.model.Pair, de.uni_mannheim.informatik.dws.winter.model.Pair)
	 */
	@Override
	public Pair<Correspondence<TypeA, TypeB>, Object> merge(
			Pair<Correspondence<TypeA, TypeB>, Object> intermediateResult1,
			Pair<Correspondence<TypeA, TypeB>, Object> intermediateResult2) {
		
		Correspondence<TypeA, TypeB> c1 = intermediateResult1.getFirst();
		Correspondence<TypeA, TypeB> c2 = intermediateResult2.getFirst();
		
		Correspondence<TypeA, TypeB> result = new Correspondence<>(
				c1.getFirstRecord(), 
				c1.getSecondRecord(), 
				c1.getSimilarityScore()+c2.getSimilarityScore(), 
				c1.getCausalCorrespondences().append(c2.getCausalCorrespondences())); 
		
		return stateless(result);
	}
}
