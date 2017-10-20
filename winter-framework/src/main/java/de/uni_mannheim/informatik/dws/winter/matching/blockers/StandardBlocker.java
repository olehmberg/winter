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
import java.util.Arrays;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.LeftIdentityPair;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.PairFirstJoinKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;
import de.uni_mannheim.informatik.dws.winter.processing.aggregators.DistributionAggregator;
import de.uni_mannheim.informatik.dws.winter.processing.aggregators.StringConcatenationAggregator;
import de.uni_mannheim.informatik.dws.winter.utils.Distribution;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * Implementation of a standard {@link AbstractBlocker} based on blocking keys. All records for which the same blocking key is generated are returned as pairs.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>			the type of records which are the input for the blocking operation
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType 
 * @param <CorrespondenceType>	the type of correspondences which are the input for the blocking operation
 * @param <BlockedType>			the type of record which is actually blocked
 */
public class StandardBlocker<RecordType extends Matchable, SchemaElementType extends Matchable, BlockedType extends Matchable, CorrespondenceType extends Matchable>
	extends AbstractBlocker<RecordType, BlockedType, CorrespondenceType>
	implements Blocker<RecordType, SchemaElementType, BlockedType, CorrespondenceType>,
	SymmetricBlocker<RecordType, SchemaElementType, BlockedType, CorrespondenceType>
{

	private BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> blockingFunction;
	private BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> secondBlockingFunction;
	private boolean measureBlockSizes = false;
	private double blockFilterRatio = 1.0;
	private boolean deduplicatePairs = true;
	
	/**
	 * @param measureBlockSizes the measureBlockSizes to set
	 */
	public void setMeasureBlockSizes(boolean measureBlockSizes) {
		this.measureBlockSizes = measureBlockSizes;
	}
	
	/**
	 * @param blockFilterRatio the blockFilterRatio to set
	 */
	public void setBlockFilterRatio(double blockFilterRatio) {
		this.blockFilterRatio = blockFilterRatio;
	}
	
	/**
	 * @param deduplicatePairs the deduplicatePairs to set
	 */
	public void setDeduplicatePairs(boolean deduplicatePairs) {
		this.deduplicatePairs = deduplicatePairs;
	}
	
	public StandardBlocker(BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> blockingFunction) {
		this.blockingFunction = blockingFunction;
		this.secondBlockingFunction = blockingFunction;
	}
	
	/**
	 * 
	 * Creates a new Standard Blocker with the given blocking function(s). 
	 * If two datasets are used and secondBlockingFunction is not null, secondBlockingFunction will be used for the second dataset. If it is null, blockingFunction will be used for both datasets 
	 * 
	 * @param blockingFunction		the blocking function for the first dataset
	 * @param secondBlockingFunction	the blocking function for the second dataset
	 */
	public StandardBlocker(BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> blockingFunction, BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> secondBlockingFunction) {
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
	
		if(measureBlockSizes) {
			System.out.println(String.format("[StandardBlocker] created %d blocking keys for first dataset", grouped1.size()));
			System.out.println(String.format("[StandardBlocker] created %d blocking keys for second dataset", grouped2.size()));
		}
		
		// join the datasets via their blocking keys
		Processable<Pair<
		Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>,
		Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>>>
			blockedData = grouped1.join(grouped2, new PairFirstJoinKeyGenerator<>());
		
		if(measureBlockSizes) {
			System.out.println(String.format("[StandardBlocker] created %d blocks from blocking keys", blockedData.size()));
		}
		
		// remove the largest blocks, if requested
		if(blockFilterRatio<1.0) {
			System.out.println(String.format("[StandardBlocker] %d blocks before filtering", blockedData.size()));
			
			Processable<Pair<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>, Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>>> toRemove = blockedData
					.sort((p)->p.getFirst().getSecond().getNumElements()*p.getSecond().getSecond().getNumElements(), false)
					.take((int)(blockedData.size()*(1-blockFilterRatio)));
			
			for(Pair<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>, Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> p : toRemove.get()) {
				System.out.println(String.format("\tRemoving block '%s' (%d pairs)", 
						p.getFirst().getFirst(),
						p.getFirst().getSecond().getNumElements() * p.getSecond().getSecond().getNumElements()));
			}
			
			blockedData = blockedData
					.sort((p)->p.getFirst().getSecond().getNumElements()*p.getSecond().getSecond().getNumElements(), true)
					.take((int)(blockedData.size()*blockFilterRatio));
			System.out.println(String.format("[StandardBlocker] %d blocks after filtering", blockedData.size()));
		}
		
		if(measureBlockSizes) {			
			// calculate block size distribution
			Processable<Pair<Integer, Distribution<Integer>>> aggregated = blockedData.aggregate(
				(Pair<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>, Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> record,
				DataIterator<Pair<Integer, Integer>> resultCollector) 
				-> {
					int blockSize = record.getFirst().getSecond().getNumElements() * record.getSecond().getSecond().getNumElements();
					resultCollector.next(new Pair<Integer, Integer>(0, blockSize));
				}
				, new DistributionAggregator<Integer, Integer, Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Integer getInnerKey(Integer record) {
						return record;
					}
				});
			
			Pair<Integer, Distribution<Integer>> aggregationResult = Q.firstOrDefault(aggregated.get());
			
			if(aggregationResult!=null) {
				Distribution<Integer> dist = aggregationResult.getSecond();
				
				System.out.println("[StandardBlocker] Block size distribution:");
				System.out.println(dist.format());
	
				// determine frequent blocking key values
				Processable<Pair<Integer, String>> blockValues = blockedData.aggregate(
						(Pair<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>, Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>> record,
						DataIterator<Pair<Integer, String>> resultCollector) 
						-> {
							int blockSize = record.getFirst().getSecond().getNumElements() * record.getSecond().getSecond().getNumElements();
							resultCollector.next(new Pair<Integer, String>(blockSize, record.getFirst().getFirst()));
						},
						new StringConcatenationAggregator<>(","))
						.sort((p)->p.getFirst(), false);
				
				System.out.println("50 most-frequent blocking key values:");
				for(Pair<Integer, String> value : blockValues.take(50).get()) {
					System.out.println(String.format("\t%d\t%s", value.getFirst(), value.getSecond()));
				}
			} else {
				System.out.println("No blocks were created!");
			}

		}
		
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
						
						int[] pairIds = new int[] { p1.getFirst().getDataSourceIdentifier(), p2.getFirst().getDataSourceIdentifier() };
						Arrays.sort(pairIds);
						
						// filter the correspondences such that only correspondences between the two records are contained (by data source id)
						causes = causes.where((c)-> {
						
							int[] causeIds = new int[] { c.getFirstRecord().getDataSourceIdentifier(), c.getSecondRecord().getDataSourceIdentifier() };
							Arrays.sort(causeIds);
							
							return Arrays.equals(pairIds, causeIds);
						});
						
						resultCollector.next(new Correspondence<BlockedType, CorrespondenceType>(record1, record2, 1.0, causes));
						
					}
					
				}
			}
		});
		
		if(deduplicatePairs) {
			//use .distinct() to remove correspondences that are found in multiple blocks
			result = result.distinct();
		}
		
		calculatePerformance(dataset1, dataset2, result);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.Blocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, boolean, de.uni_mannheim.informatik.wdi.model.ResultSet, de.uni_mannheim.informatik.wdi.matching.MatchingEngine)
	 */
	@Override
	public  Processable<Correspondence<BlockedType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset,
			Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences) {

		// combine the datasets with the schema correspondences
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
					
					Processable<Correspondence<CorrespondenceType, Matchable>> causes = new ProcessableCollection<>(p1.getSecond()).append(p2.getSecond());
					
					int[] pairIds = new int[] { p1.getFirst().getDataSourceIdentifier(), p2.getFirst().getDataSourceIdentifier() };
					Arrays.sort(pairIds);
					
					// filter the correspondences such that only correspondences between the two records (p1 & p2) are contained (by data source id)
					causes = causes.where((c)->
					{
						int[] causeIds = new int[] { c.getFirstRecord().getDataSourceIdentifier(), c.getSecondRecord().getDataSourceIdentifier() };
						Arrays.sort(causeIds);
						
						return Arrays.equals(pairIds, causeIds);
					}).distinct();
					
					collector.next(new Correspondence<>(p1.getFirst(), p2.getFirst(), 1.0, causes));
				}
			}
		});
		
		// remove duplicates that were created if two records have multiple matching blocking keys
		blocked = blocked.distinct();
		
		calculatePerformance(dataset, dataset, blocked);
		
		return blocked;
	}

}
