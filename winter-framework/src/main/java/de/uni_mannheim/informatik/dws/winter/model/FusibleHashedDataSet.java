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
import java.util.HashMap;
import java.util.Map;

/**
 * {@link HashedDataSet} class extended by functionalities for data fusion
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 */
public class FusibleHashedDataSet<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends
		HashedDataSet<RecordType, SchemaElementType> implements FusibleDataSet<RecordType, SchemaElementType> {

	private static final long serialVersionUID = 1L;
	private double score;
	private LocalDateTime date;
	
	private Map<String, RecordType> originalIdIndex = new HashMap<>();

	/**
	 * Add an original ID to a fused record (can be called multiple times)
	 * 
	 * @param record
	 * @param id
	 */
	public void addOriginalId(RecordType record, String id) {
		originalIdIndex.put(id, record);
	}

	@Override
	public RecordType getRecord(String identifier) {
		RecordType record = super.getRecord(identifier);

		if (record == null) {
			record = originalIdIndex.get(identifier);
		}

		return record;
	}

	/**
	 * 
	 * @return Returns the score of this dataset
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Sets the score of this dataset
	 * 
	 * @param score
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * 
	 * @return Returns the date of this dataset
	 */
	public LocalDateTime getDate() {
		return date;
	}

	/**
	 * Sets the date of this dataset
	 * 
	 * @param date
	 */
	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	/**
	 * 
	 * @return Calculates the overall density of this dataset
	 */
	public double getDensity() {
		int values = 0;
		int attributes = 0;

		for (RecordType record : get()) {
			values += getNumberOfValues(record);
			attributes += getNumberOfAttributes(record);
		}

		return (double) values / (double) attributes;
	}

	/**
	 * 
	 * @param record
	 * @return Returns the number of attributes that have a value for the given record
	 */
	public int getNumberOfValues(RecordType record) {
		int cnt = 0;
		for (SchemaElementType att : getSchema().get()) {
			cnt += record.hasValue(att) ? 1 : 0;
		}
		return cnt;
	}

	/**
	 * 
	 * @param record
	 * @return Returns the number of attributes for the given record
	 */
	public int getNumberOfAttributes(RecordType record) {
		return getSchema().size();
	}

	/**
	 * 
	 * @return Calculates the density for all attributes of the records in this dataset
	 */
	public Map<SchemaElementType, Double> getAttributeDensities() {
		// counts how often the attribute exists (should be equal to the number
		// of records
		Map<SchemaElementType, Integer> sizes = new HashMap<>();
		// counts how often the attribute has a value
		Map<SchemaElementType, Integer> values = new HashMap<>();

		for (RecordType record : get()) {

			for (SchemaElementType att : getSchema().get()) {

				Integer size = sizes.get(att);
				if (size == null) {
					size = 0;
				}
				sizes.put(att, size + 1);

				if (record.hasValue(att)) {
					Integer value = values.get(att);
					if (value == null) {
						value = 0;
					}
					values.put(att, value + 1);
				}
			}

		}

		Map<SchemaElementType, Double> result = new HashMap<>();

		for (SchemaElementType att : sizes.keySet()) {
			Integer valueCount = values.get(att);
			if (valueCount == null) {
				valueCount = 0;
			}
			double density = (double) valueCount / (double) sizes.get(att);
			result.put(att, density);
		}

		return result;
	}

	/**
	 * Calculates the density for all attributes of the records in this dataset
	 * and prints the result to the console
	 */
	public void printDataSetDensityReport() {
		System.out
				.println(String.format("DataSet density: %.2f", getDensity()));
		System.out.println("Attributes densities:");
		Map<SchemaElementType, Double> densities = getAttributeDensities();
		for (SchemaElementType att : densities.keySet()) {
			System.out.println(String.format("\t%s: %.2f", att.toString(),
					densities.get(att)));
		}
	}

}
