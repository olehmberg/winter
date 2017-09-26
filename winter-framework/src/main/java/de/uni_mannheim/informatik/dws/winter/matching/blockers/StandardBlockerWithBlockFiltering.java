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
package de.uni_mannheim.informatik.dws.winter.matching.blockers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.LeftIdentityPair;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Function;
import de.uni_mannheim.informatik.dws.winter.processing.Group;
import de.uni_mannheim.informatik.dws.winter.processing.PairFirstJoinKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;
import de.uni_mannheim.informatik.dws.winter.processing.aggregators.DistributionAggregator;
import de.uni_mannheim.informatik.dws.winter.utils.Distribution;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * Implementation of a standard {@link AbstractBlocker} based on blocking keys. All records for which the same blocking key is generated are returned as pairs.
 * After building the block, block filtering is applied to remove redundant and superfluous comparisons.
 * Block filtering is implemented based on the 'Scaling Entity Resolution to Large, Heterogeneous Data with Enhanced Meta-blocking' paper by Papadakis et al. in 2016: dx.doi.org/10.5441/002/edbt.2016.22
 * Based on the StandardBlocker class.
 *
 * @author Daniel Ringler
 * 
 * @param <RecordType>
 * @param <SchemaElementType>
 * @param <BlockedType>
 * @param <CorrespondenceType>
 */
public class StandardBlockerWithBlockFiltering<RecordType extends Matchable, SchemaElementType extends Matchable, BlockedType extends Matchable, CorrespondenceType extends Matchable>
	extends AbstractBlocker<RecordType, BlockedType, CorrespondenceType>
	implements Blocker<RecordType, SchemaElementType, BlockedType, CorrespondenceType>,
	SymmetricBlocker<RecordType, SchemaElementType, BlockedType, CorrespondenceType>
{

	private BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> blockingFunction;
	private BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> secondBlockingFunction;
	private double ratio;

	public StandardBlockerWithBlockFiltering(BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> blockingFunction, double ratio) {
		this.blockingFunction = blockingFunction;
		this.secondBlockingFunction = blockingFunction;
		this.ratio = ratio;
	}

	/**
	 *
	 * Creates a new Standard Blocker with the given blocking function(s).
	 * If two datasets are used and secondBlockingFunction is not null, secondBlockingFunction will be used for the second dataset. If it is null, blockingFunction will be used for both datasets
	 *
	 * @param blockingFunction
	 * @param secondBlockingFunction
	 */
	public StandardBlockerWithBlockFiltering(BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> blockingFunction, BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> secondBlockingFunction) {
		this.blockingFunction = blockingFunction;
		this.secondBlockingFunction = secondBlockingFunction == null ? blockingFunction : secondBlockingFunction;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.Blocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.ResultSet, de.uni_mannheim.informatik.wdi.matching.MatchingEngine)
	 */
	@Override
	public Processable<Correspondence<BlockedType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset1,
			DataSet<RecordType, SchemaElementType> dataset2,
			Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences){

		// combine the datasets with the schema correspondences
		Processable<Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> ds1 = combineDataWithCorrespondences(dataset1, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		Processable<Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> ds2 = combineDataWithCorrespondences(dataset2, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getSecondRecord().getDataSourceIdentifier(),r)));
	
		// if we group the records by blocking key, we can obtain duplicates for BlockedType if it is different from RecordType and multiple records generated the same blocking key for BlockedType
		// so we aggregate the results to get a unique set of BlockedType elements (using the DistributionAggregator)
		
		// create the blocking keys for the first data set
		// results in pairs of [blocking key], distribution of correspondences
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> grouped1 = 
				ds1.aggregate(blockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> getInnerKey(
					Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}

		});

		// create the blocking keys for the second data set
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> grouped2 = 
				ds2.aggregate(secondBlockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> getInnerKey(
					Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}

		});
	
		// join the datasets via their blocking keys
		Processable<Pair<
		Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
		Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>>> blockedData = grouped1.join(grouped2, new PairFirstJoinKeyGenerator<>());

		//printBlockedData(blockedData);

		//BLOCK FILTERING
		// 1. BLOCK CARDINALITIES
		HashMap<String, Long> blockCardinalities = getBlockCardinalities(blockedData);

		// 2. GET SORTED RECORD / BLOCK ASSIGNMENTS
		//left
		Processable<Group<
				Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
				Pair<
						Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
						String>>> leftRecordBlockAssignments = blockedData.group(
				(Pair<
						Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
						Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> inputPair,
				 DataIterator<
						 Pair<
						 	Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, //key
						 	Pair< //value
								 Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
								 String>>> resultCollector
				) -> {
					//get blockingKey
					String key = inputPair.getFirst().getFirst();
					//add all records for that blockingKey to the resultCollector
					for (Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> recordPair : inputPair.getFirst().getSecond().getElements()) {
						//create returnPair with record as key and blockingKey as value
						Pair<
								Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
								String> returnPair = new Pair<>(recordPair, key);
						//group by records with returnPair as value
						Pair<
								Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
								Pair<
										Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
										String>> groupedRecord = new Pair<>(recordPair, returnPair);
						resultCollector.next(groupedRecord);
					}
				}
		);
		//right
		Processable<Group<
				Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
				Pair<
						Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
						String>>> rightRecordBlockAssignments = blockedData.group(
				(Pair<
						Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
						Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> inputPair,
				 DataIterator<
						 Pair<
								 Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, //key
								 Pair< //value
										 Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
										 String>>> resultCollector
				) -> {
					//get blockingKey
					String key = inputPair.getSecond().getFirst();
					//add all records for that blockingKey to the resultCollector
					for (Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> recordPair : inputPair.getSecond().getSecond().getElements()) {
						//create returnPair with record as key and blockingKey as value
						Pair<
								Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
								String> returnPair = new Pair<>(recordPair, key);
						//group by records with returnPair as value
						Pair<
								Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
								Pair<
										Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
										String>> groupedRecord = new Pair<>(recordPair, returnPair);
						resultCollector.next(groupedRecord);
					}
				}
		);

		// 3. SORT AND DELETE BLOCKS FROM RECORDS
		// based on the ratio
		Processable<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>> leftRecordBlockAssignmentsToKeep = deleteBlocksBasedOnTheCardinality(leftRecordBlockAssignments, blockCardinalities);
		Processable<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>> rightRecordBlockAssignmentsToKeep = deleteBlocksBasedOnTheCardinality(rightRecordBlockAssignments, blockCardinalities);


		// 4. GROUP ON BLOCKING KEYS AGAIN WITHOUT THE FILTERED RECORDS
		// function that returns the blockingKey
		Function<String, Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>> groupingKey = (p) -> p.getSecond();

		//co group the left and right recordBlockAssignments with the block as key and a distribution of records as value
		blockedData = leftRecordBlockAssignmentsToKeep.coGroup(rightRecordBlockAssignmentsToKeep, groupingKey, groupingKey,
				new RecordMapper<Pair<
						Iterable<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>>,
						Iterable<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>>>,
						Pair<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
								Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>>>() {
					
									private static final long serialVersionUID = 1L;

					@Override
					public void mapRecord(
							Pair<Iterable<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>>,
									Iterable<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>>> record,
							DataIterator<Pair<
									Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
									Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>>> resultCollector) {

						String blockingKey = null;
						//add records to the distribution
						Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>> leftRecordDistribution = new Distribution<>();
						Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>> rightRecordDistribution = new Distribution<>();
						//left
						for (Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String> leftRecord : record.getFirst()) {
							blockingKey = leftRecord.getSecond();
							leftRecordDistribution.add(leftRecord.getFirst());
						}
						//right
						for (Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String> rightRecord : record.getSecond()) {
							rightRecordDistribution.add(rightRecord.getFirst());
						}
						//create return pairs and pass them to the resultCollector
						if (blockingKey != null && leftRecordDistribution.getNumElements()>0 && rightRecordDistribution.getNumElements()>0) {
							Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> leftSide = new Pair<>(blockingKey, leftRecordDistribution);
							Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> rightSide = new Pair<>(blockingKey, rightRecordDistribution);
							resultCollector.next(new Pair<>(leftSide, rightSide));
						}
					}
				}
		);
		//DONE WITH BLOCK FILTERING
		//printBlockedData(blockedData);

			// transform the blocks into pairs of records
		Processable<Correspondence<BlockedType, CorrespondenceType>> result = blockedData.map(new RecordMapper<Pair<
				Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
				Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>>, 
				Correspondence<BlockedType, CorrespondenceType>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(
					Pair<
					Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>, 
					Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> record,
					DataIterator<Correspondence<BlockedType, CorrespondenceType>> resultCollector) {
				
				// iterate over the left pairs [blocked element],[correspondences]
				for(Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> p1 : record.getFirst().getSecond().getElements()){
					
					BlockedType record1 = p1.getFirst();
					
					// iterate over the right pairs [blocked element],[correspondences]
					for(Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> p2 : record.getSecond().getSecond().getElements()){
						
						BlockedType record2 = p2.getFirst();
						
						Processable<Correspondence<CorrespondenceType, Matchable>> causes = 
								new ProcessableCollection<>(p1.getSecond())
								.append(p2.getSecond())
								.distinct();
						
						// filter the correspondences such that only correspondences between the two records are contained (by data source id)
						causes = causes.where((c)->
							Q.toSet(p1.getFirst().getDataSourceIdentifier(), p2.getFirst().getDataSourceIdentifier())
							.equals(Q.toSet(c.getFirstRecord().getDataSourceIdentifier(), c.getSecondRecord().getDataSourceIdentifier()))
//							(c.getFirstRecord().getDataSourceIdentifier()==p1.getFirst().getDataSourceIdentifier() || c.getSecondRecord().getDataSourceIdentifier()==p1.getFirst().getDataSourceIdentifier())
//							&& (c.getFirstRecord().getDataSourceIdentifier()==p2.getFirst().getDataSourceIdentifier() || c.getSecondRecord().getDataSourceIdentifier()==p2.getFirst().getDataSourceIdentifier())
							);
						
						resultCollector.next(new Correspondence<BlockedType, CorrespondenceType>(record1, record2, 1.0, causes));
						
					}
					
				}
			}
		});

		//use .distinct() to remove correspondences that are found in multiple blocks
		return result.distinct();
	}

	/**
	 * Take recordBlocks assignments as input and delete blocks from a record based on the sorted block cardinalities and the pre-defined ratio.
	 * @param recordBlockAssignments
	 * @param blockCardinalities
	 */
	private Processable<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>> deleteBlocksBasedOnTheCardinality(Processable<Group<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>>> recordBlockAssignments,
												   HashMap<String, Long> blockCardinalities) {
			return recordBlockAssignments.map(
					(Group<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>,
					Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>> record,
					DataIterator<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>> collector

			) -> {
				//get all blockingKeys for that record
				LinkedHashMap<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>, Long> blockingKeys = new LinkedHashMap<>();
				for (Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String> recordValue : record.getRecords().get()) {
					blockingKeys.put(recordValue, blockCardinalities.get(recordValue.getSecond()));
				}
				//sort blockingKeys descending based on cardinality
				LinkedHashMap<Pair<
								Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>,
								Long> sortedBlockCardinalities = sortBlockCardinalities(blockingKeys);

				//calcluate the number of blocks that should be deleted using the user-defined ratio
				int blocksToDelete = (int) Math.floor(sortedBlockCardinalities.size() * ratio);
				//System.out.println(record.getKey().getFirst().getIdentifier().toString() + " has " + sortedBlockCardinalities.size() + " keys. Deleting "+ blocksToDelete);
				int deletedBlocks = 0;
				//iterate over sortedBlockCardinalities and add recordValues that should be deleted to the recordValuesToDelete-HashSet
				for (Map.Entry<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>, Long> entry : sortedBlockCardinalities.entrySet()) {
					//System.out.println(entry.getKey().getSecond() + ": " + entry.getValue().toString());
					if (deletedBlocks<blocksToDelete) {
						deletedBlocks++;
					} else {
						//collect records to keep
						collector.next(entry.getKey());
					}
				}
			});
	}

	private LinkedHashMap<Pair<Pair<BlockedType,Processable<Correspondence<CorrespondenceType,Matchable>>>,String>,Long> sortBlockCardinalities(LinkedHashMap<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>, Long> blockingKeys) {
		LinkedHashMap<Pair<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, String>, Long> sortedBlockCardinalities = blockingKeys.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey,
						Map.Entry::getValue,
						(e1,e2)->e1,
						LinkedHashMap::new));
		return sortedBlockCardinalities;
	}

	/**
	 * Get the blocks and their cardinality. Block cardinality is calculated as total sum of comparisons between the records of the two datasets.
	 * E.g. for the records in the block 'myBlockingKey': numberOfRecordsThatHaveThisBlockingKeyInDataset1 * numberOfRecordsThatHaveThisBlockingKeyInDataset2.
	 * Therefore, the block cardinality indicates the total number of comparisons for that block (as records from the same dataset are not compared to each other: duplicate-free assumption).
	 * @param blockedData
	 * @return
	 */
	private HashMap<String,Long> getBlockCardinalities(Processable<Pair<
			Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
			Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>>> blockedData) {
		//init block cardinality HashMap
		HashMap<String, Long> blockCardinalities = new HashMap<>();

		//for each block
		for (Pair<
				Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
				Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> pair : blockedData.get()) {
			// product of number of elements of the distribution for the first dataset times number of elements of the distribution for the second dataset
			long totalCardinality = pair.getFirst().getSecond().getNumElements() * pair.getSecond().getSecond().getNumElements();
			blockCardinalities.put(pair.getFirst().getFirst(), totalCardinality);
		}
		return blockCardinalities;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.Blocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, boolean, de.uni_mannheim.informatik.wdi.model.ResultSet, de.uni_mannheim.informatik.wdi.matching.MatchingEngine)
	 */
	@Override
	public  Processable<Correspondence<BlockedType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset,
			Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences) {

		// combine the datasets with the schema correspondences
//		Processable<Pair<RecordType, Processable<SimpleCorrespondence<CorrespondenceType>>>> ds = combineDataWithCorrespondences(dataset, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		// as we only use one dataset here, we don't know if the record is on the left- or right-hand side of the correspondence
		Processable<Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> ds = combineDataWithCorrespondences(dataset, schemaCorrespondences, 
				(r,c)->
				{
					c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r));
					c.next(new Pair<>(r.getSecondRecord().getDataSourceIdentifier(),r));
				});
		
		// if we group the records by blocking key, we can obtain duplicates for BlockedType if it is different from RecordType and multiple records generated the same blocking key for BlockedType
		// so we aggregate the results to get a unique set of BlockedType elements (using the DistributionAggregator)
		
		// group all records by their blocking keys		
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> grouped = ds.aggregate(blockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>, Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> getInnerKey(
					Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}
		});
		
		// transform the groups into record pairs
		Processable<Correspondence<BlockedType, CorrespondenceType>> blocked = grouped.map((g, collector) ->
		{
			List<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>> list = new ArrayList<>(g.getSecond().getElements());
			
			// sort the list before generating the pairs, so all pairs have the lower data source id on the left-hand side.
			list.sort((o1,o2)->Integer.compare(o1.getFirst().getDataSourceIdentifier(), o2.getFirst().getDataSourceIdentifier()));
			
			for(int i = 0; i < list.size(); i++) {
				Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> p1 = list.get(i);
				for(int j = i+1; j < list.size(); j++) {
					Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>> p2 = list.get(j);
					
					Processable<Correspondence<CorrespondenceType, Matchable>> causes = new ProcessableCollection<>(p1.getSecond()).append(p2.getSecond()).distinct();
					
					// filter the correspondences such that only correspondences between the two records are contained (by data source id)
					causes = causes.where((c)->
						(c.getFirstRecord().getDataSourceIdentifier()==p1.getFirst().getDataSourceIdentifier() || c.getSecondRecord().getDataSourceIdentifier()==p1.getFirst().getDataSourceIdentifier())
						&& (c.getFirstRecord().getDataSourceIdentifier()==p2.getFirst().getDataSourceIdentifier() || c.getSecondRecord().getDataSourceIdentifier()==p2.getFirst().getDataSourceIdentifier())
						);
					
					collector.next(new Correspondence<>(p1.getFirst(), p2.getFirst(), 1.0, causes));
				}
			}
		});
		
		// remove duplicates that were created if two records have multiple matching blocking keys
		blocked = blocked.distinct();
		
		return blocked;
	}

}
