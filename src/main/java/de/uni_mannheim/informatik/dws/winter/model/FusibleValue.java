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

import java.time.LocalDateTime;

/**
 * Wrapper for a value during the data fusion process
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <ValueType>
 * @param <RecordType>
 */
public class FusibleValue<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> {

	private ValueType value;
	private RecordType record;
	private FusibleDataSet<RecordType, SchemaElementType> dataset;

	/**
	 * Creates a fusable value from the actual value, the source record and the
	 * source dataset
	 * 
	 * @param value
	 * @param record
	 * @param dataset
	 */
	public FusibleValue(ValueType value, RecordType record,
			FusibleDataSet<RecordType, SchemaElementType> dataset) {
		this.value = value;
		this.record = record;
		this.dataset = dataset;
	}

	/**
	 * 
	 * @return Returns the value
	 */
	public ValueType getValue() {
		return value;
	}

	/**
	 * 
	 * @return Returns the record that contains the value
	 */
	public RecordType getRecord() {
		return record;
	}

	/**
	 * 
	 * @return Returns the dataset that contains the value
	 */
	public FusibleDataSet<RecordType, SchemaElementType> getDataset() {
		return dataset;
	}

	/**
	 * 
	 * @return Returns the score of the dataset that contains the value
	 */
	public double getDataSourceScore() {
		return dataset.getScore();
	}

	/**
	 * 
	 * @return Returns the date of the dataset that contains the value
	 */
	public LocalDateTime getDateSourceDate() {
		return dataset.getDate();
	}

}
