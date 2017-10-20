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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.FeatureVectorDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * A {@link MatchingRule} that is defined by a weighted linear
 * combination of attribute similarities.
 * 
 * Does not make use of schema correspondences
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type of records that are matched with this rule
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType
 */
public class LinearCombinationMatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable>
		extends FilteringMatchingRule<RecordType, SchemaElementType>
		implements LearnableMatchingRule<RecordType, SchemaElementType>
{

	private static final long serialVersionUID = 1L;
	private List<Pair<Comparator<RecordType, SchemaElementType>, Double>> comparators;
	private double offset;

	/**
	 * Initialises the rule. The finalThreshold determines the matching
	 * decision.
	 * 
	 * @param finalThreshold	the similarity threshold of this rule
	 */
	public LinearCombinationMatchingRule(double finalThreshold) {
		super(finalThreshold);
		comparators = new LinkedList<>();
	}

	/**
	 * Initialises the rule. The offset is added to the weighted sum of
	 * similarities, the finalThreshold determines the matching decision.
	 * 
	 * @param offset	the offset 
	 * @param finalThreshold	the similarity threshold of this rule
	 */
	public LinearCombinationMatchingRule(double offset, double finalThreshold) {
		this(finalThreshold);
		this.offset = offset;
	}

	/**
	 * Adds a comparator with the specified weight to this rule.
	 * 
	 * @param comparator	the comparator
	 * @param weight	the weight (a double value larger than 0)
	 * @throws Exception	Throws an exception if the weight is equal to or below 0.0
	 */
	public void addComparator(Comparator<RecordType, SchemaElementType> comparator, double weight)
			throws Exception {
		if (weight > 0.0) {
			comparators.add(new Pair<Comparator<RecordType, SchemaElementType>, Double>(
					comparator, weight));
		} else {
			throw new Exception("Weight cannot be 0.0 or smaller");
		}
	}

	/**
	 * Normalize the weights of the different comparators so they sum up to 1.
	 */
	public void normalizeWeights() {
		Double sum = 0.0;
		for (Pair<Comparator<RecordType, SchemaElementType>, Double> pair : comparators) {
			sum += pair.getSecond();
		}
		List<Pair<Comparator<RecordType, SchemaElementType>, Double>> normComparators = new LinkedList<>();
		for (Pair<Comparator<RecordType, SchemaElementType>, Double> pair : comparators) {
			normComparators.add(new Pair<Comparator<RecordType, SchemaElementType>, Double>(pair
					.getFirst(), (pair.getSecond() / sum)));
		}
		comparators = normComparators;
	}

	@Override
	public Correspondence<RecordType, SchemaElementType> apply(RecordType record1, RecordType record2, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {

		double similarity = compare(record1, record2, null);

//		if (similarity >= getFinalThreshold() && similarity > 0.0) {
			return new Correspondence<RecordType, SchemaElementType>(record1, record2, similarity, schemaCorrespondences);
//		} else {
//			return null;
//		}
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.Comparator#compare(de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.SimpleCorrespondence)
	 */
	@Override
	public double compare(RecordType record1, RecordType record2,
			Correspondence<SchemaElementType, Matchable> schemaCorrespondence) {
		double sum = 0.0;
		double wSum = 0.0;
		for (int i = 0; i < comparators.size(); i++) {
			Pair<Comparator<RecordType, SchemaElementType>, Double> pair = comparators.get(i);

			Comparator<RecordType, SchemaElementType> comp = pair.getFirst();
 
			double similarity = comp.compare(record1, record2, null);
			double weight = pair.getSecond();
			wSum += weight;
			sum += (similarity * weight);
		}

		return offset + (sum / wSum);
	}
	
	@Override
	public Record generateFeatures(RecordType record1, RecordType record2, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, FeatureVectorDataSet features) {
		Record model = new Record(String.format("%s-%s",
				record1.getIdentifier(), record2.getIdentifier()), this
				.getClass().getSimpleName());

		double sum = offset;

		for (int i = 0; i < comparators.size(); i++) {
			Pair<Comparator<RecordType, SchemaElementType>, Double> pair = comparators.get(i);

			Comparator<RecordType, SchemaElementType> comp = pair.getFirst();

			double similarity = comp.compare(record1, record2, null);
			double weight = pair.getSecond();

			sum += (similarity * weight);

			String name = String.format("[%d] %s", i, comp.getClass()
					.getSimpleName());
			Attribute att = null;
			for(Attribute elem : features.getSchema().get()) {
				if(elem.toString().equals(name)) {
					att = elem;
				}
			}
			if(att==null) {
				att = new Attribute(name);
			}
			model.setValue(att, Double.toString(similarity));
		}

		model.setValue(FeatureVectorDataSet.ATTRIBUTE_FINAL_VALUE, Double.toString(sum));
		model.setValue(FeatureVectorDataSet.ATTRIBUTE_IS_MATCH, Boolean.toString(sum >= getFinalThreshold()));

		return model;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.TrainableMatchingRule#learnParameters(de.uni_mannheim.informatik.dws.winter.model.defaultmodel.FeatureVectorDataSet)
	 */
	@Override
	public Performance learnParameters(FeatureVectorDataSet features) {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.TrainableMatchingRule#storeModel(java.io.File)
	 */
	@Override
	public void storeModel(File location) {
		
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.TrainableMatchingRule#readModel(java.io.File)
	 */
	@Override
	public void readModel(File location) {
		
	}

	@Override
	public FeatureVectorDataSet initialiseFeatures() {
		return new FeatureVectorDataSet();
	}

}
