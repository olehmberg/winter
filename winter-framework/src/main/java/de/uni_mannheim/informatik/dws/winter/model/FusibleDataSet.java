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
import java.util.Map;

/**
 * {@link DataSet} extended by functionality for data fusion
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type that represents a record
 * @param <SchemaElementType>	the type that represents a schema element
 */
public interface FusibleDataSet<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends
		DataSet<RecordType, SchemaElementType> {

	/**
	 * Add an original ID to a fused record (can be called multiple times)
	 * 
	 * @param record	the record for which to set the id
	 * @param id	the id
	 */
	void addOriginalId(RecordType record, String id);

	/**
	 * 
	 * @return Returns the score of this dataset
	 */
	double getScore();

	/**
	 * Sets the score of this dataset
	 * 
	 * @param score	the score
	 */
	void setScore(double score);

	/**
	 * 
	 * @return Returns the date of this dataset
	 */
	LocalDateTime getDate();

	/**
	 * Sets the date of this dataset
	 * 
	 * @param date	the date
	 */
	void setDate(LocalDateTime date);

	/**
	 * 
	 * @return Calculates the overall density of this dataset
	 */
	double getDensity();

	/**
	 * 
	 * @param record	the record for which to return the number of values
	 * @return Returns the number of attributes that have a value for the given record
	 */
	int getNumberOfValues(RecordType record);

	/**
	 * 
	 * @param record	the record for which to return the number of attributes
	 * @return Returns the number of attributes for the given record
	 */
	int getNumberOfAttributes(RecordType record);

	/**
	 * 
	 * @return Calculates the density for all attributes of the records in this dataset
	 */
	Map<SchemaElementType, Double> getAttributeDensities();

	/**
	 * Calculates the density for all attributes of the records in this dataset
	 * and prints the result to the console
	 */
	void printDataSetDensityReport();

}
