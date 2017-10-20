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
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.LeftIdentityPair;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Function;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;
import de.uni_mannheim.informatik.dws.winter.processing.aggregators.DistributionAggregator;
import de.uni_mannheim.informatik.dws.winter.processing.aggregators.StringConcatenationAggregator;
import de.uni_mannheim.informatik.dws.winter.utils.Distribution;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * Implementation of a {@link AbstractBlocker} based on values. 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>			the type of records that is provided as input data
 * @param <SchemaElementType>	the schema element type of RecordType
 * @param <BlockedType>			the type of record that should be blocked
 */
public class ValueBasedBlocker<RecordType extends Matchable, SchemaElementType extends Matchable, BlockedType extends Matchable>
	extends AbstractBlocker<RecordType, BlockedType, MatchableValue>
	implements Blocker<RecordType, SchemaElementType, BlockedType, MatchableValue>,
	SymmetricBlocker<RecordType, SchemaElementType, BlockedType, MatchableValue>
{

	protected class Block extends LeftIdentityPair<String, Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>> {
		private static final long serialVersionUID = 1L;

		public Block(String first, Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> second) {
			super(first, second);
		}
	}
	protected class BlockJoinKeyGenerator implements Function<String,Block> {
		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.winter.processing.Function#execute(java.lang.Object)
		 */
		@Override
		public String execute(ValueBasedBlocker<RecordType, SchemaElementType, BlockedType>.Block input) {
			return input.getFirst();
		}
		
	}
	
	private BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> blockingFunction;
	private BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> secondBlockingFunction;

	private boolean measureBlockSizes = false;
	private boolean considerDuplicateValues = false;
	
	/**
	 * @param measureBlockSizes the measureBlockSizes to set
	 */
	public void setMeasureBlockSizes(boolean measureBlockSizes) {
		this.measureBlockSizes = measureBlockSizes;
	}
	
	/**
	 * if set to true, all duplicate blocking key values will count towards the similarity score
	 * if set to false, a 1:1 mapping of blocking key values is performed before calculating the similarity score 
	 * 
	 * example:
	 * 
	 * blocking keys A: 1,1,1,1,1
	 * blocking keys B: 1,1,2,3,4
	 * 
	 * if true, the resulting score will be 5.0
	 * if false, the resulting score will be 2.0
	 * 
	 * @param considerDuplicateValues the considerDuplicateValues to set
	 */
	public void setConsiderDuplicateValues(boolean considerDuplicateValues) {
		this.considerDuplicateValues = considerDuplicateValues;
	}
	
	public ValueBasedBlocker(BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> blockingFunction) {
		this.blockingFunction = blockingFunction;
		this.secondBlockingFunction = blockingFunction;
	}
	
	/**
	 * 
	 * Creates a new Standard Blocker with the given blocking function(s). 
	 * If two datasets are used and secondBlockingFunction is not null, secondBlockingFunction will be used for the second dataset. If it is null, blockingFunction will be used for both datasets 
	 * 
	 * @param blockingFunction
	 * @param secondBlockingFunction
	 */
	public ValueBasedBlocker(BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> blockingFunction, BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> secondBlockingFunction) {
		this.blockingFunction = blockingFunction;
		this.secondBlockingFunction = secondBlockingFunction == null ? blockingFunction : secondBlockingFunction;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.Blocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.ResultSet, de.uni_mannheim.informatik.wdi.matching.MatchingEngine)
	 */
	@Override
	public Processable<Correspondence<BlockedType, MatchableValue>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset1,
			DataSet<RecordType, SchemaElementType> dataset2,
			Processable<Correspondence<MatchableValue, Matchable>> schemaCorrespondences){

		// combine the datasets with the schema correspondences
		Processable<Pair<RecordType, Processable<Correspondence<MatchableValue, Matchable>>>> ds1 = combineDataWithCorrespondences(dataset1, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		Processable<Pair<RecordType, Processable<Correspondence<MatchableValue, Matchable>>>> ds2 = combineDataWithCorrespondences(dataset2, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getSecondRecord().getDataSourceIdentifier(),r)));
	
		// if we group the records by blocking key, we can obtain duplicates for BlockedType if it is different from RecordType and multiple records generated the same blocking key for BlockedType
		// so we aggregate the results to get a unique set of BlockedType elements (using the DistributionAggregator)
		
		// create the blocking keys for the first data set
		// results in pairs of [blocking key], [blocked type]
		Processable<Block> grouped1 = 
				ds1.aggregate(blockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>() {

					private static final long serialVersionUID = 1L;
		
					@Override
					public Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> getInnerKey(
							Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> record) {
						// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
						return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
					}
		
				})
				.map((Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>> record, DataIterator<ValueBasedBlocker<RecordType, SchemaElementType, BlockedType>.Block> resultCollector) 
						-> {
						resultCollector.next(new Block(record.getFirst(), record.getSecond()));
					}
				);

		// create the blocking keys for the second data set
		Processable<Block> grouped2 = 
				ds2.aggregate(secondBlockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>() {
		
					private static final long serialVersionUID = 1L;
		
					@Override
					public Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> getInnerKey(
							Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> record) {
						// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
						return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
					}
		
				})
				.map((Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>> record, DataIterator<ValueBasedBlocker<RecordType, SchemaElementType, BlockedType>.Block> resultCollector) 
						-> {
						resultCollector.next(new Block(record.getFirst(), record.getSecond()));
					}
				);
		
		// join the datasets via their blocking keys
		Processable<Pair<Block,Block>> blockedData = grouped1.join(grouped2, new BlockJoinKeyGenerator());
		
		if(measureBlockSizes) {			
			// calculate block size distribution
			Processable<Pair<Integer, Distribution<Integer>>> aggregated = blockedData.aggregate(
				(Pair<Block, Block> record,
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
			Distribution<Integer> dist = Q.firstOrDefault(aggregated.get()).getSecond();
			
			System.out.println("[ValueBasedBlocker] Block size distribution:");
			System.out.println(dist.format());

			// determine frequent blocking key values
			Processable<Pair<Integer, String>> blockValues = blockedData.aggregate(
					(Pair<Block, Block> record, DataIterator<Pair<Integer, String>> resultCollector) 
					-> {
						int blockSize = record.getFirst().getSecond().getNumElements() * record.getSecond().getSecond().getNumElements();
						resultCollector.next(new Pair<Integer, String>(blockSize, record.getFirst().getFirst()));
					}
					, new StringConcatenationAggregator<>(","))
					.sort((p)->p.getFirst(), false);
			
			System.out.println("50 most-frequent blocking key values:");
			for(Pair<Integer, String> value : blockValues.take(50).get()) {
				System.out.println(String.format("\t%d\t%s", value.getFirst(), value.getSecond()));
			}

		}
		
		// transform the blocks into pairs of records		
		Processable<Correspondence<BlockedType, MatchableValue>> result 
		= blockedData.map(new RecordMapper<Pair<Block,Block>, Correspondence<BlockedType, MatchableValue>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(
					Pair<Block, Block> record,
					DataIterator<Correspondence<BlockedType, MatchableValue>> resultCollector) {
				
				Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> dist1 = record.getFirst().getSecond();
				
				// iterate over the left pairs [blocked element],[correspondences]
				for(Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> p1 : dist1.getElements()){
					
					BlockedType record1 = p1.getFirst();
					
					// indicates how often record1 generated the blocking key value for the left block
					int record1Frequency = dist1.getFrequency(p1);
					
					Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> dist2 = record.getSecond().getSecond();
					
					// iterate over the right pairs [blocked element],[correspondences]
					for(Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> p2 : dist2.getElements()){
						
						BlockedType record2 = p2.getFirst();

						// indicates how often record2 generated the blocking key value for the right block
						int record2Frequency = dist2.getFrequency(p2);
						
						double matchCount = 0.0;
						if(considerDuplicateValues) {
							matchCount = Math.max(record1Frequency, record2Frequency);
						} else {
							matchCount = Math.min(record1Frequency, record2Frequency);
						}
						
						// generate causes from the distribution: min number of occurrences for both records is the number of possible matches
						// for simplicity, we just generate one cause with this number as similarity score
						// alternatively, we could generate causes from p1.getSecond() and p2.getSecond()
						Processable<Correspondence<MatchableValue, Matchable>> causes = new ProcessableCollection<>();
						// get the first cause from p1
						Correspondence<MatchableValue, Matchable> c1 = p1.getSecond().firstOrNull();
						// get the first cause from p2
						Correspondence<MatchableValue, Matchable> c2 = p2.getSecond().firstOrNull();
						
						causes.add(new Correspondence<MatchableValue, Matchable>(c1.getFirstRecord(), c2.getFirstRecord(), matchCount));
						
						resultCollector.next(new Correspondence<BlockedType, MatchableValue>(record1, record2, matchCount, causes));
						
					}
					
				}
			}
		});
		
		return result;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.Blocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, boolean, de.uni_mannheim.informatik.wdi.model.ResultSet, de.uni_mannheim.informatik.wdi.matching.MatchingEngine)
	 */
	@Override
	public  Processable<Correspondence<BlockedType, MatchableValue>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset,
			Processable<Correspondence<MatchableValue, Matchable>> schemaCorrespondences) {

		// combine the datasets with the schema correspondences
		Processable<Pair<RecordType, Processable<Correspondence<MatchableValue, Matchable>>>> ds = combineDataWithCorrespondences(dataset, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		
		// if we group the records by blocking key, we can obtain duplicates for BlockedType if it is different from RecordType and multiple records generated the same blocking key for BlockedType
		// so we aggregate the results to get a unique set of BlockedType elements (using the DistributionAggregator)
		
		// group all records by their blocking keys
//		Result<Group<String, Pair<BlockedType, Result<SimpleCorrespondence<CorrespondenceType>>>>> grouped = engine.groupRecords(ds, blockingFunction);		
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>> grouped = ds.aggregate(blockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> getInnerKey(
					Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}
		});
		
		// transform the groups into record pairs
		Processable<Correspondence<BlockedType, MatchableValue>> blocked = grouped.map((g, collector) ->
		{
			
			Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> dist = g.getSecond();
			
			List<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> list = new ArrayList<>(g.getSecond().getElements());
			
			// sort the list before generating the pairs, so all pairs have the lower data source id on the left-hand side.
			list.sort((o1,o2)->Integer.compare(o1.getFirst().getDataSourceIdentifier(), o2.getFirst().getDataSourceIdentifier()));
			
			for(int i = 0; i < list.size(); i++) {
				Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> p1 = list.get(i);
				for(int j = i+1; j < list.size(); j++) {
					Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> p2 = list.get(j);
					
					double matchCount = dist.getFrequency(p1);
					
					Processable<Correspondence<MatchableValue, Matchable>> causes = new ProcessableCollection<>();
					// we simply use the first cause and change the similarity value into the match count
					Correspondence<MatchableValue, Matchable> c = p1.getSecond().firstOrNull();
					Correspondence<MatchableValue, Matchable> cause = new Correspondence<>(c.getFirstRecord(), c.getSecondRecord(), matchCount);
					causes.add(cause);
					
					collector.next(new Correspondence<>(p1.getFirst(), p2.getFirst(), matchCount, causes));
				}
			}
		});
		
		return blocked;
	}

}
