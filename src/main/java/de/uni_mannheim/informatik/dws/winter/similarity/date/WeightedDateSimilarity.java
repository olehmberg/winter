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

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * 
 * Calculates the date difference with adjustable weights for years, months and days.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class WeightedDateSimilarity extends SimilarityMeasure<LocalDateTime> {

	private static final long serialVersionUID = 1L;
	private double dayWeight;
    public double getDayWeight() {
        return dayWeight;
    }
    public void setDayWeight(double dayWeight) {
        this.dayWeight = dayWeight;
    }
    
    private double monthWeight;
    public double getMonthWeight() {
        return monthWeight;
    }
    public void setMonthWeight(double monthWeight) {
        this.monthWeight = monthWeight;
    }
    
    private double yearWeight;
    public double getYearWeight() {
        return yearWeight;
    }
    public void setYearWeight(double yearWeight) {
        this.yearWeight = yearWeight;
    }
    
    private int yearRange = 0;
    public int getYearRange() {
		return yearRange;
	}
    public void setYearRange(int yearRange) {
		this.yearRange = yearRange;
	}
    
    public WeightedDateSimilarity() {
        
    }
    
    public WeightedDateSimilarity(double dayWeight, double monthWeight, double yearWeight) {
        this.dayWeight = dayWeight;
        this.monthWeight = monthWeight;
        this.yearWeight = yearWeight;
    }
    
	@Override
	public double calculate(LocalDateTime first, LocalDateTime second) {
		if(first==null || second==null) {
			return 0.0;
		}
		
        if(first.getDayOfYear() == 1 || second.getDayOfYear() == 1) {
            double yearSim = 1.0 - ((double)Math.abs(first.getYear() - second.getYear()) / (double)Math.abs(yearRange));
            return Math.max(yearSim, 0.0);
        }
        
        int days = Math.abs(first.getDayOfMonth() - second.getDayOfMonth());
        int months = Math.abs(first.getMonthValue() - second.getMonthValue());
        
        double daySim = (31.0 - days) / 31.0;
        double monthSim = (12.0 - months) / 12.0;
        double yearSim = 1.0 - ((double)Math.abs(first.getYear() - second.getYear()) / (double)Math.abs(yearRange));
        
        if(yearSim<0.0) {
        	// outside of year range
        	return 0.0;
        } else {
	        daySim = getDayWeight()*daySim;
	        monthSim = getMonthWeight()*monthSim;
	        yearSim = getYearWeight()*yearSim;
	        double value = daySim + monthSim + yearSim;
	        value = value/(getDayWeight()+getMonthWeight()+getYearWeight());
	        return value;
        }
	}

}
