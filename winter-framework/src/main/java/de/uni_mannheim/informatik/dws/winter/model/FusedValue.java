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

/**
 * Represents a fused value in the data fusion process
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <ValueType>	the value type
 * @param <RecordType>	the type that represents a record
 * @param <SchemaElementType> the type that represents a schema element
 */
public class FusedValue<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> {

	private ValueType value;
	private Map<RecordType, FusibleDataSet<RecordType, SchemaElementType>> originalRecords = new HashMap<>();

	/**
	 * Creates a fused value without any provenance information
	 * 
	 * @param value	the value
	 */
	public FusedValue(ValueType value) {
		this.value = value;
	}

	/**
	 * Creates a fused value with the original record and dataset as provenance
	 * information
	 * 
	 * @param value	the value
	 */
	public FusedValue(FusibleValue<ValueType, RecordType, SchemaElementType> value) {
		if (value != null) {
			this.value = value.getValue();
			addOriginalRecord(value.getRecord(), value.getDataset());
		}
	}

	/**
	 * 
	 * @return Returns the fused value
	 */
	public ValueType getValue() {
		return value;
	}

	/**
	 * 
	 * @return Returns a map record -&gt; dataset containing all original records that are represented by this fused value
	 */
	public Map<RecordType, FusibleDataSet<RecordType, SchemaElementType>> getOriginalRecords() {
		return originalRecords;
	}

	/**
	 * 
	 * @return Returns the IDs of all original records that are represented by this fused value
	 */
	public Collection<String> getOriginalIds() {
		Collection<String> result = new LinkedList<>();
		for (RecordType record : getOriginalRecords().keySet()) {
			result.add(record.getIdentifier());
		}
		return result;
	}

	/**
	 * Adds an original record as provenance information
	 * 
	 * @param record	the record
	 * @param dataset	the dataset that contains the record
	 */
	public void addOriginalRecord(RecordType record,
			FusibleDataSet<RecordType, SchemaElementType> dataset) {
		originalRecords.put(record, dataset);
	}

	/**
	 * Adds an original record as provenance information
	 * 
	 * @param value	the value that was generated from the original record
	 */
	public void addOriginalRecord(FusibleValue<ValueType, RecordType, SchemaElementType> value) {
		originalRecords.put(value.getRecord(), value.getDataset());
	}
}
