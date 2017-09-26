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
package de.uni_mannheim.informatik.dws.winter.processing;

import de.uni_mannheim.informatik.dws.winter.model.Pair;

/**
 * A {@link RecordMapper} that returns the result of a list-aggregation operation without the grouping key as a single output list 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class FlattenAggregationResultMapper<KeyType, RecordType> implements RecordMapper<Pair<KeyType, Processable<RecordType>>, RecordType> {

	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.RecordMapper#mapRecord(java.lang.Object, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecord(Pair<KeyType, Processable<RecordType>> record, DataIterator<RecordType> resultCollector) {
		for(RecordType r : record.getSecond().get()) {
			resultCollector.next(r);
		}
	}

	
	
}
