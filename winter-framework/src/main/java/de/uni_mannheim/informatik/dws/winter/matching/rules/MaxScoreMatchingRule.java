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

import java.util.Collection;
import java.util.LinkedList;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * A matching rule that combines several other matching rules.
 * 
 *  Each individual matching rule is executed and the result with the highest similarity score is used as result. 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class MaxScoreMatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable> extends FilteringMatchingRule<RecordType, SchemaElementType> {

	private Collection<FilteringMatchingRule<RecordType, SchemaElementType>> rules;
	
	/**
	 * @param finalThreshold	the similariy threshold for this rule
	 */
	public MaxScoreMatchingRule(double finalThreshold) {
		super(finalThreshold);
		rules = new LinkedList<>();
	}

	public void addMatchingRule(FilteringMatchingRule<RecordType, SchemaElementType> rule) {
		rules.add(rule);
	}
	
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator#compare(de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.model.Correspondence)
	 */
	@Override
	public double compare(RecordType record1, RecordType record2,
			Correspondence<SchemaElementType, Matchable> schemaCorrespondence) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.FilteringMatchingRule#apply(de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.processing.Processable)
	 */
	@Override
	public Correspondence<RecordType, SchemaElementType> apply(RecordType record1, RecordType record2,
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {
		
		Correspondence<RecordType, SchemaElementType> max = null;
		
		for(FilteringMatchingRule<RecordType, SchemaElementType> rule : rules) {
			Correspondence<RecordType, SchemaElementType> cor = rule.apply(record1, record2, schemaCorrespondences);
			
			if(
					cor.getSimilarityScore() >= rule.getFinalThreshold() &&
					(max==null ||  cor.getSimilarityScore() > max.getSimilarityScore())) {
				max = cor;
			}
		}
		
		return max;
	}

}
