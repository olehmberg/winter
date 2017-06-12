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
import de.uni_mannheim.informatik.dws.winter.processing.DatasetIterator;
import de.uni_mannheim.informatik.dws.winter.processing.PairFirstJoinKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;
import de.uni_mannheim.informatik.dws.winter.processing.aggregators.DistributionAggregator;
import de.uni_mannheim.informatik.dws.winter.utils.Distribution;

/**
 * Implementation of a {@link AbstractBlocker} based on values. 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 * @param <SchemaElementType>
 * @param <BlockedType>
 * @param <CorrespondenceType>
 */
public class ValueBasedBlocker<RecordType extends Matchable, SchemaElementType extends Matchable, BlockedType extends Matchable>
	extends AbstractBlocker<RecordType, BlockedType, MatchableValue>
	implements Blocker<RecordType, SchemaElementType, BlockedType, MatchableValue>,
	SymmetricBlocker<RecordType, SchemaElementType, BlockedType, MatchableValue>
{

	private BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> blockingFunction;
	private BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> secondBlockingFunction;
	
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
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>> grouped1 = 
				ds1.aggregateRecords(blockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> getInnerKey(
					Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}

		});

		// create the blocking keys for the second data set
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>> grouped2 = 
				ds2.aggregateRecords(secondBlockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> getInnerKey(
					Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}

		});
	
		// join the datasets via their blocking keys
		Processable<Pair<
		Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>,
		Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>>> 
			blockedData = grouped1.join(grouped2, new PairFirstJoinKeyGenerator<>());
		
		// transform the blocks into pairs of records		
		Processable<Correspondence<BlockedType, MatchableValue>> result = blockedData.transform(new RecordMapper<Pair<
				Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>,
				Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>>, 
				Correspondence<BlockedType, MatchableValue>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(
					Pair<
					Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>, 
					Pair<String,Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>> record,
					DatasetIterator<Correspondence<BlockedType, MatchableValue>> resultCollector) {
				
				Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> dist1 = record.getFirst().getSecond();
				
				// iterate over the left pairs [blocked element],[correspondences]
				for(Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> p1 : dist1.getElements()){
					
					BlockedType record1 = p1.getFirst();
					
					Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> dist2 = record.getSecond().getSecond();
					
					// iterate over the right pairs [blocked element],[correspondences]
					for(Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> p2 : dist2.getElements()){
						
						BlockedType record2 = p2.getFirst();
						
						double matchCount = Math.min(dist1.getFrequency(p1), dist2.getFrequency(p2));
						
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
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>>> grouped = ds.aggregateRecords(blockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>, Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> getInnerKey(
					Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}
		});
		
		// transform the groups into record pairs
		Processable<Correspondence<BlockedType, MatchableValue>> blocked = grouped.transform((g, collector) ->
		{
			
			Distribution<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> dist = g.getSecond();
			
			List<Pair<BlockedType, Processable<Correspondence<MatchableValue, Matchable>>>> list = new ArrayList<>(g.getSecond().getElements());
			
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
