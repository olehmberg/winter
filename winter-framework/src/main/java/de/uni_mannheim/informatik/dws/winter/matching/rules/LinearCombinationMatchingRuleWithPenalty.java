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
package de.uni_mannheim.informatik.dws.winter.matching.rules;

import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.*;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.FeatureVectorDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.RecordCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link MatchingRule} that is defined by a weighted linear combination of
 * attribute similarities.
 * In combination with the hasMissingValue of the {@link Comparator} missing values
 * can be penalised and the weights of the {@link Comparator} are redistributed.
 * 
 * Does not make use of schema correspondences
 * 
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 * 
 * @param <RecordType>
 *            the type of records that are matched with this rule
 * @param <SchemaElementType>
 *            the type of schema elements that are used in the schema of
 *            RecordType
 */
public class LinearCombinationMatchingRuleWithPenalty<RecordType extends Matchable, SchemaElementType extends Matchable>
		extends FilteringMatchingRule<RecordType, SchemaElementType>
		implements LearnableMatchingRule<RecordType, SchemaElementType> {

	private static final long serialVersionUID = 1L;
	private List<Triple<Comparator<RecordType, SchemaElementType>, Double, Double>> comparators;
	private double offset;

	/**
	 * Initializes the rule. The finalThreshold determines the matching
	 * decision.
	 *
	 * @param finalThreshold
	 *            the similarity threshold of this rule
	 */
	public LinearCombinationMatchingRuleWithPenalty(double finalThreshold) {
		super(finalThreshold);
		this.comparators = new LinkedList<>();
	}

	/**
	 * Initializes the rule. The offset is added to the weighted sum of
	 * similarities, the finalThreshold determines the matching decision.
	 *
	 * @param offset
	 *            the offset
	 * @param finalThreshold
	 *            the similarity threshold of this rule
	 */
	public LinearCombinationMatchingRuleWithPenalty(double offset, double finalThreshold) {
		this(finalThreshold);
		this.offset = offset;
		this.comparators = new LinkedList<>();
	}

	/**
	 * Adds a comparator with the specified weight to this rule.
	 * 
	 * @param comparator
	 *            the comparator
	 * @param weight
	 *            the weight (a double value larger than 0)
	 * @param penalty
	 * 			  the penalty of the comparator that is applied if one of the compared values is missing.
	 * @throws Exception
	 *             Throws an exception if the weight is below or equal to 0.0
	 */
	public void addComparator(Comparator<RecordType, SchemaElementType> comparator, double weight, double penalty) throws Exception {
		if (weight > 0.0) {
			comparators.add(new Triple<>(comparator, weight, penalty));

			if (this.isDebugReportActive()) {
				comparator.setComparisonLog(new ComparatorLogger());
				addComparatorToLog(comparator);
			}

		} else {
			throw new Exception("Weight has to be greater than 0");
		}
	}

	/**
	 * Normalize the weights of the different comparators so they sum up to 1.
	 */
	public void normalizeWeights() {
		Double sum = 0.0;
		for (Triple<Comparator<RecordType, SchemaElementType>, Double, Double> triple : comparators) {
			sum += triple.getSecond();
		}
		List<Triple<Comparator<RecordType, SchemaElementType>, Double, Double>> normComparators = new LinkedList<>();
		for (Triple<Comparator<RecordType, SchemaElementType>, Double, Double> triple : comparators) {
			normComparators.add(new Triple<>(triple.getFirst(),
					(triple.getSecond() / sum),
					triple.getThird()));
		}
		comparators = normComparators;
	}

	@Override
	public Correspondence<RecordType, SchemaElementType> apply(RecordType record1, RecordType record2,
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {

		// double similarity = compare(record1, record2, null);
		double sum = 0.0;
		Record debug = null;
		if (this.isDebugReportActive() && this.continueCollectDebugResults()) {
			debug = initializeDebugRecord(record1, record2, -1);
		}

		// Check for Comparators that don't need to be considered due to missing values
		double penaltyScore = 0.0;
		double redistributedWeights = 0.0;
		double newTotalWeights = 0.0;

		for (Triple<Comparator<RecordType, SchemaElementType>, Double, Double> triple : this.comparators) {
			Comparator<RecordType, SchemaElementType> comp = triple.getFirst();
			double weight = triple.getSecond();
			double penalty = triple.getThird();

			Correspondence<SchemaElementType, Matchable> correspondence = getCorrespondenceForComparator(
					schemaCorrespondences, record1, record2, comp);

			if (comp.hasMissingValue(record1, record2, correspondence)) {
				redistributedWeights = redistributedWeights + weight;
				penaltyScore = penaltyScore + penalty;
			}
			else{
				newTotalWeights = newTotalWeights + weight;
			}
		}


		for (int i = 0; i < this.comparators.size(); i++) {
			Triple<Comparator<RecordType, SchemaElementType>, Double, Double> triple = this.comparators.get(i);

			Comparator<RecordType, SchemaElementType> comp = triple.getFirst();


			Correspondence<SchemaElementType, Matchable> correspondence = getCorrespondenceForComparator(
					schemaCorrespondences, record1, record2, comp);
			
			if (this.isDebugReportActive()) {
				comp.getComparisonLog().initialise();
			}
			double similarity = comp.compare(record1, record2, correspondence);
			double weight = triple.getSecond();

			if(redistributedWeights > 0){
				weight = weight + (weight/newTotalWeights) * redistributedWeights;
			}

			if (!comp.hasMissingValue(record1, record2, correspondence)) {
				sum += (similarity * weight);
			}

			if (this.isDebugReportActive() && this.continueCollectDebugResults()) {
				debug = fillDebugRecord(debug, comp, i);
				addDebugRecordShort(record1, record2, comp, i);
			}
		}

		// do not normalise the sum of weights
		// if a normalised score in the range [0,1] is desired, users should
		// call normaliseWeights()
		double similarity = this.offset + sum - penaltyScore;
		if (this.isDebugReportActive() && this.continueCollectDebugResults()) {
			fillSimilarity(debug, similarity);
		}

		// if (similarity >= getFinalThreshold() && similarity > 0.0) {
		return new Correspondence<>(record1, record2, similarity, schemaCorrespondences);
		// } else {
		// return null;
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_mannheim.informatik.wdi.matching.Comparator#compare(de.
	 * uni_mannheim.informatik.wdi.model.Matchable,
	 * de.uni_mannheim.informatik.wdi.model.Matchable,
	 * de.uni_mannheim.informatik.wdi.model.SimpleCorrespondence)
	 */
	@Override
	public double compare(RecordType record1, RecordType record2,
			Correspondence<SchemaElementType, Matchable> schemaCorrespondence) {
		return 0.0;
	}

	@Override
	public Record generateFeatures(RecordType record1, RecordType record2,
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences,
			FeatureVectorDataSet features) {
		Record model = new Record(String.format("%s-%s", record1.getIdentifier(), record2.getIdentifier()),
				this.getClass().getSimpleName());

		double sum = 0.0;
		Record debug = null;
		if (this.isDebugReportActive() && this.continueCollectDebugResults()) {
			debug = initializeDebugRecord(record1, record2, -1);
		}

		for (int i = 0; i < comparators.size(); i++) {
			Triple<Comparator<RecordType, SchemaElementType>, Double, Double> triple = comparators.get(i);

			Comparator<RecordType, SchemaElementType> comp = triple.getFirst();

			if (this.isDebugReportActive()) {
				comp.getComparisonLog().initialise();
			}

			double similarity = comp.compare(record1, record2, null);

			String name = String.format("[%d] %s", i, comp.getClass().getSimpleName());
			Attribute att = null;
			for (Attribute elem : features.getSchema().get()) {
				if (elem.toString().equals(name)) {
					att = elem;
				}
			}
			if (att == null) {
				att = new Attribute(name);
			}
			model.setValue(att, Double.toString(similarity));

			if (this.isDebugReportActive() && this.continueCollectDebugResults()) {
				debug = fillDebugRecord(debug, comp, i);
				addDebugRecordShort(record1, record2, comp, i);
			}
		}

		// do not normalise the sum of weights
		// if a normalised score in the range [0,1] is desired, users should
		// call normaliseWeights()
		double similarity = offset + sum;
		if (this.isDebugReportActive() && this.continueCollectDebugResults()) {
			fillSimilarity(debug, similarity);
		}

		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.
	 * TrainableMatchingRule#learnParameters(de.uni_mannheim.informatik.dws.
	 * winter.model.defaultmodel.FeatureVectorDataSet)
	 */
	@Override
	public Performance learnParameters(FeatureVectorDataSet features) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.
	 * TrainableMatchingRule#storeModel(java.io.File)
	 */
	@Override
	public void exportModel(File location) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.
	 * TrainableMatchingRule#readModel(java.io.File)
	 */
	@Override
	public void readModel(File location) {

	}

	@Override
	public FeatureVectorDataSet initialiseFeatures(RecordType record1, RecordType record2, Processable<? extends Correspondence<SchemaElementType, ? extends Matchable>> schemaCorrespondences) {
		FeatureVectorDataSet features = new FeatureVectorDataSet();
		
		for (int i = 0; i < comparators.size(); i++) {
			Triple<Comparator<RecordType, SchemaElementType>, Double, Double> triple = comparators.get(i);

			Comparator<RecordType, SchemaElementType> comp = triple.getFirst();

			String name = String.format("[%d] %s", i, comp.getClass().getSimpleName());
			Attribute att = new Attribute(name);

			features.addAttribute(att);
		}
		
		return features;
	}

	@Override
	public String toString() {
		return String.format("LinearCombinationMatchingRule: %f + %s", offset,
				StringUtils.join(Q.project(comparators, (c) -> c.getSecond() + " " + c.getFirst().toString()), " + "));
	}

	@Override
	public void exportTrainingData(DataSet<RecordType, SchemaElementType> dataset1,
			DataSet<RecordType, SchemaElementType> dataset2, MatchingGoldStandard goldStandard, File file)
			throws IOException {
		RuleLearner<Record, Attribute> learner = new RuleLearner<>();

		@SuppressWarnings("unchecked")
		FeatureVectorDataSet features = learner.generateTrainingDataForLearning((DataSet<Record, Attribute>) dataset1,
				(DataSet<Record, Attribute>) dataset2, goldStandard, (LearnableMatchingRule<Record, Attribute>) this,
				null);
		new RecordCSVFormatter().writeCSV(file, features, null);

		writeDebugMatchingResultsToFile();

	}
}
