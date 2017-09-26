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
package de.uni_mannheim.informatik.dws.winter.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.CorrespondenceSet;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Represents a group of {@link Fusible}s.
 * 
 * If the fusion process uses schema correspondences (especially in the {@link AttributeValueFuser}s hasValue() method), you must extend this class and implement getSchemaCorrespondenceForRecord(record, schemaCorrespondences, schemaElement). This method must return the schema correspondence that an {@link AttributeValueFuser} should receive. To use the custom {@link RecordGroup}, create a {@link RecordGroupFactory} and set it before loading a {@link CorrespondenceSet}.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 */
public class RecordGroup<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> {

	private Map<String, FusibleDataSet<RecordType, SchemaElementType>> records;

	protected RecordGroup() {
		records = new HashMap<>();
	}

	/**
	 * Adds a record to this group
	 * 
	 * @param id
	 * @param dataset
	 */
	public void addRecord(String id, FusibleDataSet<RecordType, SchemaElementType> dataset) {
		records.put(id, dataset);
	}

	/**
	 * Adds all records from the provided group to this group
	 * 
	 * @param otherGroup
	 */
	public void mergeWith(RecordGroup<RecordType, SchemaElementType> otherGroup) {
		records.putAll(otherGroup.records);
	}

	/**
	 * 
	 * @return Returns the IDs of all records in this group
	 */
	public Set<String> getRecordIds() {
		return records.keySet();
	}

	/**
	 * 
	 * @return Returns the size of this group
	 */
	public int getSize() {
		return records.size();
	}

	/**
	 * 
	 * @return Returns all records of this group
	 */
	public Collection<RecordType> getRecords() {
		Collection<RecordType> result = new LinkedList<>();

		for (String id : records.keySet()) {
			DataSet<RecordType, SchemaElementType> ds = records.get(id);

			result.add(ds.getRecord(id));
		}

		return result;
	}

	/**
	 * 
	 * @return Returns all records of this group with their corresponding datasets
	 */
	public Collection<Pair<RecordType, FusibleDataSet<RecordType, SchemaElementType>>> getRecordsWithDataSets() {
		Collection<Pair<RecordType, FusibleDataSet<RecordType, SchemaElementType>>> result = new LinkedList<>();

		for (String id : records.keySet()) {
			FusibleDataSet<RecordType, SchemaElementType> ds = records.get(id);
			RecordType record = ds.getRecord(id);
			result.add(new Pair<>(record, ds));
		}

		return result;
	}
	
	/**
	 * 
	 * @param record
	 * @param schemaCorrespondences
	 * @return Returns the schema correspondence that connects the given record with the target schema
	 */
	public Correspondence<SchemaElementType, Matchable> getSchemaCorrespondenceForRecord(RecordType record, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, SchemaElementType schemaElement) {
		// this has to be implemented by the application using schema correspondences
		return null;
	}
}
