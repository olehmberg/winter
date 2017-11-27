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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	
	public Collection<Collection<RecordType>> partitionRecords() {
		// create more partitions than available threads so we can compensate for partitions which create less workload than others (so no thread runs idle)
		int numPartitions = (Runtime.getRuntime().availableProcessors() * 10);
		int partitionSize = (int)Math.floor(size() / numPartitions);
		
		List<Collection<RecordType>> partitions = new LinkedList<>();
		for(int i = 0; i < numPartitions; i++) {
			partitions.add(new LinkedList<>());
		}
		int pIdx = 0;
		
//		Collection<RecordType> partition = new LinkedList<>();
		
		Iterator<RecordType> it = get().iterator();
		
		while(it.hasNext()) {
			partitions.get(pIdx++).add(it.next());
			
			if(pIdx==numPartitions) {
				pIdx=0;
			}
//			
//			partition.add(it.next());
//			
//			if(partition.size()==partitionSize) {
//				partitions.add(partition);
//				partition = new LinkedList<>();
//			}
		}
//		if(partition.size()>0) {
//			partitions.add(partition);
//		}
		
		return partitions;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine#transform(de.uni_mannheim.informatik.wdi.model.BasicCollection, de.uni_mannheim.informatik.wdi.processing.RecordMapper)
	 */
	@Override
	public <OutputRecordType> Processable<OutputRecordType> map(final RecordMapper<RecordType, OutputRecordType> transformation) {
		final ProcessableCollector<OutputRecordType> resultCollector = new ThreadSafeProcessableCollector<>();
		
		resultCollector.setResult(createProcessable((OutputRecordType)null));
		
		resultCollector.initialise();
		
		new Parallel<Collection<RecordType>>().tryForeach(partitionRecords(), new Consumer<Collection<RecordType>>() {

			@Override
			public void execute(Collection<RecordType> parameter) {
				for(RecordType r : parameter) {
					transformation.mapRecord(r, resultCollector);
				}
			}
			
		}, "ParallelProcessableCollection.map");

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
		
		new Parallel<Collection<RecordType>>().tryForeach(partitionRecords(), new Consumer<Collection<RecordType>>() {

			@Override
			public void execute(Collection<RecordType> parameter) {
				for(RecordType r : parameter) {
					groupBy.mapRecordToKey(r, groupCollector);
				}
			}
		}, "ParallelProcessableCollection.group");
		
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
		
		new Parallel<Collection<RecordType>>().tryForeach(partitionRecords(), new Consumer<Collection<RecordType>>() {

			@Override
			public void execute(Collection<RecordType> parameter) {
				for(RecordType record : parameter) {
					groupBy.mapRecordToKey(record, aggregateCollector);
				}
			}
		}, "ParallelProcessableCollection.aggregate");
		
		aggregateCollector.finalise();
		
		return aggregateCollector.getAggregationResult();
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine#hashRecords(de.uni_mannheim.informatik.wdi.model.BasicCollection, de.uni_mannheim.informatik.wdi.processing.Function)
	 */
	@Override
	protected <KeyType, ElementType> Map<KeyType, List<ElementType>> hashRecords(Processable<ElementType> dataset,
			final Function<KeyType, ElementType> hash) {
		
		Processable<Group<KeyType, ElementType>> hashed = dataset.group((ElementType record, DataIterator<Pair<KeyType, ElementType>> resultCollector) -> resultCollector.next(new Pair<>(hash.execute(record), record)));
		
		Map<KeyType, List<ElementType>> hashMap = new HashMap<>();
		
		for(Group<KeyType, ElementType> group : hashed.get()) {
			hashMap.put(group.getKey(), new ArrayList<>(group.getRecords().get()));
		}
		
		return hashMap;
	}
	
	@Override
	public <KeyType, RecordType2> Processable<Pair<RecordType, RecordType2>> join(Processable<RecordType2> dataset2,
			Function<KeyType, RecordType> joinKeyGenerator1, Function<KeyType, RecordType2> joinKeyGenerator2) {
		
		// partition this dataset into num_processors partitions
		final Map<KeyType, List<RecordType2>> joinKeys2 = hashRecords(dataset2, joinKeyGenerator2);
		
		Processable<Pair<RecordType, RecordType2>> result = map(new RecordMapper<RecordType, Pair<RecordType, RecordType2>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(RecordType record, DataIterator<Pair<RecordType, RecordType2>> resultCollector) {
				
				KeyType thisKey = joinKeyGenerator1.execute(record);
				List<RecordType2> matches = joinKeys2.get(thisKey);
				
				if(matches!=null) {
					
					for(RecordType2 r2 : matches) {
						resultCollector.next(new Pair<>(record, r2));
					}
					
				}	
			}
		});
		
		return result;
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
