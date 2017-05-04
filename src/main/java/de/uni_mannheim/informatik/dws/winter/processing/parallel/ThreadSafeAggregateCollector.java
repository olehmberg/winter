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
package de.uni_mannheim.informatik.dws.winter.processing.parallel;

import java.util.concurrent.ConcurrentHashMap;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.AggregateCollector;
import de.uni_mannheim.informatik.dws.winter.processing.DataAggregator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Thread-Safe implementation of {@link AggregateCollector}.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ThreadSafeAggregateCollector<KeyType, RecordType, ResultType> extends AggregateCollector<KeyType, RecordType, ResultType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConcurrentHashMap<KeyType, LockableValue<ResultType>> intermediateResults;
	private Processable<Pair<KeyType, ResultType>> aggregationResult;
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.GroupCollector#initialise()
	 */
	@Override
	public void initialise() {
		super.initialise();
		intermediateResults = new ConcurrentHashMap<>();
		aggregationResult = new ParallelProcessableCollection<>();
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
		
		LockableValue<ResultType> value = intermediateResults.get(record.getFirst());
		
		if(value==null) {
			intermediateResults.putIfAbsent(record.getFirst(), new LockableValue<ResultType>());
			value = intermediateResults.get(record.getFirst());
		}
		
		synchronized (value) {
			
			if(value.getValue()==null) {
				value.setValue(aggregator.initialise(record.getFirst()));
			}
			
			value.setValue(aggregator.aggregate(value.getValue(), record.getSecond()));
			intermediateResults.put(record.getFirst(), value);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.GroupCollector#finalise()
	 */
	@Override
	public void finalise() {
		for(KeyType key : intermediateResults.keySet()) {
			aggregationResult.add(new Pair<>(key, aggregator.createFinalValue(key, intermediateResults.get(key).getValue())));
		}
	}
	
}
