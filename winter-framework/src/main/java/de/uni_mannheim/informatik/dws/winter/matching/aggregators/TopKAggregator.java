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
import de.uni_mannheim.informatik.dws.winter.processing.DataAggregator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;

/**
 * 
 * An aggregation function that keeps the k correspondences with the highest similarity score.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TopKAggregator<TypeA extends Matchable, TypeB extends Matchable, KeyType>
	implements DataAggregator<KeyType, Correspondence<TypeA,TypeB>, Processable<Correspondence<TypeA,TypeB>>> 
{

	private static final long serialVersionUID = 1L;

	private int k;
	
	public TopKAggregator(int k) {
		this.k = k;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#initialise(java.lang.Object)
	 */
	@Override
	public Pair<Processable<Correspondence<TypeA, TypeB>>, Object> initialise(KeyType keyValue) {
		return stateless(new ProcessableCollection<>());
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#aggregate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Pair<Processable<Correspondence<TypeA, TypeB>>, Object> aggregate(Processable<Correspondence<TypeA, TypeB>> previousResult,
			Correspondence<TypeA, TypeB> record, Object state) {

		previousResult.add(record);
		
		if(k>0) {
			// first create a fixed order for the input data to make this step reproducible (in cases where two correspondences have the same similarity value)
			// then sort by similarity score
			
			previousResult = previousResult.sort((r)->r.getIdentifiers()).sort((r)->r.getSimilarityScore(), false).take(k);
		}
		
		return stateless(previousResult);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.DataAggregator#merge(de.uni_mannheim.informatik.dws.winter.model.Pair, de.uni_mannheim.informatik.dws.winter.model.Pair)
	 */
	@Override
	public Pair<Processable<Correspondence<TypeA, TypeB>>, Object> merge(
			Pair<Processable<Correspondence<TypeA, TypeB>>, Object> intermediateResult1,
			Pair<Processable<Correspondence<TypeA, TypeB>>, Object> intermediateResult2) {
		return stateless(intermediateResult1.getFirst().append(intermediateResult2.getFirst()).sort((r)->r.getIdentifiers()).sort((r)->r.getSimilarityScore(), false).take(k));
	}
}
