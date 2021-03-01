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
package de.uni_mannheim.informatik.dws.winter.usecase.products;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRuleWithPenalty;
import de.uni_mannheim.informatik.dws.winter.model.*;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceCosineSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.products.identityresolution.*;
import de.uni_mannheim.informatik.dws.winter.usecase.products.model.CSVProductReader;
import de.uni_mannheim.informatik.dws.winter.usecase.products.model.Product;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import org.slf4j.Logger;

import java.io.File;

/**
 * Class containing the standard setup to perform a identity resolution task,
 * reading input data from the product use case.
 * 
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 * 
 */
public class Products_IdentityResolution_Main {

	/*
	 * Logging Options:
	 * 		default: 	level INFO	- console
	 * 		trace:		level TRACE     - console
	 * 		infoFile:	level INFO	- console/file
	 * 		traceFile:	level TRACE	- console/file
	 *
	 * To set the log level to trace and write the log to winter.log and console,
	 * activate the "traceFile" logger as follows:
	 *     private static final Logger logger = WinterLogManager.activateLogger("traceFile");
	 *
	 */

	private static final Logger logger = WinterLogManager.activateLogger("default");

	public static void main(String[] args) throws Exception {
		// loading data
		HashedDataSet<Product, Attribute> dataProductsLeft = new HashedDataSet<>();
		new CSVProductReader().loadFromCSV(new File("usecase/product/input/products_left.csv"),  dataProductsLeft);
		HashedDataSet<Product, Attribute> dataProductsRight = new HashedDataSet<>();
		new CSVProductReader().loadFromCSV(new File("usecase/product/input/products_right.csv"),  dataProductsRight);

		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"usecase/product/goldstandard/gs_products_training.csv"));

		// create a matching rule
		LinearCombinationMatchingRuleWithPenalty<Product, Attribute> matchingRule = new LinearCombinationMatchingRuleWithPenalty<>(
				0.5);
		matchingRule.activateDebugReport("usecase/product/output/debugResultsMatchingRule.csv", 1000, gsTest);

		// add comparators
		matchingRule.addComparator(new ProductTitleComparatorJaccard(),0.2, 0.0);
		// Provide the full data sets as input for the TF-IDF comparators to generate a complete inverted index for all included tokens
		matchingRule.addComparator(new ProductTitleComparatorTFIDFCosine(dataProductsLeft, dataProductsRight, null), 0.5, 0.0);
		matchingRule.addComparator(new ProductDescriptionComparatorTFIDFCosine(dataProductsLeft, dataProductsRight, null), 0.1, 0.0);

		// Use missing Value Comparator on Brand to neutralise the ProductBrandComparatorJaccard
		// if the Brand value of either record is missing.
		matchingRule.addComparator(new ProductBrandComparatorMissingValueJaccard(),0.2, 0.1);

		// create a blocker (blocking strategy)
		BlockingKeyIndexer<Product, Attribute, Product, Attribute>  blocker = new BlockingKeyIndexer<>(
				new ProductBlockingKeyByTitleGenerator(),
				new ProductBlockingKeyByTitleGenerator(), new VectorSpaceCosineSimilarity(),
				BlockingKeyIndexer.VectorCreationMethod.TFIDF, 0.2);
		//Write debug results to file:
		blocker.collectBlockSizeData("usecase/product/output/debugResultsBlocking.csv", 100);

		// Initialize Matching Engine
		MatchingEngine<Product, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<Product, Attribute>> correspondences = engine.runIdentityResolution(
				dataProductsLeft, dataProductsRight, null, matchingRule,
				blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/product/output/product_correspondences.csv"), correspondences);


		// evaluate your result
		MatchingEvaluator<Product, Attribute> evaluator = new MatchingEvaluator<>();
		Performance perfTest = evaluator.evaluateMatching(correspondences.get(),
				gsTest);

		// print the evaluation result
		logger.info("Products Left <-> Products Right");
		logger.info(String.format("Precision: %.4f", perfTest.getPrecision()));
		logger.info(String.format("Recall: %.4f", perfTest.getRecall()));
		logger.info(String.format("F1: %.4f", perfTest.getF1()));
	}
}
