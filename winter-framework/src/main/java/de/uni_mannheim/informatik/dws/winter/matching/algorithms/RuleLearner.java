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
package de.uni_mannheim.informatik.dws.winter.matching.algorithms;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.lang3.time.DurationFormatUtils;

import de.uni_mannheim.informatik.dws.winter.matching.rules.MatchingRule;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LearnableMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.FeatureVectorDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.ProgressReporter;

/**
 * Class that controls the learning of matching rules
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RuleLearner<RecordType extends Matchable, SchemaElementType extends Matchable> {

	
	public Performance learnMatchingRule(
			DataSet<RecordType, SchemaElementType> data1, 
			DataSet<RecordType, SchemaElementType> data2,
			Processable<? extends Correspondence<SchemaElementType, ?>> schemaCorrespondences,
			LearnableMatchingRule<RecordType, SchemaElementType> rule, 
			MatchingGoldStandard trainingData) {
		
		FeatureVectorDataSet features = generateTrainingDataForLearning(data1, data2, trainingData, rule, schemaCorrespondences);
		return rule.learnParameters(features);
	}
	
	
	/**
	 * Generates a data set containing features that can be used to learn
	 * matching rules.
	 * 
	 * @param dataset1
	 *            The first data set
	 * @param dataset2
	 *            The second data set
	 * @param goldStandard
	 *            The gold standard containing the labels for the generated data
	 *            set
	 * @param rule
	 * 			  The {@link MatchingRule} that is used to generate the features.
	 * @param schemaCorrespondences
	 * 			  The schema correspondences that are passed to the matching rule.
	 * @return A data set containing a feature vector for every example in the gold standard
	 */
	public FeatureVectorDataSet generateTrainingDataForLearning(
			DataSet<RecordType, SchemaElementType> dataset1, 
			DataSet<RecordType, SchemaElementType> dataset2,
			MatchingGoldStandard goldStandard,
			LearnableMatchingRule<RecordType, SchemaElementType> rule,
			Processable<? extends Correspondence<SchemaElementType, ?>> schemaCorrespondences) {
		LocalDateTime start = LocalDateTime.now();

		FeatureVectorDataSet result = rule.initialiseFeatures();
		
		goldStandard.printBalanceReport();

		System.out.println(String.format("[%s] Starting GenerateFeatures",
				start.toString()));

		ProgressReporter progress = new ProgressReporter(goldStandard
				.getPositiveExamples().size()
				+ goldStandard.getNegativeExamples().size(), "GenerateFeatures");

		// create positive examples
		for (Pair<String, String> correspondence : goldStandard
				.getPositiveExamples()) {
			RecordType record1 = dataset1.getRecord(correspondence.getFirst());
			RecordType record2 = dataset2.getRecord(correspondence.getSecond());

			// we don't know which id is from which data set
			if (record1 == null && record2 == null) {
				// so if we didn't find anything, we probably had it wrong ...
				record1 = dataset2.getRecord(correspondence.getFirst());
				record2 = dataset1.getRecord(correspondence.getSecond());
			}

			Record features = rule.generateFeatures(record1, record2, Correspondence.toMatchable(schemaCorrespondences), result);
			features.setValue(FeatureVectorDataSet.ATTRIBUTE_LABEL, "1");
			result.add(features);

			// increment and report status
			progress.incrementProgress();
			progress.report();
		}

		// create negative examples
		for (Pair<String, String> correspondence : goldStandard
				.getNegativeExamples()) {
			RecordType record1 = dataset1.getRecord(correspondence.getFirst());
			RecordType record2 = dataset2.getRecord(correspondence.getSecond());

			// we don't know which id is from which data set
			if (record1 == null && record2 == null) {
				// so if we didn't find anything, we probably had it wrong ...
				record1 = dataset2.getRecord(correspondence.getFirst());
				record2 = dataset1.getRecord(correspondence.getSecond());
			}

			Record features = rule.generateFeatures(record1, record2, Correspondence.toMatchable(schemaCorrespondences), result);
			features.setValue(FeatureVectorDataSet.ATTRIBUTE_LABEL, "0");
			result.add(features);

			// increment and report status
			progress.incrementProgress();
			progress.report();
		}

		// report total time
		LocalDateTime end = LocalDateTime.now();
		
		System.out
				.println(String
						.format("[%s] GenerateFeatures finished after %s; created %,d examples.",
								end.toString(),
								DurationFormatUtils.formatDurationHMS(Duration.between(start, end).toMillis()),
								result.size()));
		
		return result;
	}

}
