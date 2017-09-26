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
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollector;
import de.uni_mannheim.informatik.dws.winter.processing.RecordKeyValueMapper;

/**
 * 
 * A matching rule that specifies how correspondences are grouped for aggregation.
 * 
 * If a correspondence has a score below finalThreshold, its score is set to 0.0 instead of removing it to allow for correct aggregation of the scores in a followin step (i.e. average score)
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class AggregableMatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable> extends MatchingRule<RecordType, SchemaElementType> 
	implements RecordKeyValueMapper<Pair<RecordType, RecordType>, Correspondence<RecordType, SchemaElementType>, Correspondence<RecordType, SchemaElementType>>
{
	
	private static final long serialVersionUID = 1L;
	
	public AggregableMatchingRule(double finalThreshold) {
		super(finalThreshold);
	}
	
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.RecordKeyValueMapper#mapRecordToKey(java.lang.Object, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecordToKey(Correspondence<RecordType, SchemaElementType> record,
			DataIterator<Pair<Pair<RecordType, RecordType>, Correspondence<RecordType, SchemaElementType>>> resultCollector) {
		
		ProcessableCollector<Correspondence<RecordType, SchemaElementType>> collector = new ProcessableCollector<>();
		
		collector.setResult(new ProcessableCollection<>());
		collector.initialise();
		
		mapRecord(record, collector);
		
		collector.finalise();
		
		for(Correspondence<RecordType, SchemaElementType> cor : collector.getResult().get()) {
			if(cor.getSimilarityScore()<getFinalThreshold()) {
				// important for vote aggregation: if a correspondence is below the similarity threshold, 
				// keep it with score 0.0 such that the number of correspondences is not changed (the aggregation step might need this number)
				// as we don't want to change an existing correspondence (which be used somewhere else), we must create a new correspondence with the changed score
				cor = new Correspondence<>(cor.getFirstRecord(), cor.getSecondRecord(), 0.0, cor.getCausalCorrespondences());
			}
			
			resultCollector.next(new Pair<>(generateAggregationKey(cor), cor));
		}
	}

	protected Pair<RecordType, RecordType> generateAggregationKey(Correspondence<RecordType, SchemaElementType> cor) {
		return new Pair<RecordType, RecordType>(cor.getFirstRecord(), cor.getSecondRecord());
	}
}
