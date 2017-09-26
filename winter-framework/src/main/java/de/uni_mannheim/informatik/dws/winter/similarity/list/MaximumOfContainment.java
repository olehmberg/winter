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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class MaximumOfContainment<DataType> extends SimilarityMeasure<Collection<DataType>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure#calculate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double calculate(Collection<DataType> first, Collection<DataType> second) {

		Set<DataType> firstSet = new HashSet<>(first);
		Set<DataType> secondSet = new HashSet<>(second);
	
		Set<DataType> intersection = Q.intersection(first, second);
		
		return Math.max(intersection.size()/(double)firstSet.size(), intersection.size()/(double)secondSet.size());
	}

}
