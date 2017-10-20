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

import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * 
 * Interface for data sets. Specifies access to records and schema information.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>		the type that represents a record
 * @param <SchemaElementType>	the type that represents a schema element
 */
public interface DataSet<RecordType extends Matchable, SchemaElementType extends Matchable> extends Processable<RecordType> {

	/**
	 * 
	 * @param identifier
	 *            The identifier of the entry that should be returned
	 * @return Returns the entry with the specified identifier or null, if it is not found.
	 */
	public RecordType getRecord(String identifier);

	public SchemaElementType getAttribute(String identifier);
	
	/**
	 * 
	 * @return Returns a random record from the data set
	 */
	public RecordType getRandomRecord();

	/***
	 * Removes all records from this dataset
	 */
	public void ClearRecords();
	
	public void addAttribute(SchemaElementType attribute);
	
	public DataSet<SchemaElementType, SchemaElementType> getSchema();
	
	public void removeRecord(String identifier);
}