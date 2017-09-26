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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Action;
import de.uni_mannheim.informatik.dws.winter.processing.AggregateCollector;
import de.uni_mannheim.informatik.dws.winter.processing.DataAggregator;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Function;
import de.uni_mannheim.informatik.dws.winter.processing.Group;
import de.uni_mannheim.informatik.dws.winter.processing.GroupCollector;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollector;
import de.uni_mannheim.informatik.dws.winter.processing.RecordKeyValueMapper;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;
import de.uni_mannheim.informatik.dws.winter.utils.parallel.Consumer;
import de.uni_mannheim.informatik.dws.winter.utils.parallel.Parallel;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * 
 * Multi-threaded implementation of {@link Processable}
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ParallelProcessableCollection<RecordType> extends ProcessableCollection<RecordType> {
	
	private static final long serialVersionUID = 1L;

	public ParallelProcessableCollection() {
		super(new ConcurrentLinkedQueue<RecordType>());
	}
	
	public ParallelProcessableCollection(Collection<RecordType> elements) {
		super(new ConcurrentLinkedQueue<RecordType>(elements));
	}
	
	public ParallelProcessableCollection(Processable<RecordType> elements) {
		super(new ConcurrentLinkedQueue<RecordType>(elements.get()));
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine#createResultSet(java.lang.Object)
	 */
	@Override
	public  <OutputRecordType> Processable<OutputRecordType> createProcessable(OutputRecordType dummyForTypeInference) {
		return new ParallelProcessableCollection<>();
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.ProcessableCollection#createProcessableFromCollection(java.util.Collection)
	 */
	@Override
	public <OutputRecordType> Processable<OutputRecordType> createProcessableFromCollection(
			Collection<OutputRecordType> data) {
		return new ParallelProcessableCollection<>(data);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine#iterateDataset(de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void foreach(final DataIterator<RecordType> iterator) {
		iterator.initialise();
		
		new Parallel<RecordType>().tryForeach(get(), new Consumer<RecordType>() {

			@Override
			public void execute(RecordType parameter) {
				iterator.next(parameter);
			}
		});
		iterator.finalise();
	}
	
	@Override
	public void foreach(Action<RecordType> action) {
		new Parallel<RecordType>().tryForeach(get(), (r)->action.execute(r));
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine#transform(de.uni_mannheim.informatik.wdi.model.BasicCollection, de.uni_mannheim.informatik.wdi.processing.RecordMapper)
	 */
	@Override
	public <OutputRecordType> Processable<OutputRecordType> map(final RecordMapper<RecordType, OutputRecordType> transformation) {
		final ProcessableCollector<OutputRecordType> resultCollector = new ProcessableCollector<>();
		
		resultCollector.setResult(createProcessable((OutputRecordType)null));
		
		resultCollector.initialise();
		
		new Parallel<RecordType>().tryForeach(get(), new Consumer<RecordType>() {

			@Override
			public void execute(RecordType parameter) {
				transformation.mapRecord(parameter, resultCollector);
			}
			
		});
		
		resultCollector.finalise();
		
		return resultCollector.getResult();
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine#groupRecords(de.uni_mannheim.informatik.wdi.model.BasicCollection, de.uni_mannheim.informatik.wdi.processing.RecordMapper)
	 */
	@Override
	public <KeyType, OutputRecordType> Processable<Group<KeyType, OutputRecordType>> group(
			final RecordKeyValueMapper<KeyType, RecordType, OutputRecordType> groupBy) {
		
		final GroupCollector<KeyType, OutputRecordType> groupCollector = new ThreadSafeGroupCollector<>();
		
		groupCollector.initialise();
		
		new Parallel<RecordType>().tryForeach(get(), new Consumer<RecordType>() {

			@Override
			public void execute(RecordType parameter) {
				groupBy.mapRecordToKey(parameter, groupCollector);
			}
		});
		
		groupCollector.finalise();
		
		return groupCollector.getResult();
	}
	
	@Override
	public 
	<KeyType, OutputRecordType, ResultType> 
	Processable<Pair<KeyType, ResultType>> 
	aggregate( 
			final RecordKeyValueMapper<KeyType, RecordType, OutputRecordType> groupBy, 
			DataAggregator<KeyType, OutputRecordType, ResultType> aggregator) {

		final AggregateCollector<KeyType, OutputRecordType, ResultType> aggregateCollector = new ThreadSafeAggregateCollector<>();
		
		aggregateCollector.setAggregator(aggregator);
		aggregateCollector.initialise();
		
		new Parallel<RecordType>().tryForeach(get(), new Consumer<RecordType>() {

			@Override
			public void execute(RecordType parameter) {
				groupBy.mapRecordToKey(parameter, aggregateCollector);
			}
		});
		
		aggregateCollector.finalise();
		
		return aggregateCollector.getAggregationResult();
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine#hashRecords(de.uni_mannheim.informatik.wdi.model.BasicCollection, de.uni_mannheim.informatik.wdi.processing.Function)
	 */
	@Override
	protected <KeyType, ElementType> Map<KeyType, List<ElementType>> hashRecords(Processable<ElementType> dataset,
			final Function<KeyType, ElementType> hash) {
		final ConcurrentHashMap<KeyType, List<ElementType>> hashMap = new ConcurrentHashMap<>(dataset.size());
		
		new Parallel<ElementType>().tryForeach(dataset.get(), new Consumer<ElementType>() {

			@Override
			public void execute(ElementType record) {
				KeyType key = hash.execute(record);
				
				if(key!=null) {
					hashMap.putIfAbsent(key, Collections.synchronizedList(new LinkedList<ElementType>()));
					
					List<ElementType> records = hashMap.get(key);
					
					records.add(record);
				}
			}
		});
		
		for(KeyType key : hashMap.keySet()) {
			hashMap.put(key, new ArrayList<>(hashMap.get(key)));
		}
		
		return hashMap;
	}
	
	@Override
	public <KeyType> Processable<Pair<RecordType,RecordType>> symmetricJoin( 
			final Function<KeyType, RecordType> joinKeyGenerator,
			final ProcessableCollector<Pair<RecordType, RecordType>> collector) {
		
		final Map<KeyType, List<RecordType>> joinKeys = hashRecords(this, joinKeyGenerator);
		
		collector.setResult(createProcessable((Pair<RecordType, RecordType>)null));
		collector.initialise();
		
		List<Pair<List<RecordType>, Integer[]>> tasks = new LinkedList<>();
		int idx = 0;
		for(List<RecordType> block : Q.sort(joinKeys.values(), new Comparator<List<RecordType>>() {

			@Override
			public int compare(List<RecordType> o1, List<RecordType> o2) {
				return Integer.compare(o1.size(), o2.size());
			}
		})) {
			
			if(idx++==joinKeys.values().size()-1) {
			
				// split the largest hash bucket into smaller parts, such that it can be distributed among more processors
				// in cases where few very large hash buckets exists (fewer than number of processors), they will take quite long to process and all other processors will be idle during that time
				// so we split the largest bucket to make sure all processors are busy
				
				int startIndex = 0;
				
				if(block.size()%2!=0) {
					Pair<List<RecordType>, Integer[]> task = new Pair<List<RecordType>, Integer[]>(block, new Integer[] { 0 });
					tasks.add(task);
					startIndex++;
				}
				
				for(int i = startIndex; i < block.size()/2; i++) {
					Pair<List<RecordType>, Integer[]> task = new Pair<List<RecordType>, Integer[]>(block, new Integer[] { i, block.size() - i - 1 + startIndex });
					tasks.add(task);
				}
			
			} else {				
				Pair<List<RecordType>, Integer[]> task = new Pair<List<RecordType>, Integer[]>(block, null);
				tasks.add(task);
				
			}
		}
		
		new Parallel<Pair<List<RecordType>, Integer[]>>().tryForeach(tasks, new Consumer<Pair<List<RecordType>, Integer[]>>() {

			@Override
			public void execute(Pair<List<RecordType>, Integer[]> task) {
				
				if(task.getSecond()!=null) {
					for(int i : task.getSecond()) {
						for(int j = i+1; j<task.getFirst().size(); j++) {
							if(i!=j) {
								collector.next(new Pair<>(task.getFirst().get(i), task.getFirst().get(j)));
							}
						}
					}
				} else {
					for(int i = 0; i < task.getFirst().size(); i++) {
						for(int j = i+1; j<task.getFirst().size(); j++) {
							if(i!=j) {
								collector.next(new Pair<>(task.getFirst().get(i), task.getFirst().get(j)));
							}
						}
					}					
				}
				
			}
		});
	
		collector.finalise();
		
		return collector.getResult();
	}
}
