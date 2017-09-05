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

package de.uni_mannheim.informatik.dws.winter.similarity.date;



import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;


/**
 * 
 * Calculates the difference of two dates in days and normalises the value with the provided value range.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class NormalisedDateSimilarity extends SimilarityMeasure<LocalDateTime> {

	private static final long serialVersionUID = 1L;
	private LocalDateTime minDate = null;
	
    public void setMinDate(LocalDateTime minDate) {
        this.minDate = minDate;
    }
    public LocalDateTime getMinDate() {
        return minDate;
    }
    
    private LocalDateTime maxDate = null;
    
    public void setMaxDate(LocalDateTime maxDate) {
        this.maxDate = maxDate;
    }
    public LocalDateTime getMaxDate() {
        return maxDate;
    }
    
    private int dateRange = 0;
    public int getDateRange() {
        return dateRange;
    }
    
    public void setValueRange(LocalDateTime minValue, LocalDateTime maxValue) {
        setMinDate(minValue);
        setMaxDate(maxValue);
        calcDateRange();
    }
    
    private void calcDateRange() {
        if(minDate!=null && maxDate!=null) {
            dateRange = Math.abs(getMaxDate().get(ChronoField.EPOCH_DAY) -  getMinDate().get(ChronoField.EPOCH_DAY));
        }
    }
    
    @Override
    public double calculate(LocalDateTime first, LocalDateTime second) {
        int days = Math.abs(first.get(ChronoField.EPOCH_DAY) - second.get(ChronoField.EPOCH_DAY));
        
        return Math.max(1.0 - ((double)days / (double)getDateRange()),0.0);
    }


}
