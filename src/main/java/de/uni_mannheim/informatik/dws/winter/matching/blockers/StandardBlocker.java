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
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.SimpleCorrespondence;
import de.uni_mannheim.informatik.dws.winter.processing.DatasetIterator;
import de.uni_mannheim.informatik.dws.winter.processing.PairFirstJoinKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;
import de.uni_mannheim.informatik.dws.winter.processing.aggregators.DistributionAggregator;
import de.uni_mannheim.informatik.dws.winter.utils.Distribution;

/**
 * Implementation of a standard {@link Blocker} based on blocking keys. All records for which the same blocking key is generated are returned as pairs.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 * @param <SchemaElementType>
 * @param <BlockedType>
 * @param <CorrespondenceType>
 */
public class StandardBlocker<RecordType extends Matchable, SchemaElementType extends Matchable, BlockedType extends Matchable, CorrespondenceType extends Matchable>
	extends Blocker<RecordType, BlockedType, CorrespondenceType>
	implements CrossDataSetBlocker<RecordType, SchemaElementType, BlockedType, CorrespondenceType>,
	SingleDataSetBlocker<RecordType, SchemaElementType, BlockedType, CorrespondenceType>
{

	private BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> blockingFunction;
	private BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> secondBlockingFunction;
	
	public StandardBlocker(BlockingKeyGenerator<RecordType, CorrespondenceType, BlockedType> blockingFunction) {
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
			Processable<SimpleCorrespondence<CorrespondenceType>> schemaCorrespondences){

		// combine the datasets with the schema correspondences
		Processable<Pair<RecordType, Processable<SimpleCorrespondence<CorrespondenceType>>>> ds1 = combineDataWithCorrespondences(dataset1, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		Processable<Pair<RecordType, Processable<SimpleCorrespondence<CorrespondenceType>>>> ds2 = combineDataWithCorrespondences(dataset2, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getSecondRecord().getDataSourceIdentifier(),r)));
	
		// if we group the records by blocking key, we can obtain duplicates for BlockedType if it is different from RecordType and multiple records generated the same blocking key for BlockedType
		// so we aggregate the results to get a unique set of BlockedType elements (using the DistributionAggregator)
		
		// create the blocking keys for the first data set
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>> grouped1 = 
				ds1.aggregateRecords(blockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>, Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> getInnerKey(
					Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}

		});

		// create the blocking keys for the second data set
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>> grouped2 = 
				ds2.aggregateRecords(secondBlockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>, Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> getInnerKey(
					Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}

		});
	
		// join the datasets via their blocking keys
		Processable<Pair<
		Pair<String,Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>,
		Pair<String,Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>>>
			blockedData = grouped1.join(grouped2, new PairFirstJoinKeyGenerator<>());
		
		// transform the blocks into pairs of records
		Processable<Correspondence<BlockedType, CorrespondenceType>> result = blockedData.transform(new RecordMapper<Pair<
				Pair<String,Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>,
				Pair<String,Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>>, 
				Correspondence<BlockedType, CorrespondenceType>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(
					Pair<
					Pair<String,Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>, 
					Pair<String,Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>> record,
					DatasetIterator<Correspondence<BlockedType, CorrespondenceType>> resultCollector) {
				for(Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> p1 : record.getFirst().getSecond().getElements()){
					BlockedType record1 = p1.getFirst();
					for(Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> p2 : record.getSecond().getSecond().getElements()){
						BlockedType record2 = p2.getFirst();
						
						Processable<SimpleCorrespondence<CorrespondenceType>> causes = 
								new ProcessableCollection<>(p1.getSecond())
								.append(p2.getSecond())
								.deduplicate();
						
						resultCollector.next(new Correspondence<BlockedType, CorrespondenceType>(record1, record2, 1.0, causes));	
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
	public  Processable<Correspondence<BlockedType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset,
			boolean isSymmetric,
			Processable<SimpleCorrespondence<CorrespondenceType>> schemaCorrespondences) {

		// combine the datasets with the schema correspondences
		Processable<Pair<RecordType, Processable<SimpleCorrespondence<CorrespondenceType>>>> ds = combineDataWithCorrespondences(dataset, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		
		// if we group the records by blocking key, we can obtain duplicates for BlockedType if it is different from RecordType and multiple records generated the same blocking key for BlockedType
		// so we aggregate the results to get a unique set of BlockedType elements (using the DistributionAggregator)
		
		// group all records by their blocking keys
//		Result<Group<String, Pair<BlockedType, Result<SimpleCorrespondence<CorrespondenceType>>>>> grouped = engine.groupRecords(ds, blockingFunction);		
		Processable<Pair<String, Distribution<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>>> grouped = ds.aggregateRecords(blockingFunction, new DistributionAggregator<String, Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>, Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> getInnerKey(
					Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> record) {
				// change the pairs such that they are considered equal if the first element is equal (ignoring the second element)
				return new LeftIdentityPair<>(record.getFirst(), record.getSecond());
			}
		});
		
		// transform the groups into record pairs
		Processable<Correspondence<BlockedType, CorrespondenceType>> blocked = grouped.transform((g, collector) ->
		{
			List<Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>>> list = new ArrayList<>(g.getSecond().getElements());
			
			for(int i = 0; i < list.size(); i++) {
				Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> p1 = list.get(i);
				for(int j = i+1; j < list.size(); j++) {
					Pair<BlockedType, Processable<SimpleCorrespondence<CorrespondenceType>>> p2 = list.get(j);
					
					Processable<SimpleCorrespondence<CorrespondenceType>> causes = new ProcessableCollection<>(p1.getSecond()).append(p2.getSecond()).deduplicate();
					
					collector.next(new Correspondence<>(p1.getFirst(), p2.getFirst(), 1.0, causes));
				}
			}
		});
		
		return blocked;
	}

}
