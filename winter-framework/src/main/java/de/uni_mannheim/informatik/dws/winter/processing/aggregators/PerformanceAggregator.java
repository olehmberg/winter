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

import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.processing.DataAggregator;

/**
 * Aggregates {@link Performance} objects by summing up their values
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class PerformanceAggregator<KeyType> implements DataAggregator<KeyType, Performance, Performance> {

	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.DataAggregator#initialise(java.lang.Object)
	 */
	@Override
	public Performance initialise(KeyType keyValue) {
		return new Performance(0, 0, 0);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.DataAggregator#aggregate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Performance aggregate(Performance previousResult, Performance record) {
		
		return new Performance(
				previousResult.getNumberOfCorrectlyPredicted() + record.getNumberOfCorrectlyPredicted(), 
				previousResult.getNumberOfPredicted() + record.getNumberOfPredicted(), 
				previousResult.getNumberOfCorrectTotal() + record.getNumberOfCorrectTotal());
	}

}
