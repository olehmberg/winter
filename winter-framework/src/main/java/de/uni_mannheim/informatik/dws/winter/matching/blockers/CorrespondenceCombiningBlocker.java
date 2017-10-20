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

import java.util.Iterator;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * 
 * Combines the provided record correspondences with the schema correspondences.
 * 
 * Ignores the provided datasets.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>			the type of records which are the input for the blocking operation
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType 
 * @param <CorrespondenceType>	the type of correspondences which are the input for the blocking operation
 * @param <BlockedType>			the type of record which is actually blocked
 */
public class CorrespondenceCombiningBlocker<RecordType extends Matchable, SchemaElementType extends Matchable, BlockedType extends Matchable, CorrespondenceType extends Matchable> 
	extends AbstractBlocker<RecordType, BlockedType, CorrespondenceType>
	implements Blocker<RecordType, SchemaElementType, BlockedType, CorrespondenceType>,
	SymmetricBlocker<RecordType, SchemaElementType, BlockedType, CorrespondenceType>
{

	private boolean createCorrespondencesWithIdenticalDataSource = true;
	/**
	 * @param createCorrespondencesWithIdenticalDataSource the createCorrespondencesWithIdenticalDataSource to set
	 */
	public void setCreateCorrespondencesWithIdenticalDataSource(boolean createCorrespondencesWithIdenticalDataSource) {
		this.createCorrespondencesWithIdenticalDataSource = createCorrespondencesWithIdenticalDataSource;
	}
	
	protected Processable<Correspondence<BlockedType, CorrespondenceType>> correspondences;
	
	/**
	 * @param correspondences the correspondences to set
	 */
	public void setCorrespondences(Processable<Correspondence<BlockedType, CorrespondenceType>> correspondences) {
		this.correspondences = correspondences;
	}
	
	public CorrespondenceCombiningBlocker(Processable<Correspondence<BlockedType, CorrespondenceType>> correspondences) {
		this.correspondences = correspondences;
	}

	public CorrespondenceCombiningBlocker() {
		
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.SingleDataSetBlocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, boolean, de.uni_mannheim.informatik.wdi.model.Result, de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine)
	 */
	@Override
	public Processable<Correspondence<BlockedType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset, Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences) {
		
		Processable<Correspondence<BlockedType, CorrespondenceType>> result;
		
		if(schemaCorrespondences!=null) {
			// co-group the record pairs with the schema correspondences
			// (1) results in a join of the record pairs with the schema correspondences, which allows this operation to be executed distributedly
			// (2) filters out schema correspondences for other data sources (if multiple data sources are used for the same Dataset
			result = correspondences.coGroup(
				schemaCorrespondences, 
				(p) -> Q.toSet(p.getFirstRecord().getDataSourceIdentifier(), p.getSecondRecord().getDataSourceIdentifier()), 
				(c) -> Q.toSet(c.getFirstRecord().getDataSourceIdentifier(), c.getSecondRecord().getDataSourceIdentifier()),
				(p, c) -> {
					// collect all schema correspondences
					Processable<Correspondence<CorrespondenceType, Matchable>> causes = new ProcessableCollection<>();
					Iterator<Correspondence<CorrespondenceType, Matchable>> it = p.getSecond().iterator();
					while(it.hasNext()) {
						causes.add(it.next());
					}
					// create a correspondence for each record pair
					for(Correspondence<BlockedType, CorrespondenceType> pair : p.getFirst()) {
						
						if(createCorrespondencesWithIdenticalDataSource || pair.getFirstRecord().getDataSourceIdentifier()!=pair.getSecondRecord().getDataSourceIdentifier()) {
							c.next(new Correspondence<BlockedType, CorrespondenceType>(pair.getFirstRecord(), pair.getSecondRecord(), 1.0, causes));
						}
					}					
				});
		} else {
			// no schema correspondences available, so just produce the record pairs as output
			result = correspondences.map((p, collector) -> {
				if(createCorrespondencesWithIdenticalDataSource || p.getFirstRecord().getDataSourceIdentifier()!=p.getSecondRecord().getDataSourceIdentifier()) {
					collector.next(new Correspondence<BlockedType, CorrespondenceType>(p.getFirstRecord(), p.getSecondRecord(), 1.0, null));
				}
			});	
		}	

		calculatePerformance(dataset, dataset, result);
		
		setResult(result);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.CrossDataSetBlocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.Result, de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine)
	 */
	@Override
	public Processable<Correspondence<BlockedType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset1, DataSet<RecordType, SchemaElementType> dataset2,
			Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences) {
		
		Processable<Correspondence<BlockedType, CorrespondenceType>> result;
		
		if(schemaCorrespondences!=null) {
			// co-group the record pairs with the schema correspondences
			// (1) results in a join of the record pairs with the schema correspondences, which allows this operation to be executed distributedly
			// (2) filters out schema correspondences for other data sources (if multiple data sources are used for the same Dataset
			result = correspondences.coGroup(
				schemaCorrespondences, 
				(p) -> Q.toSet(p.getFirstRecord().getDataSourceIdentifier(), p.getSecondRecord().getDataSourceIdentifier()), 
				(c) -> Q.toSet(c.getFirstRecord().getDataSourceIdentifier(), c.getSecondRecord().getDataSourceIdentifier()),
				(p, c) -> {
					// collect all schema correspondences
					Processable<Correspondence<CorrespondenceType, Matchable>> causes = new ProcessableCollection<>();
					Iterator<Correspondence<CorrespondenceType, Matchable>> it = p.getSecond().iterator();
					while(it.hasNext()) {
						causes.add(it.next());
					}
					// create a correspondence for each record pair
					for(Correspondence<BlockedType, CorrespondenceType> pair : p.getFirst()) {
						if(createCorrespondencesWithIdenticalDataSource || pair.getFirstRecord().getDataSourceIdentifier()!=pair.getSecondRecord().getDataSourceIdentifier()) {
							c.next(new Correspondence<BlockedType, CorrespondenceType>(pair.getFirstRecord(), pair.getSecondRecord(), 1.0, causes));
						}
					}					
				});
		} else {
			// no schema correspondences available, so just produce the record pairs as output
			result = correspondences.map((p, collector) -> {
				if(createCorrespondencesWithIdenticalDataSource || p.getFirstRecord().getDataSourceIdentifier()!=p.getSecondRecord().getDataSourceIdentifier()) {
					collector.next(new Correspondence<BlockedType, CorrespondenceType>(p.getFirstRecord(), p.getSecondRecord(), 1.0, null));
				}
			});	
		}		

		calculatePerformance(dataset1, dataset2, result);
		
		return result;
	}
	
}
