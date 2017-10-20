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

package de.uni_mannheim.informatik.dws.winter.matrices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.utils.query.Func;

/**
 * super class for all similarity matrices
 * 
 * @author Oliver
 * 
 * @param <T>	the type of the matrix' dimensions
 */
public abstract class SimilarityMatrix<T> {

    /**
     * 
     * 
     * @param first	the value for the first dimension
     * @param second	the value for the second dimension
     * @return returns the similarity of the provided objects
     */
    public abstract Double get(T first, T second);

    /**
     * sets the similarity of the provided objects
     * 
     * @param first		the value for the first dimension
     * @param second	the value for the second dimension
     * @param similarity	the similarity value
     */
    public abstract void set(T first, T second, Double similarity);
    
    /**
     *  
     * @return returns the list of all objects in the first dimension (passed as 'first' parameter to the set() method)
     */
    public abstract Collection<T> getFirstDimension();

    /**
     * 
     * @return returns the list of all objects in the second dimension (passed as 'second' parameter to the set() method)
     */
    public abstract Collection<T> getSecondDimension();

    /**
     * 
     * @param first 	the value for the first dimension
     * @return returns all objects in the second dimension that have a similarity &gt; 0
     */
    public abstract Collection<T> getMatches(T first);

    /**
     * 
     * @param first		the value for the first dimension
     * @param similarityThreshold	the minimal similarity value
     * @return returns all objects in the second dimension that have a similarity &gt; similarityThreshold
     */
    public abstract Collection<T> getMatchesAboveThreshold(T first,
            double similarityThreshold);

    /***
     * Adds value to the existing value for first and second, if no value exists yet, value is set
     * @param first		the value for the first dimension
     * @param second	the value for the second dimension
     * @param value		the similarity value
     */
    public void add(T first, T second, Double value) {
    	Double existing = get(first, second);
    	
    	if(existing==null) {
    		existing = 0.0;
    	}
    	
    	set(first, second, existing + value);
    }
    
    /**
     * Normalize all values in the matrix by the factor
     * 
     * v = v / normalizingFactor
     * 
     * @param normalizingFactor	the factor to normalise with
     */
    public void normalize(double normalizingFactor) {
        //System.out.println("norm: " + normalizingFactor);
        for (T value : getFirstDimension()) {
            for (T secondValue : getMatches(value)) {
                double d = get(value, secondValue) / normalizingFactor;
                set(value, secondValue, d);
            }
        }
    }

    /**
     * Multiply the matrix with a scalar value
     * 
     * @param scalar	the value to multiply with
     */
    public void multiplyScalar(double scalar) {
        if(scalar==1.0) {
            return;
        }
        for (T value : getFirstDimension()) {
            for (T secondValue : getMatches(value)) {
                double d = get(value, secondValue) * scalar;
                set(value, secondValue, d);
            }
        }
    }
    
    /**
     * Normalise all values in the matrix in the range between 0 and 1.
     */
    public void normalize() {
        // determine the maximum value
        
        Double max = getMaxValue();
        
        if(max!=null) {
            normalize(max);
        }
    }
    
    /**
     * Changes the values of the matrix such that all values sum to 1, preserving the relative differences of the values
     */
    public void makeStochastic() {
        
        double sum = 0.0;
        
        for(T row : getFirstDimension()) {
            
            // sum all values
            for(T col : getMatches(row)) {
                
                if(get(row, col)!=null) {
                    sum += get(row, col);
                }
                
            }
        }
        
        
        for(T row : getFirstDimension()) {
            
            // sum all values
            for(T col : getMatches(row)) {
                
                if(get(row, col)!=null) {
                    set(row, col, get(row, col)/sum);
                }
                
            }
        }
    }
    
    /**
     * Changes the values of the matrix such that each row sums to 1, preserving the relative differences of the values in each row
     */
    public void makeRowStochastic() {
        
        for(T row : getFirstDimension()) {
            
            double sum = 0.0;
            
            // sum all values of the current row
            for(T col : getMatches(row)) {
                
                sum += get(row, col);
                
            }
            
            // divide each value by the sum
            for(T col : getMatches(row)) {
                
                set(row, col, get(row, col) / sum);
                
            }
        }
        
    }
    
    /**
     * Changes the values of the matrix such that each column sums to 1, preserving the relative differences of the values in each column
     */
    public void makeColumnStochastic() {
        
        for(T col : getSecondDimension()) {
            
            double sum = 0.0;
            
            // sum all values of the current column
            for(T row : getFirstDimension()) {
                
                sum += get(row, col);
                
            }
            
            // divide each value by the sum
            for(T row : getFirstDimension()) {
                
                set(row, col, get(row, col) / sum);
                
            }
        }
        
    }
    
    /**
     * inverts each value of the matrix x by replacing it with 1/x
     */
    public void invert() {
        
        for(T first : getFirstDimension()) {
            
            for(T second : getMatches(first)) {
                
                Double sim = get(first, second);
                
                if(sim!=null && sim > 0.0) {
                    set(first, second, 1.0/sim);
                }
                
            }
            
        }
    }
    
    /**
     * Changes the values of the matrix such that each value is either 1 (if above the threshold) or 0.
     * @param threshold		the threshold for removing values from the matrix
     * @return returns this instance
     */
    public SimilarityMatrix<T> makeBinary(double threshold) {
        
        for(T first : getFirstDimension()) {
            
            for(T second : getMatches(first)) {
                
                if(get(first, second)>threshold) {
                    set(first, second, 1.0);
                } else {
                    set(first, second, 0.0);
                }
                
            }
            
        }
        
        return this;
    }
    
    public Collection<Double> getRowSums() {
        
        ArrayList<Double> sums = new ArrayList<Double>(getFirstDimension().size());
        
        for(T row : getFirstDimension()) {
            
            double sum = 0.0;
            
            // sum all values of the current row
            for(T col : getMatches(row)) {
                
                sum += get(row, col);
                
            }
            
            sums.add(sum);
        }

        return sums;
    }
    
    public Collection<Double> getColSums() {
        
        ArrayList<Double> sums = new ArrayList<Double>(getSecondDimension().size());
        
        for(T col : getSecondDimension()) {
            
            double sum = 0.0;
            
            // sum all values of the current column
            for(T row : getFirstDimension()) {
                
                Double d = get(row, col);
                
                if(d!=null) {
                    sum += get(row, col);
                }
                
            }
            
            sums.add(sum);
        }
        
        return sums;
    }
    
    public Double getSum() {
        
        double sum = 0.0;
        
        for(Double d : getColSums()) {
            
            sum += d;
            
        }
        
        return sum;
    }
    
    /**
     * removes all elements below the given threshold
     * @param belowThreshold	the minimal similarity value
     */
    public void prune(double belowThreshold) {
        
        for(T first : getFirstDimension()) {
            
            for(T second : getMatches(first)) {
                
                if(get(first, second)<belowThreshold) {
                    set(first, second, null);
                }
                
            }
            
        }
        
    }
    
    public int getNumberOfElements() {
        return getFirstDimension().size() * getSecondDimension().size();
    }
    
    public int getNumberOfNonZeroElements() {
        int i = 0;
        for (T value : getFirstDimension()) {
            i += getMatchesAboveThreshold(value, 0.0).size();
        }
        return i;
    }

    private HashMap<T, Object> labels = new HashMap<T, Object>();

    /**
     * 
     * @param instance	the instance for which the label should be returned
     * @return returns the label of an instance that is part of the first or second dimension
     */
    public Object getLabel(T instance) {
        if (labels.containsKey(instance)) {
            return labels.get(instance);
        } else {
            return "";
        }
    }

    public void setLabel(T instance, Object label) {
        labels.put(instance, label);
    }

    protected String padRight(String s, int n) {
        if(n==0) {
            return "";
        }
        if (s.length() > n) {
            s = s.substring(0, n);
        }
        s = s.replace("\n", " ");
        return String.format("%1$-" + n + "s", s);
    }

    protected static String padLeft(String s, int n) {
        if(n==0) {
            return "";
        }
        if (s.length() > n) {
            s = s.substring(0, n);
        }
        s = s.replace("\n", " ");
        return String.format("%1$" + n + "s", s);
    }

    public String getOutput() {
        return getOutput(null, null);
    }
    
    public String getOutput(int colWidth) {
        return getOutput(null, null, colWidth);
    }
    
    public <U> String getOutput(Collection<Object> filterFirstDimension, Collection<Object> filterSecondDimension) {
    	return getOutput(filterFirstDimension, filterSecondDimension, 20);
    }
    
    public <U> String getOutput(Collection<Object> filterFirstDimension, Collection<Object> filterSecondDimension, int colWidth) {
        String output = "";
        output = getFirstDimension().size() + " x "
                + getSecondDimension().size();
        
        Collection<T> firstDim = new ArrayList<T>(getFirstDimension().size());
        
        for(T inst : getFirstDimension()) {
            firstDim.add(inst);
        }
        
        Collection<T> secondDim = new ArrayList<T>(getSecondDimension().size());
        
        for(T inst : getSecondDimension()) {
            secondDim.add(inst);
        }
        
        if(filterFirstDimension!=null) {
            Iterator<T> it = firstDim.iterator();
            while(it.hasNext()) {
                T current = it.next();
                
                if(!filterFirstDimension.contains(current)) {
                    it.remove();
                }
            }
            
            // add more elements till we reach 100
            it = getFirstDimension().iterator();
            while(firstDim.size()<100 && it.hasNext()) {
                T current = it.next();
                
                if(!filterFirstDimension.contains(current)) {
                    firstDim.add(current);
                }
            }
        }
        
        if(filterSecondDimension!=null) {
            Iterator<T> it = secondDim.iterator();
            while(it.hasNext()) {
                T current = it.next();

                if(!filterSecondDimension.contains(current)) {
                    it.remove();
                }
            }
            
            it = getSecondDimension().iterator();
            while(secondDim.size()<100 && it.hasNext()) {
                T current = it.next();

                if(!filterSecondDimension.contains(current)) {
                    secondDim.add(current);
                }
            }
        }
        
        if(firstDim.size()>100) {
            output += "First dimension too large, showing only the first 100 elements.";
            
            Collection<T> tmp = new ArrayList<T>(100);
            Iterator<T> it = firstDim.iterator();
            for(int i=0;i<100;i++) {
                tmp.add(it.next());
            }
            
            firstDim = tmp;
        }
        
        if(secondDim.size()>100) {
            output += "Second dimension too large, showing only the first 100 elements.";
            
            Collection<T> tmp = new ArrayList<T>(100);
            Iterator<T> it = secondDim.iterator();
            for(int i=0;i<100;i++) {
                tmp.add(it.next());
            }
            secondDim = tmp;
        }
        
//        int colWidth = 20;
        LinkedList<Integer> columnWidths = new LinkedList<Integer>();
        
        // measure size of columns
        if (labels.size() > 0) {
            
            int max = 0;
            for (T value : firstDim) {
                if(labels.get(value)!=null) {
                    max = Math.max(labels.get(value).toString().length(), max);
                }
            }
            columnWidths.add(Math.min(max, colWidth));
            
            max = 0;
            for (T value : firstDim) {
                max = Math.max(value.toString().length(), max);
            }
            columnWidths.add(Math.min(max, colWidth));
            
            for (T value : secondDim) {
                int lblLength = 0;
                if(getLabel(value)!=null) {
                    lblLength = getLabel(value).toString().length();
                }
                columnWidths.add(Math.min(Math.max(lblLength, value.toString().length()), colWidth));
            }
        }
        else
        {
            int max = 0;
            for (T value : firstDim) {
                if(value!=null) {
                    max = Math.max(value.toString().length(), max);
                }
            }
            columnWidths.add(Math.min(max, colWidth));
            
            for (T value : secondDim) {
                if(value==null) {
                    columnWidths.add(colWidth);
                } else {
                    columnWidths.add(Math.min(value.toString().length(), colWidth));
                }
            }
        }

        output += "\n";
        
        if (labels.size() > 0) {
            // print second dimension labels
            output += padLeft("", columnWidths.get(0)) + " | ";
            output += padLeft("", columnWidths.get(1)) + " | ";
            int i=2;
            for (T value : secondDim) {
                output += padLeft(getLabel(value).toString(), columnWidths.get(i++)) + " | ";
            }
            output += "\n";
            output += padLeft("", columnWidths.get(0)) + " | ";
            output += padLeft("", columnWidths.get(1)) + " | ";
        }
        else {
            output += padLeft("", columnWidths.get(0)) + " | ";
        }

        // print second dimension values
        int colIdx = labels.size()>0 ? 2 : 1;
        for (T value : secondDim) {
            output += padLeft(value + "", columnWidths.get(colIdx++)) + " | ";
        }
        output += "\n";

        // print first dimension labels & values
        colIdx=0;
        for (T value : firstDim) {
            if (labels.size() > 0) {
                output += padLeft(getLabel(value)+"", columnWidths.get(0)) + " | ";
                colIdx=1;
            }
            output += padLeft(value + "", columnWidths.get(colIdx)) + " | ";

            // print scores
            int colIdx2 = colIdx+1;
            for (T secondValue : secondDim) {
                if (get(value, secondValue) == null) {
                    output += padRight("", columnWidths.get(colIdx2++)) + " | ";
                } else {
                    output += padRight(
                            String.format("%1$,.8f", get(value, secondValue)),
                            columnWidths.get(colIdx2++)) + " | ";
                }
            }
            output += "\n";
        }
        return output;
    }
    
    public String getOutput2(Collection<Object> filterFirstDimension, Collection<Object> filterSecondDimension) {
        String output = "";
        output = getFirstDimension().size() + " x " + getSecondDimension().size();
        
        Collection<T> firstDim = new ArrayList<T>(getFirstDimension().size());
        
        for(T inst : getFirstDimension()) {
            firstDim.add(inst);
        }
        
        Collection<T> secondDim = new ArrayList<T>(getSecondDimension().size());
        
        for(T inst : getSecondDimension()) {
            secondDim.add(inst);
        }
        
        if(filterFirstDimension!=null) {
            Iterator<T> it = firstDim.iterator();
            while(it.hasNext()) {
                T current = it.next();
                
                if(!filterFirstDimension.contains(current)) {
                    it.remove();
                }
            }
            
            // add more elements till we reach 100
            it = getFirstDimension().iterator();
            while(firstDim.size()<100 && it.hasNext()) {
                T current = it.next();
                
                if(!filterFirstDimension.contains(current)) {
                    firstDim.add(current);
                }
            }
        }
        
        if(filterSecondDimension!=null) {
            Iterator<T> it = secondDim.iterator();
            while(it.hasNext()) {
                T current = it.next();
                
                if(!filterSecondDimension.contains(current)) {
                    it.remove();
                }
            }
            
            it = getSecondDimension().iterator();
            while(secondDim.size()<100 && it.hasNext()) {
                T current = it.next();
                
                if(!filterSecondDimension.contains(current)) {
                    secondDim.add(current);
                }
            }
        }
        
        if(firstDim.size()>100) {
            output += "First dimension too large, showing only the first 100 elements.";
            
            Collection<T> tmp = new ArrayList<T>(100);
            Iterator<T> it = firstDim.iterator();
            for(int i=0;i<100;i++) {
                tmp.add(it.next());
            }
            
            firstDim = tmp;
        }
        
        if(secondDim.size()>100) {
            output += "Second dimension too large, showing only the first 100 elements.";
            
            Collection<T> tmp = new ArrayList<T>(100);
            Iterator<T> it = secondDim.iterator();
            for(int i=0;i<100;i++) {
                tmp.add(it.next());
            }
            secondDim = tmp;
        }
        
        int colWidth = 20;
        LinkedList<Integer> columnWidths = new LinkedList<Integer>();
        
        // measure size of columns
        
            
        int max = 0;
        for (T value : firstDim) {
            if(value!=null) {
                max = Math.max(value.toString().length(), max);
            }
        }
        columnWidths.add(Math.min(max, colWidth * 3));
        
        max = 0;
        for (T value : firstDim) {
            max = Math.max(value.toString().length(), max);
        }
        columnWidths.add(Math.min(max, colWidth));
        
        for (T value : secondDim) {
            columnWidths.add(Math.min(value.toString().length(), colWidth));
        }


        
        // print second dimension labels
        output += padLeft("", columnWidths.get(0)) + " | ";
        output += padLeft("", columnWidths.get(1)) + " | ";
        int i=2;
        for (T value : secondDim) {
            output += padLeft(value.toString(), columnWidths.get(i++)) + " | ";
        }
        output += "\n";
        output += padLeft("", columnWidths.get(0)) + " | ";
        output += padLeft("", columnWidths.get(1)) + " | ";
        

        // print second dimension values
        int colIdx = 2;
        for (T value : secondDim) {
            output += padLeft(value.toString(), columnWidths.get(colIdx++)) + " | ";
        }
        output += "\n";

        // print first dimension labels & values
        colIdx=0;
        for (T value : firstDim) {
            
            output += padRight(value.toString(), columnWidths.get(0)) + " | ";
            colIdx=1;

            output += padLeft(value + "", columnWidths.get(colIdx)) + " | ";

            // print scores
            int colIdx2 = colIdx+1;
            for (T secondValue : secondDim) {
                if (get(value, secondValue) == null) {
                    output += padRight("", columnWidths.get(colIdx2++)) + " | ";
                } else {
                    output += padRight(
                            String.format("%1$,.8f", get(value, secondValue)),
                            columnWidths.get(colIdx2++)) + " | ";
                }
            }
            output += "\n";
        }
        return output;
    }
    
    public String listPairs() {
        StringBuilder sb = new StringBuilder();
        
        for(T first : getFirstDimension()) {
            
            for(T second : getMatches(first)) {
                
                sb.append(first);
                sb.append("\t");
                sb.append(second);
                sb.append("\t");
                sb.append(get(first, second));
                sb.append("\n");
                
            }
            
        }
        
        return sb.toString();
    }
    
    public void printStatistics(String label) {
        double percent = (double) getNumberOfNonZeroElements()
                / (double) getNumberOfElements();

        System.out.println(String.format("%s -> %d / %d non-zero (%.2f %%)",
                label, getNumberOfNonZeroElements(), getNumberOfElements(),
                percent * 100));
    }
    
    public SimilarityMatrix<T> copy() {
        SimilarityMatrix<T> m = createEmptyCopy();
        
        for(T first : getFirstDimension()) {
            for(T second : getSecondDimension()) {
                m.set(first, second, get(first, second));
            }
        }
        
        return m;
    }
    
    protected abstract SimilarityMatrix<T> createEmptyCopy();
    
    public Double getMaxValue() {
        Double d = null;
        
        for(T first : getFirstDimension()) {
            for(T second : getMatches(first)) {
                if(d==null) {
                    d = get(first, second);
                } else {
                    d = Math.max(d, get(first, second));
                }
            }
        }
        
        return d;
    }
    
    public List<Triple<T, T, Double>> getPairsSortedDescending() {
        List<Triple<T, T, Double>> triples = new LinkedList<>();
        
        for(T t : getFirstDimension()) {
            
            for(T t2 : getMatches(t)) {
                
                triples.add(new Triple<T, T, Double>(t, t2, get(t, t2)));
                
            }
            
        }
        
        Collections.sort(triples, new Comparator<Triple<T, T, Double>>() {

            @Override
            public int compare(Triple<T, T, Double> o1, Triple<T, T, Double> o2) {
                return -Double.compare(o1.getThird(), o2.getThird());
            }
        });
        
        return triples;
    }
    
//    public <U extends Matchable> Collection<Correspondence<T, U>> toCorrespondences() {
//    	LinkedList<Correspondence<T, U>> result = new LinkedList<>();
//    	
//    	for(T t1 : getFirstDimension()) {
//    		for(T t2 : getMatches(t1)) {
//    			Correspondence<T, U> cor = new Correspondence<T, U>(t1, t2, get(t1, t2), null);
//    			result.add(cor);
//    		}
//    	}
//    	
//    	return result;
//    }
    
    public static <T extends Matchable, U extends Matchable> SimilarityMatrix<T> fromCorrespondences(Collection<Correspondence<T, U>> correspondences, SimilarityMatrixFactory factory) {
    	SimilarityMatrix<T> m = factory.createSimilarityMatrix(0, 0);
    	
    	for(Correspondence<T, U> cor : correspondences) {
    		m.add(cor.getFirstRecord(), cor.getSecondRecord(), cor.getSimilarityScore());
    	}
    	
    	return m;
    }
    
    public HasMatchPredicate<T> getHasMatchPredicate() {
    	return new HasMatchPredicate<>(this);
    }
    
    public static class HasMatchPredicate<T> implements Func<Boolean, T> {

    	private SimilarityMatrix<T> m;
    	
		public HasMatchPredicate(SimilarityMatrix<T> m) {
			this.m = m;
		}
    	
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public Boolean invoke(T in) {
			return m.getMatches(in).size()>0;
		}
    	
    }
}
