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

package de.uni_mannheim.informatik.dws.winter.similarity.numeric;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;


/**
 * 
 * Calculates the absolute difference between two numbers and normalises it with the provided value range.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class NormalisedNumericSimilarity extends SimilarityMeasure<Double> {

	private static final long serialVersionUID = 1L;
	private Double min;
    public Double getMin() {
        return min;
    }
    public void setMin(Double min) {
        this.min = min;
    }
    
    private Double max;
    public Double getMax() {
        return max;
    }
    public void setMax(Double max) {
        this.max = max;
    }
    
    private Double range;
    public Double getRange() {
        return range;
    }
    public void setRange(Double range) {
        this.range = range;
    }
    
    public void setValueRange(Double minValue, Double maxValue) {
        setMax(maxValue);
        setMin(minValue);
        
        if(getMin()!=null && getMax()!=null) {
            setRange(getMax() - getMin());
        }
    }
    
    @Override
    public double calculate(Double first, Double second) {
        
        Double diff = Math.abs(first-second);
        
        if(diff>range) {
            return 0.0;
        } else {
            return diff / range;
        }
    }

}
