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

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Super-class for matching rules for schema matching
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>	the type of records that are matched with this rule
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType
 */
public abstract class FilteringMatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable> extends MatchingRule<RecordType, SchemaElementType> {

	private static final long serialVersionUID = 1L;

	/**
	 * @param finalThreshold	the similarity threshold of this rule
	 */
	public FilteringMatchingRule(double finalThreshold) {
		super(finalThreshold);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.RecordKeyValueMapper#mapRecord(java.lang.Object, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecord(Correspondence<RecordType, SchemaElementType> record,
			DataIterator<Correspondence<RecordType, SchemaElementType>> resultCollector) {
		Correspondence<RecordType, SchemaElementType> cor = apply(record.getFirstRecord(), record.getSecondRecord(), record.getCausalCorrespondences());
		
		if(cor!=null && cor.getSimilarityScore()>0.0 && cor.getSimilarityScore()>=getFinalThreshold()) {
			resultCollector.next(cor);
		}
	}
	
	/**
	 * applies rule to the combination of first and second record and returns the correspondence between them if satisfies the criteria  
	 * @param record1
	 * 			the first record (must not be null)
	 * @param record2
	 * 			the second record (must not be null)
	 * @param schemaCorrespondences 
	 * 			the schema correspondences between the first and the second records
	 * @return the correspondence between the first and the second records
	 */
	public abstract Correspondence<RecordType, SchemaElementType> apply(RecordType record1,
			RecordType record2, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences); 
}
