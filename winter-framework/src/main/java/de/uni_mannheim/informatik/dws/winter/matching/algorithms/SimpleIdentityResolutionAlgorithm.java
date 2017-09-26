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

import de.uni_mannheim.informatik.dws.winter.matching.aggregators.CorrespondenceAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.Blocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.IdentityMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * 
 * Implementation of instance-based schema matching: the schemas are matched by comparing the values of the schema elements.
 * 
 * A blocker is used to determine the values for the schema elements, an aggregator calculates a similarity score based on the value overlap.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class SimpleIdentityResolutionAlgorithm<RecordType extends Matchable, SchemaElementType extends Matchable> 
	implements MatchingAlgorithm<RecordType, MatchableValue> {

	public SimpleIdentityResolutionAlgorithm(DataSet<RecordType, SchemaElementType> dataset1,
			DataSet<RecordType, SchemaElementType> dataset2,
			Blocker<RecordType, SchemaElementType, RecordType, MatchableValue> blocker,
			CorrespondenceAggregator<RecordType, MatchableValue> aggregator) {
		this.dataset1 = dataset1;
		this.dataset2 = dataset2;
		this.blocker = blocker;
		this.aggregator = aggregator;
	}
	
	DataSet<RecordType, SchemaElementType> dataset1;
	DataSet<RecordType, SchemaElementType> dataset2;
	Blocker<RecordType, SchemaElementType, RecordType, MatchableValue> blocker;
	CorrespondenceAggregator<RecordType, MatchableValue> aggregator;
	Processable<Correspondence<RecordType, MatchableValue>> result;
	
	/**
	 * @return the dataset1
	 */
	public DataSet<RecordType, SchemaElementType> getDataset1() {
		return dataset1;
	}
	/**
	 * @return the dataset2
	 */
	public DataSet<RecordType, SchemaElementType> getDataset2() {
		return dataset2;
	}
	
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.algorithms.MatchingAlgorithm#run()
	 */
	@Override
	public void run() {
		// run the blocker to generate initial correspondences between the schema elements 
		Processable<Correspondence<RecordType, MatchableValue>> blocked = blocker.runBlocking(getDataset1(), getDataset2(), null);
		
		// aggregate the correspondences to calculate a similarity score
		Processable<Pair<Pair<RecordType, RecordType>, Correspondence<RecordType, MatchableValue>>> aggregated = blocked.aggregate(new IdentityMatchingRule<RecordType, MatchableValue>(0.0), aggregator);
		
		// transform the result to the expected correspondence format
		Processable<Correspondence<RecordType, MatchableValue>> result = aggregated.map((p, collector) -> {
			if(p.getSecond()!=null)
			{
				collector.next(p.getSecond());
			}
		});
	
		setResult(result);
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Processable<Correspondence<RecordType, MatchableValue>> result) {
		this.result = result;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.algorithms.MatchingAlgorithm#getResult()
	 */
	@Override
	public Processable<Correspondence<RecordType, MatchableValue>> getResult() {
		return result;
	}

}
