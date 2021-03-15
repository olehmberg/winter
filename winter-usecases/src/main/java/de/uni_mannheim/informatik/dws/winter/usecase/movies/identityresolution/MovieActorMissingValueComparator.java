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

import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Actor;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MovieActorMissingValueComparator implements Comparator<Movie, Attribute> {

	private static final long serialVersionUID = 1L;
	
	private ComparatorLogger comparisonLog;

	@Override
	public double compare(
			Movie record1,
			Movie record2,
			Correspondence<Attribute, Matchable> schemaCorrespondences) {
		
		Set<String> actors1 = new HashSet<>();
		Set<String> actors2 = new HashSet<>();
		
		for(Actor a : record1.getActors()) {
			actors1.add(a.getName());
		}
		for(Actor a : record2.getActors()) {
			actors2.add(a.getName());
		}
		
		double similarity = Q.intersection(actors1, actors2).size() / (double)Math.max(actors1.size(), actors2.size());
		
		if(this.comparisonLog != null){
			this.comparisonLog.setComparatorName(getClass().getName());
		
			this.comparisonLog.setRecord1Value(actors1.toString());
			this.comparisonLog.setRecord2Value(actors2.toString());
    	
			this.comparisonLog.setSimilarity(Double.toString(similarity));
		}
		
		return similarity;
	}
	
	@Override
	public ComparatorLogger getComparisonLog() {
		return this.comparisonLog;
	}

	@Override
	public void setComparisonLog(ComparatorLogger comparatorLog) {
		this.comparisonLog = comparatorLog;
	}

	@Override
	public boolean hasMissingValue(Movie record1, Movie record2, Correspondence<Attribute, Matchable> schemaCorrespondence) {
		List<Actor> list1 = record1.getActors();
		List<Actor> list2 = record2.getActors();

		if(list1.size() == 0 || list2.size() == 0){
			return true;
		}
		else {
			return false;
		}

		//return s1 == null || s2 == null;
	}
}
