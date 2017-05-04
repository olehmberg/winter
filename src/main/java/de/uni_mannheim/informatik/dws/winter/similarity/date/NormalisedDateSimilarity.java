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

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;


/**
 * 
 * Calculates the difference of two dates in days and normalises the value with the provided value range.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class NormalisedDateSimilarity extends SimilarityMeasure<DateTime> {

	private static final long serialVersionUID = 1L;
	private Date minDate = null;
    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }
    public Date getMinDate() {
        return minDate;
    }
    
    private Date maxDate = null;
    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }
    public Date getMaxDate() {
        return maxDate;
    }
    
    private int dateRange = 0;
    public int getDateRange() {
        return dateRange;
    }
    
    public void setValueRange(DateTime minValue, DateTime maxValue) {
        setMinDate(minValue.toDate());
        setMaxDate(maxValue.toDate());
        calcDateRange();
    }
    
    private void calcDateRange() {
        if(minDate!=null && maxDate!=null) {
            dateRange = Math.abs(Days.daysBetween(new DateTime(getMaxDate()), new DateTime(getMinDate())).getDays());
        }
    }
    
    @Override
    public double calculate(DateTime first, DateTime second) {
        int days = Math.abs(Days.daysBetween(first, second).getDays());
        
        return Math.max(1.0 - ((double)days / (double)getDateRange()),0.0);
    }


}
