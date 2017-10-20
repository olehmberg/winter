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
package de.uni_mannheim.informatik.dws.winter.processing;

import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.Pair;

/**
 * 
 * A group collector that aggregates the elements of a group
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class AggregateCollector<KeyType, RecordType, ResultType> extends GroupCollector<KeyType, RecordType>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<KeyType, Pair<ResultType,Object>> intermediateResults;
	private Processable<Pair<KeyType, ResultType>> aggregationResult;
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.GroupCollector#initialise()
	 */
	@Override
	public void initialise() {
		super.initialise();
		intermediateResults = new HashMap<>();
		aggregationResult = new ProcessableCollection<>();
	}
	
	/**
	 * @param aggregationResult the aggregationResult to set
	 */
	protected void setAggregationResult(
			Processable<Pair<KeyType, ResultType>> aggregationResult) {
		this.aggregationResult = aggregationResult;
	}
	/**
	 * @return the aggregationResult
	 */
	public Processable<Pair<KeyType, ResultType>> getAggregationResult() {
		return aggregationResult;
	}

	private DataAggregator<KeyType, RecordType, ResultType> aggregator;
	/**
	 * @param aggregator the aggregator to set
	 */
	public void setAggregator(DataAggregator<KeyType, RecordType, ResultType> aggregator) {
		this.aggregator = aggregator;
	}
	
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.GroupCollector#next(de.uni_mannheim.informatik.wdi.model.Pair)
	 */
	@Override
	public void next(Pair<KeyType, RecordType> record) {
		Pair<ResultType,Object> result = intermediateResults.get(record.getFirst());
		
		if(result==null) {
			result = aggregator.initialise(record.getFirst());
		}
		
		result = aggregator.aggregate(result.getFirst(), record.getSecond(), result.getSecond());
		intermediateResults.put(record.getFirst(), result);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.GroupCollector#finalise()
	 */
	@Override
	public void finalise() {
		for(KeyType key : intermediateResults.keySet()) {
			Pair<ResultType, Object> value = intermediateResults.get(key);
			ResultType result = aggregator.createFinalValue(key, value.getFirst(), value.getSecond());
			if(result!=null) {
				aggregationResult.add(new Pair<>(key, result));
			}
		}
	}
}
