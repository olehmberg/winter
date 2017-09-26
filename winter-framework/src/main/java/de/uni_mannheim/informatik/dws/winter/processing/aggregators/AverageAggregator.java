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

import de.uni_mannheim.informatik.dws.winter.processing.DataAggregator;

/**
 * 
 * {@link DataAggregator} that calculates the average.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class AverageAggregator<KeyType, RecordType> implements DataAggregator<KeyType, RecordType, Double> {

	private static final long serialVersionUID = 1L;

	int numberofElements = -1;
	boolean fixedNumberOfElements = false;
	
	public AverageAggregator() {
		this.fixedNumberOfElements = false;
	}
	
	public AverageAggregator(int numberofElements) {
		this.numberofElements = numberofElements;
		this.fixedNumberOfElements = true;
	}
	
	@Override
	public Double aggregate(Double previousResult,
			RecordType record) {
		if(!fixedNumberOfElements) {
			numberofElements++;
		}
		if(previousResult==null) {
			return getValue(record);
		} else {
			return previousResult+getValue(record);
		}
	}
	
	protected abstract Double getValue(RecordType record);

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#initialise(java.lang.Object)
	 */
	@Override
	public Double initialise(KeyType keyValue) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#createFinalValue(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Double createFinalValue(KeyType keyValue, Double result) {
		return result / (double)numberofElements;
	}
	
}
