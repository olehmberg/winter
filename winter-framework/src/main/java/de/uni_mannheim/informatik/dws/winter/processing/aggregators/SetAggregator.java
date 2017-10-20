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
package de.uni_mannheim.informatik.dws.winter.processing.aggregators;

import java.util.HashSet;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataAggregator;

/**
 * Aggregates all elements into a set (=only keeps unique records).
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class SetAggregator<KeyType, RecordType> implements DataAggregator<KeyType, RecordType, Set<RecordType>> {

	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#initialise(java.lang.Object)
	 */
	@Override
	public Pair<Set<RecordType>,Object> initialise(KeyType keyValue) {
		return stateless(new HashSet<>());
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#aggregate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Pair<Set<RecordType>,Object> aggregate(Set<RecordType> previousResult, RecordType record, Object state) {
		previousResult.add(record);
		
		return stateless(previousResult);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.DataAggregator#merge(de.uni_mannheim.informatik.dws.winter.model.Pair, de.uni_mannheim.informatik.dws.winter.model.Pair)
	 */
	@Override
	public Pair<Set<RecordType>, Object> merge(Pair<Set<RecordType>, Object> intermediateResult1,
			Pair<Set<RecordType>, Object> intermediateResult2) {

		Set<RecordType> result = intermediateResult1.getFirst();
		result.addAll(intermediateResult2.getFirst());
		
		return stateless(result);
	}
}
