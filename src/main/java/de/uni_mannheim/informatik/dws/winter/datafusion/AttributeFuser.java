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

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Abstract super class for all Fusers used by a fusion strategy
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>	the type that represents a record
 */
public abstract class AttributeFuser<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> {

	/**
	 * fuses the group of records and assigns values to the fused Record
	 * @param group the group of values to be fused (input)
	 * @param fusedRecord the fused record (output)
	 * @param schemaCorrespondences	the schema correspondences
	 * @param schemaElement	the schema element to fuse
	 */
	public abstract void fuse(RecordGroup<RecordType, SchemaElementType> group, RecordType fusedRecord, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, SchemaElementType schemaElement);	
	
	/**
	 * Determines whether the record has a value for the attribute that is used by this fuser. Required for the collection of fusable values.
	 * @param record	the record
	 * @param correspondence	the correspondence that specifies the schema element
	 * @return true if the record has a value for the attribute in the correspondence
	 */
	public abstract boolean hasValue(RecordType record, Correspondence<SchemaElementType, Matchable> correspondence);
	
	/**
	 * Determines if the given group of records has conflicting values
	 * @param group		the record group
	 * @param rule		the evaluation rule
	 * @param schemaCorrespondences	the schema correspondences
	 * @param schemaElement	the schema element
	 * @return the consistency value if any values are available. NULL if no values are available
	 */
	public abstract Double getConsistency(RecordGroup<RecordType, SchemaElementType> group, EvaluationRule<RecordType, SchemaElementType> rule, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, SchemaElementType schemaElement);

}
