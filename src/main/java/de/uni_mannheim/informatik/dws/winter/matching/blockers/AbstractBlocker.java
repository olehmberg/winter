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

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Group;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.RecordKeyValueMapper;

/**
 * The super class for all blocking strategies. The generation of pairs
 * based on the {@link AbstractBlocker} can be executed for one dataset, by implementing {@link SymmetricBlocker} or for two
 * datasets, by implementing {@link Blocker}
 * resolution.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * @author Robert Meusel (robert@dwslab.de)
 * 
 * @param <RecordType>
 * 			The type of Records in the input dataset(s)
 * @param <BlockedType>
 * 			The type of Records in the Correspondences (Pairs) that are the result of the blocking
 * @param <CorrespondenceType>
 * 			The type of Records in the causes of the Correspondences (Pairs) that are the result of the blocking
 */
public abstract class AbstractBlocker<RecordType extends Matchable, BlockedType extends Matchable, CorrespondenceType extends Matchable>
{

	private double reductionRatio = 1.0;

	/**
	 * Returns the reduction ratio of the last blocking operation. Only
	 * available after calculatePerformance(...) has been called.
	 * 
	 * @return the reduction ratio
	 */
	public double getReductionRatio() {
		return reductionRatio;
	}
	
	private Processable<Correspondence<BlockedType, CorrespondenceType>> result;
	/**
	 * @param result the result to set
	 */
	protected void setResult(Processable<Correspondence<BlockedType, CorrespondenceType>> result) {
		this.result = result;
	}
	
	/**
	 * 
	 * 
	 * @return Returns the result of the blocking operation.
	 */
	public Processable<Correspondence<BlockedType, CorrespondenceType>> getBlockedPairs() {
		return result;
	}
	
	/**
	 * Calculates the reduction ratio. Must be called by all sub classes in
	 * generatePairs(...).
	 * 
	 * @param dataset1
	 *            the first data set (must not be null)
	 * @param dataset2
	 *            the second data set (must not be null)
	 * @param blocked
	 *            the list of pairs that resulted from the blocking (must not be null)
	 */
	protected void calculatePerformance(Processable<? extends Matchable> dataset1,
			Processable<? extends Matchable> dataset2,
			Processable<? extends Correspondence<? extends Matchable, ? extends Matchable>> blocked) {
		long size1 = (long) dataset1.size();
		long size2 = (long) dataset2.size();
		long maxPairs = size1 * size2;

		reductionRatio = 1.0 - ((double)blocked.size() / (double)maxPairs);
	}
	
	public 
	Processable<Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>> 
	combineDataWithCorrespondences(
			Processable<RecordType> dataset1, 
			Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences,
			RecordKeyValueMapper<Object, Correspondence<CorrespondenceType, Matchable>, Correspondence<CorrespondenceType, Matchable>> correspondenceJoinKey) {
		
		if(schemaCorrespondences!=null) {
			// group the schema correspondences by data source (if no data sources are defined, all schema correspondences are used)
			Processable<Group<Object, Correspondence<CorrespondenceType, Matchable>>> leftCors = schemaCorrespondences.group(correspondenceJoinKey);
	
			// join the dataset with the correspondences
			Processable<Pair<RecordType, Group<Object, Correspondence<CorrespondenceType, Matchable>>>> joined = dataset1.leftJoin(leftCors, (r)->r.getDataSourceIdentifier(), (r)->r.getKey());
			
			return joined.map((p,c)-> {
				if(p.getSecond()!=null) {
					c.next(new Pair<>(p.getFirst(), p.getSecond().getRecords()));
				} else {
					c.next(new Pair<>(p.getFirst(), null));
				}
				
			});
		} else {
			return dataset1.map((r,c)->c.next(new Pair<>(r,null)));
		}
	}
	
	protected Processable<Correspondence<CorrespondenceType, Matchable>> createCausalCorrespondences(
			Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>> p1,
			Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>> p2) {
		return new ProcessableCollection<>(p1.getSecond()).append(p2.getSecond()).distinct();
	}
}
