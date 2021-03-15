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

import de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TFIDFCosineSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceCosineSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.products.model.Product;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import org.slf4j.Logger;

/**
 * {@link Comparator} for {@link Product}s based on the {@link Product#getDescription()}
 * value and their TF-IDF & Cosine Similarity value.
 * 
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 * 
 */
public class ProductDescriptionComparatorTFIDFCosine implements Comparator<Product, Attribute> {

	private static final long serialVersionUID = 1L;

	private ComparatorLogger comparisonLog;
	private TFIDFCosineSimilarity tfidfCosineSimilarity;


	public ProductDescriptionComparatorTFIDFCosine(HashedDataSet<Product, Attribute> dataProductsLeft,
                                                   HashedDataSet<Product, Attribute> dataProductsRight){

		this.tfidfCosineSimilarity = new TFIDFCosineSimilarity(dataProductsLeft, dataProductsRight,
											new ProductTokenGeneratorByDescription());
	}

	@Override
	public double compare(
			Product record1,
			Product record2,
			Correspondence<Attribute, Matchable> schemaCorrespondences) {

		String s1 = record1.getDescription();
		String s2 = record2.getDescription();

		double similarity = tfidfCosineSimilarity.calculate(s1, s2);
    	
		if(this.comparisonLog != null){
			this.comparisonLog.setComparatorName(getClass().getName());
		
			this.comparisonLog.setRecord1Value(s1);
			this.comparisonLog.setRecord2Value(s2);
    	
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

}
