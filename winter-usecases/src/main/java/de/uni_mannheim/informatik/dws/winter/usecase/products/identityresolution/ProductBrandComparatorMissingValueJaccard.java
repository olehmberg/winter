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
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.products.model.Product;

/**
 * {@link Comparator} for {@link Movie}s based on the
 * {@link Product#getBrand()} values, and their null value similarity.
 * If either of the {@link Product#getBrand()} ()} values is null, the similarity is 0.
 *
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 * 
 */
public class ProductBrandComparatorMissingValueJaccard implements Comparator<Product, Attribute> {

	private static final long serialVersionUID = 1L;

	private ComparatorLogger comparisonLog;
	private TokenizingJaccardSimilarity sim = new TokenizingJaccardSimilarity();

	@Override
	public double compare(
			Product record1,
			Product record2,
			Correspondence<Attribute, Matchable> schemaCorrespondences) {
		
		String s1 = record1.getBrand();
		String s2 = record2.getBrand();

		// calculate similarity
		double similarity = sim.calculate(s1, s2);

		if (this.comparisonLog != null) {
			this.comparisonLog.setComparatorName(getClass().getName());

			this.comparisonLog.setRecord1Value(s1);
			this.comparisonLog.setRecord2Value(s2);

			String logSimilarity = Double.toString(similarity);
			if(hasMissingValue(record1, record2, schemaCorrespondences)){
				logSimilarity = "MissingValue";
			}
			this.comparisonLog.setSimilarity(logSimilarity);
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
	public boolean hasMissingValue(Product record1, Product record2, Correspondence<Attribute, Matchable> schemaCorrespondence) {
		String s1 = record1.getBrand();
		String s2 = record2.getBrand();

		return s1 == null || s2 == null;
	}
}
