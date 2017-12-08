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
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;

/**
 * Super class for all matching rules.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type of records that are matched with this rule
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType
 */
public abstract class MatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable> 
	implements Comparator<RecordType,SchemaElementType>,
	RecordMapper<Correspondence<RecordType, SchemaElementType>, Correspondence<RecordType, SchemaElementType>>
{

	private static final long serialVersionUID = 1L;
	private double finalThreshold;

	public double getFinalThreshold() {
		return finalThreshold;
	}

	public void setFinalThreshold(double finalThreshold) {
		this.finalThreshold = finalThreshold;
	}

	public MatchingRule(double finalThreshold) {
		this.finalThreshold = finalThreshold;
	}
	
	public Correspondence<SchemaElementType, Matchable> getCorrespondenceForComparator(
			Processable<Correspondence<SchemaElementType, Matchable>> correspondences,
			RecordType record1,
			RecordType record2,
			Comparator<RecordType, SchemaElementType> comparator) {
		if(correspondences!=null) {
			Processable<Correspondence<SchemaElementType, Matchable>> matchingSchemaCorrespondences = correspondences
					// first filter correspondences to make sure we only use correspondences between the data sources of record1 and record2
				.where((c)->
					c.getFirstRecord().getDataSourceIdentifier()==record1.getDataSourceIdentifier()
					&&
					c.getSecondRecord().getDataSourceIdentifier()==record2.getDataSourceIdentifier()
					)
					// then filter the remaining correspondences based on the comparators arguments, if present
				.where((c)->
					(comparator.getFirstSchemaElement()==null || comparator.getFirstSchemaElement()==c.getFirstRecord())
					&&
					(comparator.getSecondSchemaElement()==null || comparator.getSecondSchemaElement()==c.getSecondRecord())
					);
			// after the filtering, there should only be one correspondence left (if not, the mapping is ambiguous)
			return matchingSchemaCorrespondences.firstOrNull();
		} else {
			return null;
		}
	}
}
