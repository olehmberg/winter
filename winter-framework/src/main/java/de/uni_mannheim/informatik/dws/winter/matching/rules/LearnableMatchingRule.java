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

import java.io.File;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.FeatureVectorDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public interface LearnableMatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable> {

	/**
	 * creates the record with the respective features
	 * @param record1
	 * 			the first record (must not be null)
	 * @param record2
	 * 			the second record (must not be null)
	 * @param schemaCorrespondences 
	 * 			the schema correspondences between the first and the second records
	 * @param features feature vector (must not be null)
	 * @return the record containing the respective features
	 */
	Record generateFeatures(RecordType record1,
			RecordType record2, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, FeatureVectorDataSet features);
	
	FeatureVectorDataSet initialiseFeatures();
	
	Performance learnParameters(FeatureVectorDataSet features);
	
	void storeModel(File location);
	void readModel(File location);
	
}
