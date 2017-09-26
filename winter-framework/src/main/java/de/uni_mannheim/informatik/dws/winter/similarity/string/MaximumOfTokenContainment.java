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

import java.util.HashSet;
import java.util.Set;

import com.wcohen.ss.api.Token;
import com.wcohen.ss.tokens.SimpleTokenizer;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;
import de.uni_mannheim.informatik.dws.winter.similarity.list.MaximumOfContainment;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class MaximumOfTokenContainment extends SimilarityMeasure<String> {

	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure#calculate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double calculate(String first, String second) {

		// tokenise strings
		SimpleTokenizer tok = new SimpleTokenizer(true, true);
		
		Set<String> s1 = new HashSet<>();
		Set<String> s2 = new HashSet<>();
		
		for(Token t : tok.tokenize(first)) {
			s1.add(t.getValue());
		}
		for(Token t : tok.tokenize(second)) {
			s2.add(t.getValue());
		}
		
		// calculate score
		MaximumOfContainment<String> sim = new MaximumOfContainment<>();
		
		return sim.calculate(s1, s2);
	}

}
