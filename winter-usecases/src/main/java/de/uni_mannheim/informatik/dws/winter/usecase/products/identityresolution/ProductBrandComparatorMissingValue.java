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
package de.uni_mannheim.informatik.dws.winter.usecase.products.identityresolution;

import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.MissingValueComparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.products.model.Product;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link Comparator} for {@link Movie}s based on the
 * {@link Product#getBrand()} values, and their null value similarity.
 * If either of the {@link Product#getBrand()} ()} values is null, the similarity is 0.
 *
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 * 
 */
public class ProductBrandComparatorMissingValue implements MissingValueComparator<Product, Attribute> {

	private static final long serialVersionUID = 1L;

	private ComparatorLogger comparisonLog;
	private List<Comparator<Product, Attribute>> penalisedComparators;
	private double penalty = 0.0;

	public ProductBrandComparatorMissingValue(double penalty){
		this.penalisedComparators = new LinkedList<>();
		this.penalty = penalty;
	}

	@Override
	public double compare(
			Product record1,
			Product record2,
			Correspondence<Attribute, Matchable> schemaCorrespondences) {
		
		String s1 = record1.getBrand();
		String s2 = record2.getBrand();

		// calculate similarity
		if (this.comparisonLog != null) {
			this.comparisonLog.setComparatorName(getClass().getName());

			this.comparisonLog.setRecord1Value(s1);
			this.comparisonLog.setRecord2Value(s2);
		}
		double similarity = 0.0;

		if (s1 == null || s2 == null) {
			similarity = 1.0;
		}

		if (this.comparisonLog != null) {
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
	public List<Comparator<Product, Attribute>> getPenalisedComparators() {
		return this.penalisedComparators;
	}

	@Override
	public void addPenalisedComparator(Comparator<Product, Attribute> comparator) {
		this.penalisedComparators.add(comparator);
	}

	@Override
	public Double getPenalty() {
		return this.penalty;
	}
}
