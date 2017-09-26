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
package de.uni_mannheim.informatik.dws.winter.matching.blockers;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * 
 * Interface for a blocker that generates pairs of records from two data sets
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>
 * 			The type of Records in the input dataset(s)
 * @param <SchemaElementType>
 * 			The type of Schema Elements in the input dataset(s)
 * @param <BlockedType>
 * 			The type of Records in the Correspondences (Pairs) that are the result of the blocking
 * @param <CorrespondenceType>
 * 			The type of Records in the causes of the Correspondences (Pairs) that are the result of the blocking
 */
public interface Blocker<RecordType extends Matchable, SchemaElementType extends Matchable, BlockedType extends Matchable, CorrespondenceType extends Matchable> {

	/**
	 * Gives the possible candidates for the correspondences between the first and second data sets.
	 * @param dataset1
	 * 			the first data set (must not be null)
	 * @param dataset2
	 * 			the second data set (must not be null)
	 * @param schemaCorrespondences
	 * 			schema correspondences between the first and second data sets (must not be null)
	 * @return the blocked pairs between the first and second data sets.
	 */
	Processable<Correspondence<BlockedType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset1, 
			DataSet<RecordType, SchemaElementType> dataset2, 
			Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences);
	
	double getReductionRatio();
}
