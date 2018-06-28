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
package de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution;



import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.similarity.EqualsSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;

/**
 * {@link Comparator} for {@link Movie}s based on the {@link Movie#getTitle()}
 * value and their {@link LevenshteinSimilarity} value.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class MovieTitleComparatorEqual implements Comparator<Movie, Attribute> {

	private static final long serialVersionUID = 1L;
	private EqualsSimilarity<String> sim = new EqualsSimilarity<String>();
	
	private HashMap<ComparatorDetails, String> comparisonResult = new HashMap<ComparatorDetails, String>();


	@Override
	public double compare(
			Movie record1,
			Movie record2,
			Correspondence<Attribute, Matchable> schemaCorrespondences) {
		
		this.comparisonResult.put(ComparatorDetails.comparatorName, MovieTitleComparatorEqual.class.getName());
    	
    	this.comparisonResult.put(ComparatorDetails.record1Value, record1.getTitle());
    	this.comparisonResult.put(ComparatorDetails.record2Value, record2.getTitle());
    	
    	double similarity = sim.calculate(record1.getTitle(), record2.getTitle());
    	
    	this.comparisonResult.put(ComparatorDetails.similarity, Double.toString(similarity));
		return similarity;
	}


	@Override
	public Map<ComparatorDetails, String> getComparisonResult() {
		return this.comparisonResult;
	}

}
