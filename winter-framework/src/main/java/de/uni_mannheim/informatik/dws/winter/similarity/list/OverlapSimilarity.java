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
package de.uni_mannheim.informatik.dws.winter.similarity.list;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * {@link SimilarityMeasure} which calculates the overlap similarity between two
 * lists of strings.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class OverlapSimilarity extends SimilarityMeasure<List<String>> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public double calculate(List<String> first, List<String> second) {

		Set<String> firstSet = new HashSet<>(first);
		Set<String> secondSet = new HashSet<>(second);

		int min = Math.min(firstSet.size(), secondSet.size());
		int matches = 0;

		if(min==0) {
			return 1.0;
		} else {
			for (String s1 : firstSet) {
				for (String s2 : secondSet) {
					if (s1.equals(s2)) {
						matches++;
						continue;
					}
				}
			}

			return (double) matches / (double) min;
		}
	}

}
