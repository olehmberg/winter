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
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;

/**
 * 
 * A matching rule that generates a vote for every causal correspondence that is passed and aggregates these votes with the provided voting strategy.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class VotingMatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable> 
	extends AggregableMatchingRule<RecordType, SchemaElementType>
{

	private static final long serialVersionUID = 1L;

	public VotingMatchingRule(double finalThreshold) {
		super(finalThreshold);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.MatchingRule#mapRecord(de.uni_mannheim.informatik.wdi.model.Correspondence, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecord(Correspondence<RecordType, SchemaElementType> record,
			DataIterator<Correspondence<RecordType, SchemaElementType>> resultCollector) {
		
		// create one correspondence for each causal correspondence
		for(Correspondence<SchemaElementType, Matchable> cor : record.getCausalCorrespondences().get()) {
			Correspondence<RecordType, SchemaElementType> newCor = apply(record.getFirstRecord(), record.getSecondRecord(), cor);
			
			if(newCor!=null) {
				resultCollector.next(newCor);
			}
		}
	}
	
	
	public Correspondence<RecordType, SchemaElementType> apply(RecordType record1,
			RecordType record2, Correspondence<SchemaElementType, Matchable> correspondence) {
		double sim = compare(record1, record2, correspondence);
		
		Processable<Correspondence<SchemaElementType, Matchable>> cause = new ProcessableCollection<>();
		cause.add(correspondence);
		return new Correspondence<RecordType, SchemaElementType>(record1, record2, sim, cause);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.Comparator#compare(java.lang.Object, java.lang.Object, de.uni_mannheim.informatik.wdi.model.Correspondence)
	 */
	@Override
	public abstract double compare(RecordType record1, RecordType record2, Correspondence<SchemaElementType, Matchable> correspondence);

}
