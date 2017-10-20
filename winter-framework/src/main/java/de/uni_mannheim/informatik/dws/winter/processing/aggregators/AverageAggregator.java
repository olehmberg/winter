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

/**
 * 
 * {@link DataAggregator} that calculates the average.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class AverageAggregator<KeyType, RecordType> implements DataAggregator<KeyType, RecordType, Double> {

	private static final long serialVersionUID = 1L;

	int numberOfElements = -1;
	boolean fixedNumberOfElements = false;
	
	public AverageAggregator() {
		this.fixedNumberOfElements = false;
	}
	
	public AverageAggregator(int numberofElements) {
		this.numberOfElements = numberofElements;
		this.fixedNumberOfElements = true;
	}
	
	@Override
	public Pair<Double,Object> aggregate(Double previousResult,
			RecordType record, Object state) {
		
		Double result = null;
		if(previousResult==null) {
			result = getValue(record);
		} else {
			result =  previousResult+getValue(record);
		}
		
		if(!fixedNumberOfElements) {
			Integer numberOfElements = (Integer)state;
			numberOfElements++;
			return state(result,numberOfElements);
		} else {
			return stateless(result);
		}
	}
	
	protected abstract Double getValue(RecordType record);

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#initialise(java.lang.Object)
	 */
	@Override
	public Pair<Double,Object> initialise(KeyType keyValue) {
		return state(null,0);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.processing.DataAggregator#merge(de.uni_mannheim.informatik.dws.winter.model.Pair, de.uni_mannheim.informatik.dws.winter.model.Pair)
	 */
	@Override
	public Pair<Double,Object> merge(Pair<Double, Object> intermediateResult1, Pair<Double, Object> intermediateResult2) {
		if(fixedNumberOfElements) {
//			return new Pair<Double,Object>(intermediateResult1.getFirst()+intermediateResult2.getFirst(), (Integer)numberOfElements);
			return stateless(intermediateResult1.getFirst()+intermediateResult2.getFirst());
		} else {
			return state(intermediateResult1.getFirst()+intermediateResult2.getFirst(), (Integer)intermediateResult1.getSecond()+(Integer)intermediateResult2.getSecond());
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.DataAggregator#createFinalValue(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Double createFinalValue(KeyType keyValue, Double result, Object state) {
		if(fixedNumberOfElements) {
			return result / (double)numberOfElements;
		} else {
			Integer numberOfElements = (Integer)state;
			return result / (double)numberOfElements;
		}
	}
	
}
