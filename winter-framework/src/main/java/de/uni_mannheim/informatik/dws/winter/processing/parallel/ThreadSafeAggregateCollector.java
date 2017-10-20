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

import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.AggregateCollector;
import de.uni_mannheim.informatik.dws.winter.processing.DataAggregator;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.ProgressReporter;
import de.uni_mannheim.informatik.dws.winter.utils.parallel.ThreadBoundObject;

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
	private ThreadBoundObject<Map<KeyType, Pair<ResultType,Object>>> intermediateResults;
	private Processable<Pair<KeyType, ResultType>> aggregationResult;
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.GroupCollector#initialise()
	 */
	@Override
	public void initialise() {
		super.initialise();
		intermediateResults = new ThreadBoundObject<>((t)->new HashMap<>());
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
		
		Pair<ResultType,Object> value = intermediateResults.get().get(record.getFirst());
		
		if(value==null) {
			value = aggregator.initialise(record.getFirst());
		}
		
		value = aggregator.aggregate(value.getFirst(), record.getSecond(), value.getSecond());
		
		intermediateResults.get().put(record.getFirst(), value);		
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.GroupCollector#finalise()
	 */
	@Override
	public void finalise() {
		
		Map<KeyType, Pair<ResultType,Object>> results = null;
		
		ProgressReporter prg = new ProgressReporter(intermediateResults.getAll().size(), "[ThreadSafeAggregateCollector] Merging partial results");
		for(Map<KeyType, Pair<ResultType,Object>> partialResult : intermediateResults.getAll()) {
			if(results==null) {
				results = partialResult;
			} else {
				for(Map.Entry<KeyType, Pair<ResultType,Object>> entry : partialResult.entrySet()) {
					Pair<ResultType,Object> value = results.get(entry.getKey());
					
					if(value==null) {
						value = entry.getValue();
					} else {
						value = aggregator.merge(value, entry.getValue());						
					}
					
					results.put(entry.getKey(), value);
				}
			}
			
			prg.incrementProgress();
			prg.report();
		}
		
		if(results!=null) {
			
			aggregationResult = new ParallelProcessableCollection<>(results.entrySet()).map(
					(Map.Entry<KeyType, Pair<ResultType, Object>> record, DataIterator<Pair<KeyType,ResultType>> resultCollector) 
					-> {
						KeyType key = record.getKey();
						Pair<ResultType, Object> value = record.getValue();
						
						resultCollector.next(new Pair<>(key, aggregator.createFinalValue(key, value.getFirst(), value.getSecond())));
					});
		}
	}
	
}
