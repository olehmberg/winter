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
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * Abstract super class for all evaluation rules
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>	the type that represents a record
 */
public abstract class EvaluationRule<RecordType extends Matchable, SchemaElementType extends Matchable> {

	/**
	 * 
	 * @param record1	the first record
	 * @param record2	the second record
	 * @param schemaElement	the schema element to compare
	 * @return Returns whether the values of the given records are equal according to this evaluation rule (assuming both records are in the target schema)
	 */
	public abstract boolean isEqual(RecordType record1, RecordType record2, SchemaElementType schemaElement);
	
	/**
	 * 
	 * 
	 * @param record1	the first record
	 * @param record2	the second record
	 * @param schemaCorrespondence	the schema correspondence that specifies which schema elements to compare
	 * @return Returns whether the values of the given records are equal according to this evaluation rule (assuming the records are in different schemas)
	 */
	public abstract boolean isEqual(RecordType record1, RecordType record2, Correspondence<SchemaElementType, Matchable> schemaCorrespondence);
	
	
}
