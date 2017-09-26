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
package de.uni_mannheim.informatik.dws.winter.webtables;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class TableColumnStatistics{

	private double standardDeviation;
    private double distinctValues;
    private double average;
    private double kurtosis;
    private double skewness;
    private double variance;
  
    /**
     * @return the standardDeviation
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * @param standardDeviation the standardDeviation to set
     */
    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    /**
     * @return the distinctValues
     */
    public double getDistinctValues() {
        return distinctValues;
    }

    /**
     * @param distinctValues the distinctValues to set
     */
    public void setDistinctValues(double distinctValues) {
        this.distinctValues = distinctValues;
    }

    /**
     * @return the average
     */
    public double getAverage() {
        return average;
    }

    /**
     * @param average the average to set
     */
    public void setAverage(double average) {
        this.average = average;
    }

    /**
     * @return the kurtosis
     */
    public double getKurtosis() {
        return kurtosis;
    }

    /**
     * @param kurtosis the kurtosis to set
     */
    public void setKurtosis(double kurtosis) {
        this.kurtosis = kurtosis;
    }

    /**
     * @return the skewness
     */
    public double getSkewness() {
        return skewness;
    }

    /**
     * @param skewness the skewness to set
     */
    public void setSkewness(double skewness) {
        this.skewness = skewness;
    }

    /**
     * @return the variance
     */
    public double getVariance() {
        return variance;
    }

    /**
     * @param variance the variance to set
     */
    public void setVariance(double variance) {
        this.variance = variance;
    }
    
    public TableColumnStatistics() {
	}
	
	public TableColumnStatistics(TableColumn c) {
		calculate(c);
	}

	private void calculate(TableColumn c) {
		
		SynchronizedDescriptiveStatistics statistics = new SynchronizedDescriptiveStatistics();
        statistics.setWindowSize(-1);
        for (TableRow r : c.getTable().getRows()) {
        	if(r.get(c.getColumnIndex()) != null)
        		statistics.addValue((double) r.get(c.getColumnIndex()));
        }
	
        Set<Double> setofDistinctValues = new HashSet<>();
        for (double value : statistics.getValues()) {
        	setofDistinctValues.add(value);
        }
        
        this.setVariance(statistics.getVariance());
        this.setSkewness(statistics.getSkewness());
        this.setKurtosis(statistics.getKurtosis());
        this.setDistinctValues((double) setofDistinctValues.size());
        this.setStandardDeviation(statistics.getStandardDeviation());
        this.setAverage(statistics.getMean());
		
	}
	
}
