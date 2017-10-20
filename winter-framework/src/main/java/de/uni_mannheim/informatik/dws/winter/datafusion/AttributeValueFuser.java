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
package de.uni_mannheim.informatik.dws.winter.datafusion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.clustering.ConnectedComponentClusterer;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Abstract super class for all Fusers tailored to specific attributes (hence the ValueType). Ignores schema correspondences.
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <ValueType>	the type of the values that are fused
 * @param <RecordType>	the type that represents a record
 */
public abstract class AttributeValueFuser<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends AttributeFuser<RecordType, SchemaElementType> {

	/**
	 * Collects all fusable values from the group of records
	 * @param group	the group of records to use
	 * @param schemaCorrespondences	optional schema correspondences
	 * @param schemaElement	the schema element for which to collect the values
	 * @return A list of fusable values
	 */
	protected List<FusibleValue<ValueType, RecordType, SchemaElementType>> getFusableValues(RecordGroup<RecordType, SchemaElementType> group, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, SchemaElementType schemaElement) {
		List<FusibleValue<ValueType, RecordType, SchemaElementType>> values = new LinkedList<>();
		
		for(Pair<RecordType, FusibleDataSet<RecordType, SchemaElementType>> p : group.getRecordsWithDataSets()) {
			// only consider existing values
			RecordType record = p.getFirst();
			Correspondence<SchemaElementType, Matchable> correspondence = group.getSchemaCorrespondenceForRecord(p.getFirst(), schemaCorrespondences, schemaElement); 
			if(hasValue(record, correspondence)) {
				ValueType v = getValue(record, correspondence);
				FusibleValue<ValueType, RecordType, SchemaElementType> value = new FusibleValue<ValueType, RecordType, SchemaElementType>(v, p.getFirst(), p.getSecond());
				values.add(value);
			}
		}
		
		return values;
	}
	
	/**
	 * returns the value that is used by this fuser from the given record. Required for the collection of fusable values.
	 * @param record	the record to get the value from
	 * @return The value to fuse
	 */
	protected abstract ValueType getValue(RecordType record, Correspondence<SchemaElementType, Matchable> correspondence);
	
	@Override
	public Double getConsistency(RecordGroup<RecordType, SchemaElementType> group,
			EvaluationRule<RecordType, SchemaElementType> rule, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, SchemaElementType schemaElement) {
		
		List<RecordType> records = new ArrayList<>(group.getRecords());

		// remove non-existing values
		Iterator<RecordType> it = records.iterator();
		while(it.hasNext()) {
			RecordType record = it.next();
			Correspondence<SchemaElementType, Matchable> cor = group.getSchemaCorrespondenceForRecord(record, schemaCorrespondences, schemaElement);

			if(!hasValue(record, cor)) {
				it.remove();
			}
		}
		
		if(records.size()==0) {
			return null;
		} else if(records.size()==1) {
			return 1.0; // this record group is consistent, as there is only one value
		}
		
		ConnectedComponentClusterer<RecordType> con = new ConnectedComponentClusterer<>();
		
		// calculate pair-wise similarities
		for(int i=0; i<records.size();i++) {
			RecordType r1 = records.get(i);
			Correspondence<SchemaElementType, Matchable> cor1 = group.getSchemaCorrespondenceForRecord(r1, schemaCorrespondences, schemaElement);
			if(cor1!=null) {
				for(int j=i+1; j<records.size(); j++) {
					RecordType r2 = records.get(j);
					Correspondence<SchemaElementType, Matchable> cor2 = group.getSchemaCorrespondenceForRecord(r2, schemaCorrespondences, schemaElement);
				
					if(cor2!=null && !con.isEdgeAlreadyInCluster(r1, r2)) {

						// assumption: in fusion we have a target schema, so all schema correspondences refer to the target schema 
						// this means that we can simply combine both schema correspondences to get a schema correspondence between the two records
						Correspondence<SchemaElementType, Matchable> cor = Correspondence.<SchemaElementType, Matchable>combine(cor1, cor2);
						
						if(rule.isEqual(r1, r2, cor)) {
							con.addEdge(new Triple<>(r1, r2, 1.0));
						}
					
					}
				}
			}
		}
		
		Map<Collection<RecordType>, RecordType> clusters = con.createResult();
		int largestClusterSize = 0;
		for(Collection<RecordType> cluster : clusters.keySet()) {
			if(cluster.size()>largestClusterSize) {
				largestClusterSize = cluster.size();
			}
		}
		
		if(largestClusterSize>group.getSize()) {
			System.out.println("Wrong cluster!");
		}
		
		return (double)largestClusterSize / (double)records.size();
	}
	
	private ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> conflictResolution;
	
	/**
	 * Creates an instance, specifies the conflict resolution function to use
	 * @param conflictResolution	the conflict resolution function
	 */
	public AttributeValueFuser(ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> conflictResolution) {
		this.conflictResolution = conflictResolution;
	}

	/**
	 * 
	 * Returns the fused value by applying the conflict resolution function to the list of fusible values
	 * 
	 * @param group		the group of records
	 * @param schemaCorrespondences	the schema correspondences
	 * @param schemaElement	the schema element for which the value should be returned
	 * @return	returns the fused value for a given schema element
	 */
	protected FusedValue<ValueType, RecordType, SchemaElementType> getFusedValue(RecordGroup<RecordType, SchemaElementType> group, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, SchemaElementType schemaElement) {
		return conflictResolution.resolveConflict(getFusableValues(group, schemaCorrespondences, schemaElement));
	}
}
