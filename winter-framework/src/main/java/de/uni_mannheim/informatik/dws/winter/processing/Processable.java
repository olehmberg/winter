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

import java.io.Serializable;
import java.util.Collection;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.parallel.ParallelProcessableCollection;

/**
 * 
 * Interface for all processable collections. Specifies many data processing operations that are used throughout the framework.
 * Implementing classes decide how to perform the operations, which allows for a replaceable processing environment.
 * 
 * Default implementations are {@link ProcessableCollection} for single-threaded and {@link ParallelProcessableCollection} for multi-threaded processing.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType> the type of elements in this processable
 */
public interface Processable<RecordType> extends Serializable {

	/**
	 * Adds an entry to this collection.
	 * 
	 * @param element	the element that should be added
	 */
	void add(RecordType element);

	/**
	 * @return Returns a collection with all entries of this data set.
	 */
	Collection<RecordType> get();
	
	/**
	 * @return Returns the number of entries in this data set.
	 */
	int size();
		
	public void remove(RecordType element);
	public void remove(Collection<RecordType> element);	
	
	public Processable<RecordType> copy();
	
	/**
	 * 
	 * @return Returns the first element or null if no elements exist
	 */
	public RecordType firstOrNull();
	
	/**
	 * Creates a new {@link Processable} for the type given as parameter.
	 * @param dummyForTypeInference	a dummy variable that is used to infer the type parameter
	 * @return a new {@link Processable}
	 */
	<OutputRecordType> Processable<OutputRecordType> createProcessable(OutputRecordType dummyForTypeInference);

	/**
	 * Creates a new {@link Processable} with the provided elements as content
	 * @param data	the data from which the new processable should be created
	 * @return a new {@link Processable}
	 */
	<OutputRecordType> Processable<OutputRecordType> createProcessableFromCollection(Collection<OutputRecordType> data);
	
	/**
	 * Executes the given function for every record and provides a unique long value for each call, effectively allowing the assignment of unique ids to all records.
	 * 
	 * @param assignUniqueId	a function that creates the unique ids
	 * @return A {@link Processable} with the result of the operation
	 */
	Processable<RecordType> assignUniqueRecordIds(Function<RecordType, Pair<Long, RecordType>> assignUniqueId);

	/**
	 * Iterates over the given dataset without producing a result
	 * @param iterator	the iterator that should be executed
	 */
	void foreach(DataIterator<RecordType> iterator);

	/**
	 * Iterates over the processable and executes the provided action for each element.
	 * @param action	the action that should be executed
	 */
	void foreach(Action<RecordType> action);
	
	/**
	 * Iterates over all elements and produces a result
	 * @param transformation	the transformation that should be applied
	 * @return A {@link Processable} with the result of the operation
	 */
	<OutputRecordType> Processable<OutputRecordType> map(RecordMapper<RecordType, OutputRecordType> transformation);

	/**
	 * Joins the data to itself via the provided joinKeyGenerator (inner join). Assumes that the join is symmetric, i.e., a result a/b is equal to b/a and hence only a/b is created.
	 * @param joinKeyGenerator	a function that returns the join key for each element
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType> Processable<Pair<RecordType, RecordType>> symmetricJoin(Function<KeyType, RecordType> joinKeyGenerator);

	/**
	 * Joins the data to itself via the provided joinKeyGenerator (inner join). Assumes that the join is symmetric, i.e., a result a/b is equal to b/a and hence only a/b is created.
	 * @param joinKeyGenerator	a function that returns the join key for each element
	 * @param collector
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType> Processable<Pair<RecordType, RecordType>> symmetricJoin(Function<KeyType, RecordType> joinKeyGenerator, ProcessableCollector<Pair<RecordType, RecordType>> collector);

	/**
	 * Joins two processables using the provided joinKeyGenerator (inner join).
	 * @param dataset2
	 * @param joinKeyGenerator
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType> Processable<Pair<RecordType, RecordType>> join(Processable<RecordType> dataset2, Function<KeyType, RecordType> joinKeyGenerator);

	/**
	 * 
	 * Joins two processables using the provided joinKeyGenerators (inner join).
	 * 
	 * @param dataset2
	 * @param joinKeyGenerator1
	 * @param joinKeyGenerator2
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType, RecordType2> Processable<Pair<RecordType, RecordType2>> join(Processable<RecordType2> dataset2,
			Function<KeyType, RecordType> joinKeyGenerator1, Function<KeyType, RecordType2> joinKeyGenerator2);

	/**
	 * Joins two processables using the provided joinKeyGenerator (left join).
	 * @param dataset2
	 * @param joinKeyGenerator
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType> Processable<Pair<RecordType, RecordType>> leftJoin(Processable<RecordType> dataset2,
			Function<KeyType, RecordType> joinKeyGenerator);
	
	/**
	 * 
	 * Joins two processables using the provided joinKeyGenerators (left join).
	 * 
	 * @param dataset2
	 * @param joinKeyGenerator1
	 * @param joinKeyGenerator2
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType, RecordType2> Processable<Pair<RecordType, RecordType2>> leftJoin(Processable<RecordType2> dataset2,
			Function<KeyType, RecordType> joinKeyGenerator1, Function<KeyType, RecordType2> joinKeyGenerator2);

	/***
	 * 
	 * Groups records based on the given groupBy mapper.
	 * 
	 * KeyType = Type of the grouping key
	 * RecordType = Type of the input records
	 * OutputType = Type of the records in the resulting groups (can be the same as RecordType)
	 * 
	 * @param groupBy
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType, OutputRecordType> Processable<Group<KeyType, OutputRecordType>> group(RecordKeyValueMapper<KeyType, RecordType, OutputRecordType> groupBy);

	/**
	 * 
	 * Groups records based on the given groupBy mapper and then aggregates the groups using the aggregator.
	 * 
	 * @param groupBy
	 * @param aggregator
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType, OutputRecordType, ResultType> Processable<Pair<KeyType, ResultType>> aggregate(
			RecordKeyValueMapper<KeyType, RecordType, OutputRecordType> groupBy,
			DataAggregator<KeyType, OutputRecordType, ResultType> aggregator);

	/**
	 * Sorts the given records.
	 * @param sortingKey
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType extends Comparable<KeyType>> Processable<RecordType> sort(Function<KeyType, RecordType> sortingKey);

	/**
	 * Sorts the given records.
	 * @param sortingKey
	 * @param ascending
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType extends Comparable<KeyType>> Processable<RecordType> sort(Function<KeyType, RecordType> sortingKey, boolean ascending);

	/**
	 * Filters the given data. Only keeps elements where criteria evaluates to true.
	 * @param criteria
	 * @return A {@link Processable} with the result of the operation
	 */
	Processable<RecordType> where(Function<Boolean, RecordType> criteria);

	/**
	 * co-groups two datasets
	 * @param data2
	 * @param groupingKeyGenerator1
	 * @param groupingKeyGenerator2
	 * @param resultMapper
	 * @return A {@link Processable} with the result of the operation
	 */
	<KeyType, RecordType2, OutputRecordType> Processable<OutputRecordType> coGroup(
			Processable<RecordType2> data2,
			Function<KeyType, RecordType> groupingKeyGenerator1, Function<KeyType, RecordType2> groupingKeyGenerator2,
			RecordMapper<Pair<Iterable<RecordType>, Iterable<RecordType2>>, OutputRecordType> resultMapper);

	/**
	 * appends the given data to this data
	 * @param data2
	 * @return A {@link Processable} with the result of the operation
	 */
	Processable<RecordType> append(Processable<RecordType> data2);

	/**
	 * returns only distinct elements (based on hashCode and equals)
	 * @return A {@link Processable} with the result of the operation
	 */
	Processable<RecordType> distinct();

	/**
	 * returns the first numberOfRecords records.
	 * @param numberOfRecords
	 * @return A {@link Processable} with the result of the operation
	 */
	Processable<RecordType> take(int numberOfRecords);

}