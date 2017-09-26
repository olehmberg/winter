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

package de.uni_mannheim.informatik.dws.winter.similarity.string;

import java.util.LinkedList;
import java.util.List;

import com.wcohen.ss.api.Token;
import com.wcohen.ss.tokens.SimpleTokenizer;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;
import de.uni_mannheim.informatik.dws.winter.similarity.list.GeneralisedJaccard;

/**
 * 
 * Generalised Jaccard for strings.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class GeneralisedStringJaccard extends SimilarityMeasure<String>  {

	private static final long serialVersionUID = 1L;
	private SimilarityMeasure<String> innerFunction;
    public SimilarityMeasure<String> getInnerFunction() {
        return innerFunction;
    }
    public void setInnerFunction(SimilarityMeasure<String> innerFunction) {
        this.innerFunction = innerFunction;
    }
    
    private double innerThreshold;
    public double getInnerThreshold() {
        return innerThreshold;
    }
    public void setInnerThreshold(double innerThreshold) {
        this.innerThreshold = innerThreshold;
    }
    
    private double JaccardThreshold;
    public double getJaccardThreshold() {
        return JaccardThreshold;
    }
    public void setJaccardThreshold(double jaccardThreshold) {
        JaccardThreshold = jaccardThreshold;
    }
    
    public GeneralisedStringJaccard(SimilarityMeasure<String> innerSimilarityFunction, double innerSimilarityThreshold, double jaccardThreshold) {
        setInnerFunction(innerSimilarityFunction);
        setInnerThreshold(innerSimilarityThreshold);
        setJaccardThreshold(jaccardThreshold);
    }
    
    @Override
    public double calculate(String first, String second) {
        
        // split strings into tokens
        SimpleTokenizer tok = new SimpleTokenizer(true, true);
        
        List<String> f = new LinkedList<>();
        List<String> s = new LinkedList<>();
        
        if(first!=null) {
            for(Token t : tok.tokenize(first)) {
                f.add(t.getValue());
            }
        }
        
        if(second!=null) {
            for(Token t : tok.tokenize(second)) {
                s.add(t.getValue());
            }
        }
        
        // run Set-based similarity function
        GeneralisedJaccard<String> j = new GeneralisedJaccard<>();        
        j.setInnerSimilarityThreshold(getInnerThreshold());
        j.setInnerSimilarity(getInnerFunction());
        double sim = j.calculate(f, s);
        
        return sim >= getJaccardThreshold() ? sim : 0.0;
    }

}
