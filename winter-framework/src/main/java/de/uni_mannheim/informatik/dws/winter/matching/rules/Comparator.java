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
package de.uni_mannheim.informatik.dws.winter.matching.rules;

import java.io.Serializable;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorJaccard;

/**
 * Interface for all {@link AbstractRecord} comparators.
 * 
 * A Comparator calculates a similarity value for two given records and an optional correspondence. 
 * Implementations can test the values of specific attributes or use the correspondence to determine which values to compare.
 * 
 * For an example of a specific attribute comparator, see {@link RecordComparatorJaccard}.
 * 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type of records that are compared with this comparator
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType
 */
public interface Comparator<RecordType extends Matchable, SchemaElementType extends Matchable> extends Serializable {

	/**
	 * Compares two records and returns a similarity value
	 * 
	 * @param record1
	 *            the first record (must not be null)
	 * @param record2
	 *            the second record (must not be null)
	 * @param schemaCorrespondence
	 * 			  A schema correspondence between two record1 and record2 (can be null)  
	 * @return the similarity of the records
	 */
	double compare(RecordType record1, RecordType record2, Correspondence<SchemaElementType, Matchable> schemaCorrespondence);

}
