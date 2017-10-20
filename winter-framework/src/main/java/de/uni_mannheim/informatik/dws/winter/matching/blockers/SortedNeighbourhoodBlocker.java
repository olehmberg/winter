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
import java.util.Comparator;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;

/**
 * Implementation of the Sorted-Neighbourhood {@link AbstractBlocker}, which based on
 * the blocking key of the {@link BlockingKeyGenerator} compares only the
 * surrounding {@link AbstractRecord}s.
 * 
 * Only supports single-threaded execution.
 * Does not consider data source identifiers.
 * 
 * @author Robert Meusel (robert@dwslab.de)
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>			the type of records which are the input for the blocking operation
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType 
 * @param <CorrespondenceType>	the type of correspondences which are the input for the blocking operation
 */
public class SortedNeighbourhoodBlocker<RecordType extends Matchable, SchemaElementType extends Matchable, CorrespondenceType extends Matchable> 
	extends AbstractBlocker<RecordType, SchemaElementType, CorrespondenceType>
	implements Blocker<RecordType, SchemaElementType, RecordType, CorrespondenceType>,
	SymmetricBlocker<RecordType, SchemaElementType, RecordType, CorrespondenceType>
{

	private BlockingKeyGenerator<RecordType, CorrespondenceType, RecordType> blockingFunction;
	private int windowSize;

	public SortedNeighbourhoodBlocker(
			BlockingKeyGenerator<RecordType, CorrespondenceType, RecordType> blockingFunction, int windowSize) {
		this.blockingFunction = blockingFunction;
		this.windowSize = windowSize;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.SingleDataSetBlocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, boolean, de.uni_mannheim.informatik.wdi.model.Result, de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine)
	 */
	@Override
	public Processable<Correspondence<RecordType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset, Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences) {
		Processable<Correspondence<RecordType, CorrespondenceType>> result = new ProcessableCollection<>();

		Processable<Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> ds = combineDataWithCorrespondences(dataset, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		
		// add all instances to one list, and compute the keys
		Processable<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> blockingKeys = ds.map(blockingFunction);
		ArrayList<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> keyIdentifierList = new ArrayList<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>(blockingKeys.get());
//		for (RecordType record : dataset.get()) {
//			keyIdentifierList.add(new Pair<String, RecordType>(blockingFunction
//					.getBlockingKey(record), record));
//		}
		
		
		// sort the list by the keys
		Comparator<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> pairComparator = new Comparator<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>() {

			@Override
			public int compare(Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> o1,
					Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> o2) {
				return o1.getFirst().compareTo(o2.getFirst());
			}

		};
		Collections.sort(keyIdentifierList, pairComparator);
		for (int i = 0; i < keyIdentifierList.size() - 1; i++) {
			Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>> p1 = keyIdentifierList.get(i).getSecond();
			for (int j = i + 1; ((j - i) < windowSize)
					&& (j < keyIdentifierList.size()); j++) {
				Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>> p2 = keyIdentifierList.get(j).getSecond();
				
				result.add(new Correspondence<RecordType, CorrespondenceType>(p1.getFirst(),p2.getFirst(), 1.0, createCausalCorrespondences(p1, p2)));
			}
		}

		calculatePerformance(dataset, dataset, result);
		return result;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.CrossDataSetBlocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.Result, de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine)
	 */
	@Override
	public Processable<Correspondence<RecordType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset1, DataSet<RecordType, SchemaElementType> dataset2,
			Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences) {
		Processable<Correspondence<RecordType, CorrespondenceType>> result = new ProcessableCollection<>();

		Processable<Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> ds1 = combineDataWithCorrespondences(dataset1, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		Processable<Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> ds2 = combineDataWithCorrespondences(dataset2, schemaCorrespondences, (r,c)->c.next(new Pair<>(r.getFirstRecord().getDataSourceIdentifier(),r)));
		
		// add all instances to one list, and compute the keys
		Processable<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> blocked1 = ds1.map(blockingFunction);
		ArrayList<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> keyIdentifierList = new ArrayList<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>(blocked1.get());
//		for (RecordType record : dataset1.get()) {
//			keyIdentifierList.add(new Pair<String, RecordType>(blockingFunction
//					.getBlockingKey(record), record));
//		}
		Processable<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> blocked2 = ds2.map(blockingFunction);
		keyIdentifierList.addAll(blocked2.get());
//		for (RecordType record : dataset2.get()) {
//			keyIdentifierList.add(new Pair<String, RecordType>(blockingFunction
//					.getBlockingKey(record), record));
//		}
		// sort the list by the keys
		Comparator<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>> pairComparator = new Comparator<Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>>>() {

			@Override
			public int compare(Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> o1,
					Pair<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> o2) {
				return o1.getFirst().compareTo(o2.getFirst());
			}

		};
		Collections.sort(keyIdentifierList, pairComparator);

		for (int i = 0; i < keyIdentifierList.size() - 1; i++) {
			Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>> p1 = keyIdentifierList.get(i).getSecond();
			
			// make sure r1 belongs to dataset1
			if(dataset1.getRecord(p1.getFirst().getIdentifier())!=null) {
			
				int counter = 1;
				int j = i;
				while ((counter < windowSize)
						&& (j < (keyIdentifierList.size() - 1))) {
					Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>> p2 = keyIdentifierList.get(++j).getSecond();
					// check if they belong *not* to the same dataset
					if (!p2.getFirst().getProvenance().equals(p1.getFirst().getProvenance())) {
						result.add(new Correspondence<RecordType, CorrespondenceType>(p1.getFirst(), p2.getFirst(), 1.0, createCausalCorrespondences(p1, p2)));
						counter++;
					}
				}
			
			}
		}

		calculatePerformance(dataset1, dataset2, result);
		return result;
	}
}
