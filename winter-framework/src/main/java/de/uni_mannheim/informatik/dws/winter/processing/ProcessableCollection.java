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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.utils.ProgressReporter;

/**
 * 
 * Single-Threaded implementation of {@link Processable} 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ProcessableCollection<RecordType> implements Processable<RecordType> {
	
	private static final long serialVersionUID = 1L;
	protected Collection<RecordType> elements;
	
	public ProcessableCollection() {
		elements = new LinkedList<>();
	}
	
	public ProcessableCollection(Collection<RecordType> elements) {
		if(elements!=null) {
			this.elements = elements;
		} else {
			elements = new LinkedList<>();
		}
	}
	
	public ProcessableCollection(Processable<RecordType> elements) {
		if(elements!=null) {
			this.elements = elements.get();
		} else {
			this.elements = new LinkedList<>();
		}
	}

	public void add(RecordType element) {
		elements.add(element);
	}
	
	public Collection<RecordType> get() {
		return elements;
	}
	
	public int size() {
		return elements.size();
	}
	
	public void merge(Processable<RecordType> other) {
		if(other!=null) {
			for(RecordType elem : other.get()) {
				add(elem);
			}
		}
	}
	
	public void remove(RecordType element) {
		elements.remove(element);
	}
	
	public void remove(Collection<RecordType> element) {
		elements.removeAll(element);
	}
	
	@Override
	public Processable<RecordType> copy() {
		return createProcessableFromCollection(get());
	}
	
	public RecordType firstOrNull() {
		Collection<RecordType> data = get();
		if(data==null || data.size()==0) {
			return null;
		} else {
			return data.iterator().next();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#createProcessable(java.lang.Object)
	 */
	@Override
	public 
	<OutputRecordType> Processable<OutputRecordType> 
	createProcessable(
			OutputRecordType dummyForTypeInference) {
		return new ProcessableCollection<>();
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.Processable#createProcessableFromCollection(java.util.Collection)
	 */
	@Override
	public <OutputRecordType> Processable<OutputRecordType> createProcessableFromCollection(
			Collection<OutputRecordType> data) {
		return new ProcessableCollection<>(data);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#assignUniqueRecordIds(de.uni_mannheim.informatik.dws.winter.processing.Function)
	 */
	@Override
	public 
	Processable<RecordType> 
	assignUniqueRecordIds( 
			Function<RecordType, Pair<Long,RecordType>> assignUniqueId) {
		long id = 0;
		
		Processable<RecordType> result = createProcessable((RecordType)null);
		
		for(RecordType record : get()) {
			RecordType r = assignUniqueId.execute(new Pair<Long, RecordType>(id++, record));
			result.add(r);
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#iterateDataset(de.uni_mannheim.informatik.dws.winter.processing.DatasetIterator)
	 */
	@Override
	public 
	void 
	foreach( 
			DataIterator<RecordType> iterator) {
		
		iterator.initialise();
		
		for(RecordType r : get()) {
			iterator.next(r);
		}
		
		iterator.finalise();
	}
	
	@Override
	public void foreach(Action<RecordType> action) {
		for(RecordType r : get()) {
			action.execute(r);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#transform(de.uni_mannheim.informatik.dws.winter.processing.RecordMapper)
	 */
	@Override
	public 
	<OutputRecordType> 
	Processable<OutputRecordType> 
	map( 
			RecordMapper<RecordType, OutputRecordType> transformation) {
		
		ProgressReporter progress = new ProgressReporter(size(),"");
		
		ProcessableCollector<OutputRecordType> resultCollector = new ProcessableCollector<>();
		
		resultCollector.setResult(createProcessable((OutputRecordType)null));
		
		resultCollector.initialise();
		
		for(RecordType record : get()) {
			transformation.mapRecord(record, resultCollector);
			
			progress.incrementProgress();
			progress.report();
		}
		
		resultCollector.finalise();
		
		return resultCollector.getResult();
	}
	
	/**
	 * Applies the hash function to all records
	 * @param dataset
	 * @param hash
	 * @return A map from the hash value to the respective records
	 */
	protected 
	<KeyType, ElementType> 
	Map<KeyType, List<ElementType>> 
	hashRecords(
			Processable<ElementType> dataset, 
			Function<KeyType, ElementType> hash) {
		HashMap<KeyType, List<ElementType>> hashMap = new HashMap<>();
		
		for(ElementType record : dataset.get()) {
			KeyType key = hash.execute(record);
			
			if(key!=null) {
				List<ElementType> records = hashMap.get(key);
				if(records==null) {
					records = new ArrayList<>();
					hashMap.put(key, records);
				}
				
				records.add(record);
			}
		}
		
		return hashMap;
	}

	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#symmetricJoin(de.uni_mannheim.informatik.dws.winter.processing.Function)
	 */
	@Override
	public 
	<KeyType> 
	Processable<Pair<RecordType,RecordType>> 
	symmetricJoin( Function<KeyType, RecordType> joinKeyGenerator) {
		return symmetricJoin(joinKeyGenerator, new ProcessableCollector<Pair<RecordType, RecordType>>());
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#symmetricJoin(de.uni_mannheim.informatik.dws.winter.processing.Function, de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollector)
	 */
	@Override
	public 
	<KeyType> 
	Processable<Pair<RecordType,RecordType>> 
	symmetricJoin( 
			Function<KeyType, RecordType> joinKeyGenerator, 
			final ProcessableCollector<Pair<RecordType, RecordType>> collector) {
		
		Map<KeyType, List<RecordType>> joinKeys = hashRecords(this, joinKeyGenerator);
		
		collector.setResult(createProcessable((Pair<RecordType,RecordType>)null));
		collector.initialise();
		
		for(List<RecordType> block : joinKeys.values()) {
			for(int i = 0; i < block.size(); i++) {
				for(int j = i+1; j<block.size(); j++) {
					if(i!=j) {
						collector.next(new Pair<>(block.get(i), block.get(j)));
					}
				}
			}
		}
		
		collector.finalise();
		
		return collector.getResult();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#join(de.uni_mannheim.informatik.dws.winter.processing.Processable, de.uni_mannheim.informatik.dws.winter.processing.Function)
	 */
	@Override
	public 
	<KeyType> 
	Processable<Pair<RecordType,RecordType>> 
	join( 
			Processable<RecordType> dataset2, 
			Function<KeyType, RecordType> joinKeyGenerator) {
		return join(dataset2, joinKeyGenerator, joinKeyGenerator);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#join(de.uni_mannheim.informatik.dws.winter.processing.Processable, de.uni_mannheim.informatik.dws.winter.processing.Function, de.uni_mannheim.informatik.dws.winter.processing.Function)
	 */
	@Override
	public 
	<KeyType, RecordType2> 
	Processable<Pair<RecordType,RecordType2>> 
	join(
			Processable<RecordType2> dataset2, 
			Function<KeyType, RecordType> joinKeyGenerator1, 
			Function<KeyType, RecordType2> joinKeyGenerator2) {
		
		final Map<KeyType, List<RecordType>> joinKeys1 = hashRecords(this, joinKeyGenerator1);
		final Map<KeyType, List<RecordType2>> joinKeys2 = hashRecords(dataset2, joinKeyGenerator2);
		
		Processable<Pair<RecordType, RecordType2>> result = createProcessableFromCollection(joinKeys1.keySet()).map(new RecordMapper<KeyType, Pair<RecordType, RecordType2>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(KeyType key1, DataIterator<Pair<RecordType, RecordType2>> resultCollector) {
				List<RecordType> block = joinKeys1.get(key1);
				List<RecordType2> block2 = joinKeys2.get(key1);
				
				if(block2!=null) {
					
					for(RecordType r1 : block) {
						for(RecordType2 r2 : block2) {
							resultCollector.next(new Pair<>(r1, r2));
						}
					}
					
				}
			}
		});
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#leftJoin(de.uni_mannheim.informatik.dws.winter.processing.Processable, de.uni_mannheim.informatik.dws.winter.processing.Function)
	 */
	@Override
	public 
	<KeyType> 
	Processable<Pair<RecordType,RecordType>> 
	leftJoin(
			Processable<RecordType> dataset2, 
			Function<KeyType, RecordType> joinKeyGenerator) {
		return leftJoin(dataset2, joinKeyGenerator, joinKeyGenerator);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#leftJoin(de.uni_mannheim.informatik.dws.winter.processing.Processable, de.uni_mannheim.informatik.dws.winter.processing.Function, de.uni_mannheim.informatik.dws.winter.processing.Function)
	 */
	@Override
	public 
	<KeyType, RecordType2> 
	Processable<Pair<RecordType,RecordType2>> 
	leftJoin(
			Processable<RecordType2> dataset2, 
			Function<KeyType, RecordType> joinKeyGenerator1, 
			Function<KeyType, RecordType2> joinKeyGenerator2) {
		
		Processable<Pair<RecordType, RecordType2>> result = createProcessable((Pair<RecordType, RecordType2>)null);
		
		Map<KeyType, List<RecordType>> joinKeys1 = hashRecords(this, joinKeyGenerator1);
		Map<KeyType, List<RecordType2>> joinKeys2 = hashRecords(dataset2, joinKeyGenerator2);
	
		for(KeyType key1 : joinKeys1.keySet()) {
			List<RecordType> block = joinKeys1.get(key1);
			List<RecordType2> block2 = joinKeys2.get(key1);
			
			for(RecordType r1 : block) {
				
				if(block2!=null) {
					for(RecordType2 r2 : block2) {
						result.add(new Pair<>(r1, r2));
					}
				} else {
					result.add(new Pair<>(r1, (RecordType2)null));
				}
			}
			
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#groupRecords(de.uni_mannheim.informatik.dws.winter.processing.RecordKeyValueMapper)
	 */
	@Override
	public 
	<KeyType, OutputRecordType> 
	Processable<Group<KeyType, OutputRecordType>> 
	group( RecordKeyValueMapper<KeyType, RecordType, OutputRecordType> groupBy) {
				
		GroupCollector<KeyType, OutputRecordType> groupCollector = new GroupCollector<>();
		
		groupCollector.initialise();
		
		for(RecordType r : get()) {
			groupBy.mapRecordToKey(r, groupCollector);
		}
		
		groupCollector.finalise();
		
		return groupCollector.getResult();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#aggregateRecords(de.uni_mannheim.informatik.dws.winter.processing.RecordKeyValueMapper, de.uni_mannheim.informatik.dws.winter.processing.DataAggregator)
	 */
	@Override
	public 
	<KeyType, OutputRecordType, ResultType> 
	Processable<Pair<KeyType, ResultType>> 
	aggregate(
			RecordKeyValueMapper<KeyType, RecordType, OutputRecordType> groupBy, 
			DataAggregator<KeyType, OutputRecordType, ResultType> aggregator) {

		AggregateCollector<KeyType, OutputRecordType, ResultType> aggregateCollector = new AggregateCollector<>();
		
		aggregateCollector.setAggregator(aggregator);
		aggregateCollector.initialise();
		
		ProgressReporter prg = new ProgressReporter(size(), "Aggregating");
		for(RecordType r : get()) {
			groupBy.mapRecordToKey(r, aggregateCollector);
			prg.incrementProgress();
			prg.report();
		}
		
		aggregateCollector.finalise();
		
		return aggregateCollector.getAggregationResult();
	}

	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#sort(de.uni_mannheim.informatik.dws.winter.processing.Function)
	 */
	@Override
	public 
	<KeyType extends Comparable<KeyType>> 
	Processable<RecordType> 
	sort(Function<KeyType, RecordType> sortingKey) {
		return sort(sortingKey, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#sort(de.uni_mannheim.informatik.dws.winter.processing.Function, boolean)
	 */
	@Override
	public 
	<KeyType extends Comparable<KeyType>> 
	Processable<RecordType> 
	sort(
			final Function<KeyType, RecordType> sortingKey, 
			final boolean ascending) {
		ArrayList<RecordType> list = new ArrayList<>(get());
		
		Collections.sort(list, new Comparator<RecordType>() {

			@Override
			public int compare(RecordType o1, RecordType o2) {
				return (ascending ? 1 : -1) * sortingKey.execute(o1).compareTo(sortingKey.execute(o2));
			}
		});
		
		Processable<RecordType> result = createProcessable((RecordType)null);
		for(RecordType elem : list) {
			result.add(elem);
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#filter(de.uni_mannheim.informatik.dws.winter.processing.Function)
	 */
	@Override
	public 
	Processable<RecordType> 
	where(Function<Boolean, RecordType> criteria) {
		Processable<RecordType> result = createProcessable((RecordType)null);
		
		for(RecordType element : get()) {
			if(criteria.execute(element)) {
				result.add(element);
			}
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#coGroup(de.uni_mannheim.informatik.dws.winter.processing.Processable, de.uni_mannheim.informatik.dws.winter.processing.Function, de.uni_mannheim.informatik.dws.winter.processing.Function, de.uni_mannheim.informatik.dws.winter.processing.RecordMapper)
	 */
	@Override
	public 
	<KeyType, RecordType2, OutputRecordType> 
	Processable<OutputRecordType> 
	coGroup( 
			Processable<RecordType2> data2, 
			final Function<KeyType, RecordType> groupingKeyGenerator1, 
			final Function<KeyType, RecordType2> groupingKeyGenerator2, 
			final RecordMapper<Pair<Iterable<RecordType>, Iterable<RecordType2>>, OutputRecordType> resultMapper) {
		 Processable<Group<KeyType, RecordType>> group1 = group(new RecordKeyValueMapper<KeyType, RecordType, RecordType>() {

			private static final long serialVersionUID = 1L;
		
			@Override
			public void mapRecordToKey(RecordType record, DataIterator<Pair<KeyType, RecordType>> resultCollector) {
				resultCollector.next(new Pair<KeyType, RecordType>(groupingKeyGenerator1.execute(record), record));
			}
		});
		
		 Processable<Group<KeyType, RecordType2>> group2 = data2.group(new RecordKeyValueMapper<KeyType, RecordType2, RecordType2>() {

			private static final long serialVersionUID = 1L;
		
			@Override
			public void mapRecordToKey(RecordType2 record, DataIterator<Pair<KeyType, RecordType2>> resultCollector) {
				resultCollector.next(new Pair<KeyType, RecordType2>(groupingKeyGenerator2.execute(record), record));
			}
		});
		 
		 Processable<Pair<Group<KeyType, RecordType>, Group<KeyType, RecordType2>>> joined = group1.join(group2, new Function<KeyType, Group<KeyType, RecordType>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public KeyType execute(Group<KeyType, RecordType> input) {
				return input.getKey();
			}
		},new Function<KeyType, Group<KeyType, RecordType2>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public KeyType execute(Group<KeyType, RecordType2> input) {
				return (KeyType)input.getKey();
			}
		});
		
		 return joined.map(new RecordMapper<Pair<Group<KeyType, RecordType>, Group<KeyType, RecordType2>>, OutputRecordType>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(Pair<Group<KeyType, RecordType>, Group<KeyType, RecordType2>> record,
					DataIterator<OutputRecordType> resultCollector) {
				resultMapper.mapRecord(new Pair<Iterable<RecordType>, Iterable<RecordType2>>(record.getFirst().getRecords().get(), record.getSecond().getRecords().get()), resultCollector);
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#append(de.uni_mannheim.informatik.dws.winter.processing.Processable)
	 */
	@Override
	public 
	Processable<RecordType> 
	append(Processable<RecordType> data2) {
		Processable<RecordType> result = createProcessable((RecordType)null);
		
		for(RecordType r : get()) {
			result.add(r);
		}
		
		if(data2!=null) {
			for(RecordType r : data2.get()) {
				result.add(r);
			}
		}

		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#distinct()
	 */
	@Override
	public 
	Processable<RecordType> 
	distinct() {
		return createProcessableFromCollection(new ArrayList<>(new HashSet<>(get())));
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.Processable#take(int)
	 */
	@Override
	public 
	Processable<RecordType>
	take(int numberOfRecords) {
		Processable<RecordType> result = createProcessable((RecordType)null);
		
		Iterator<RecordType> it = get().iterator();
		
		while(it.hasNext() && result.size() < numberOfRecords) {
			result.add(it.next());
		}
		
		return result;
	}
}
