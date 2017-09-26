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
import de.uni_mannheim.informatik.dws.winter.processing.FlattenAggregationResultMapper;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;

/**
 * 
 * A {@link RecordMapper} that returns the result of a list-aggregation operation without the grouping key as a single output list.
 * 
 * Removes all correspondences with a score of 0.0
 * 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class FlattenAggregatedCorrespondencesRule<KeyType, RecordType extends Matchable, CausalType extends Matchable> extends FlattenAggregationResultMapper<KeyType, Correspondence<RecordType, CausalType>> {

	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.FlattenAggregationResultMapper#mapRecord(de.uni_mannheim.informatik.wdi.model.Pair, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecord(Pair<KeyType, Processable<Correspondence<RecordType, CausalType>>> record,
			DataIterator<Correspondence<RecordType, CausalType>> resultCollector) {
		for(Correspondence<RecordType, CausalType> cor : record.getSecond().get()) {
			if(cor.getSimilarityScore()>0.0) {
				resultCollector.next(cor);
			}
		}
	}
	

}
