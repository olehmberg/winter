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

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.similarity.string.generator.TokenGenerator;
import de.uni_mannheim.informatik.dws.winter.usecase.products.model.Product;

/**
 * {@link TokenGenerator} for {@link Product}s, which generates  blocking
 * key values based on the description split by white space.
 * 
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 * 
 */
public class ProductTokenGeneratorByTitle extends
		TokenGenerator<Product, Attribute> {

	private static final long serialVersionUID = 1L;


	@Override
	public void generateTokens(Product record, Processable<Correspondence<Attribute, Matchable>> correspondences, DataIterator<Pair<String, Product>> resultCollector) {
		String[] descriptionTokens = tokenizeString(record.getTitle());

		for (String token : descriptionTokens) {
			resultCollector.next(new Pair<>(token, record));
		}
	}

	@Override
	public String[] tokenizeString(String value) {
		return value.split(" ");
	}

}
