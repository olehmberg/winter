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

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataAggregator;
import de.uni_mannheim.informatik.dws.winter.utils.Distribution;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * 
 * {@link DataAggregator} that creates a frequency distribution of the values given by getInnerKey(record).
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class DistributionAggregator<KeyType, RecordType, InnerKeyType> implements DataAggregator<KeyType, RecordType, Distribution<InnerKeyType>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#initialise(java.lang.Object)
	 */
	@Override
	public Pair<Distribution<InnerKeyType>,Object> initialise(KeyType keyValue) {
		return stateless(new Distribution<>());
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#aggregate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Pair<Distribution<InnerKeyType>,Object> aggregate(Distribution<InnerKeyType> previousResult, RecordType record, Object state) {

		previousResult.add(getInnerKey(record));
		
		return stateless(previousResult);
	}

	public abstract InnerKeyType getInnerKey(RecordType record);

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.DataAggregator#merge(de.uni_mannheim.informatik.dws.winter.model.Pair, de.uni_mannheim.informatik.dws.winter.model.Pair)
	 */
	@Override
	public Pair<Distribution<InnerKeyType>, Object> merge(Pair<Distribution<InnerKeyType>, Object> intermediateResult1,
			Pair<Distribution<InnerKeyType>, Object> intermediateResult2) {

		Distribution<InnerKeyType> dist1 = intermediateResult1.getFirst();
		Distribution<InnerKeyType> dist2 = intermediateResult2.getFirst();
		
		Distribution<InnerKeyType> result = new Distribution<>();
		
		for(InnerKeyType elem : Q.union(dist1.getElements(), dist2.getElements())) {
			result.add(elem, dist1.getFrequency(elem));
			result.add(elem, dist2.getFrequency(elem));
		}
		
		return stateless(result);
	}
}
