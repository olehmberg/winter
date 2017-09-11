/** 
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.dws.winter.matching.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.FeatureVectorDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.pmml.PMMLFactory;

/**
 * Class that creates and applies a matching Rule based on supervised learning
 * using the Weka Library.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */

public class WekaMatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable>
		extends FilteringMatchingRule<RecordType, SchemaElementType>
		implements LearnableMatchingRule<RecordType, SchemaElementType> {

	private static final long serialVersionUID = 1L;
	private String[] parameters;
	private Classifier classifier;
	private List<Comparator<RecordType, SchemaElementType>> comparators;

	// Handling of feature subset selection
	private boolean forwardSelection = false;
	private boolean backwardSelection = false;
	private AttributeSelection fs;

	public final String trainingSet = "trainingSet";
	public final String machtSet = "matchSet";

	// TODO Discuss finalThreshold --> Can be set via options -C <confidence
	// factor for pruning>
	/**
	 * Create a MatchingRule, which can be trained using the Weka library for
	 * identity resolution.
	 * 
	 * @param finalThreshold
	 *            determines the confidence level, which needs to be exceeded by
	 *            the classifier, so that it can classify a record as match.
	 * 
	 * @param classifierName
	 *            Has the name of a specific classifier from the Weka library.
	 * 
	 * @param parameters
	 *            Hold the parameters to tune the classifier.
	 */

	public WekaMatchingRule(double finalThreshold, String classifierName, String parameters[]) {
		super(finalThreshold);

		this.parameters = parameters;

		// create classifier
		try {
			this.classifier = (Classifier) Utils.forName(Classifier.class, classifierName, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// create list for comparators
		this.comparators = new LinkedList<>();
	}
	

	public String[] getparameters() {
		return parameters;
	}

	public void setparameters(String[] parameters) {
		this.parameters = parameters;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * Adds a comparator with the specified weight to this rule.
	 * 
	 * @param comparator
	 *            Hold the new to be added comparator
	 */

	public void addComparator(Comparator<RecordType, SchemaElementType> comparator) {
		comparators.add(comparator);
	}

	/**
	 * 
	 * Learns the rule from parsed features in a cross validation and the set
	 * parameters. Additionally feature subset selection is conducted, if the
	 * parameters this.forwardSelection or this.backwardSelection are set
	 * accordingly.
	 * 
	 * @param features
	 *            Contains features to learn a classifier
	 */

	@Override
	public Performance learnParameters(FeatureVectorDataSet features) {
		// create training
		Instances trainingData = transformToWeka(features, this.trainingSet);

		try {
			Evaluation eval = new Evaluation(trainingData);
			// apply feature subset selection
			if (this.forwardSelection || this.backwardSelection) {

				GreedyStepwise search = new GreedyStepwise();
				search.setSearchBackwards(this.backwardSelection);

				this.fs = new AttributeSelection();
				WrapperSubsetEval wrapper = new WrapperSubsetEval();

				// Do feature subset selection, but using a 10-fold cross
				// validation
				wrapper.buildEvaluator(trainingData);
				wrapper.setClassifier(this.classifier);
				wrapper.setFolds(10);
				wrapper.setThreshold(0.01);

				this.fs.setEvaluator(wrapper);
				this.fs.setSearch(search);

				this.fs.SelectAttributes(trainingData);

				trainingData = fs.reduceDimensionality(trainingData);

			}
			// perform 10-fold Cross Validation to evaluate classifier
			eval.crossValidateModel(this.classifier, trainingData, 10, new Random(1));
			System.out.println(eval.toSummaryString("\nResults\n\n", false));
			
			this.classifier.buildClassifier(trainingData);
			
			int truePositive = (int) eval.numTruePositives(trainingData.classIndex());
			int falsePositive = (int) eval.numFalsePositives(trainingData.classIndex());
			int falseNegative = (int) eval.numFalseNegatives(trainingData.classIndex());
			Performance performance = new Performance(truePositive, truePositive + falsePositive,
					truePositive + falseNegative);

			return performance;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Transform features into Weka format
	 * 
	 * @param features
	 *            Holds all features including a label, if training data is
	 *            created.
	 * @param datasetName
	 *            Holds the data set´s name
	 * @return returns the created data set
	 */

	public Instances transformToWeka(FeatureVectorDataSet features, String datasetName) {
		Instances dataset = defineDataset(features, datasetName);
		// Loop through all features
		for (Iterator<Record> iterator = features.get().iterator(); iterator.hasNext();) {
			Record record = iterator.next();

			// calculate feature number
			Collection<Attribute> attributes = features.getSchema().get();
			int featureNum = attributes.size();

			double[] values = new double[featureNum];
			int index = 0;
			for (Iterator<Attribute> attrIterator = attributes.iterator(); attrIterator.hasNext();) {
				Attribute attr = attrIterator.next();
				// get features
				if (!attr.equals(FeatureVectorDataSet.ATTRIBUTE_LABEL)) {
					String feature = record.getValue(attr);
					// convert to double if applicable
					if (feature != null) {
						double featureValue = Double.parseDouble(feature);
						values[index] = featureValue;
					} else {
						values[index] = 0;
					}

					index++;
				}
			}

			Instance inst = new DenseInstance(1.0, values);
			// Treat the label as a special case, which is always at the last
			// position of the dataset.
			if (datasetName.equals(this.trainingSet)) {
				String labelRecord = record.getValue(FeatureVectorDataSet.ATTRIBUTE_LABEL);
				values[index] = dataset.attribute(index).indexOfValue(labelRecord);
			}

			dataset.add(inst);
		}

		return dataset;
	}

	/**
	 * Defines the structure of a Weka table
	 * 
	 * @param features
	 *            Holds all features including a label, if training data is
	 *            created.
	 * @param datasetName
	 *            Holds the dataset´s name
	 * @return returns the empty created dataset
	 */

	private Instances defineDataset(FeatureVectorDataSet features, String datasetName) {
		ArrayList<weka.core.Attribute> attributes = new ArrayList<weka.core.Attribute>();
		// create features per attributes of the FeatureVectorDataSet
		for (Iterator<Attribute> attrIterator = features.getSchema().get().iterator(); attrIterator.hasNext();) {
			Attribute attr = attrIterator.next();
			if (!attr.equals(FeatureVectorDataSet.ATTRIBUTE_LABEL)) {
				weka.core.Attribute attribute = new weka.core.Attribute(attr.getIdentifier());
				attributes.add(attribute);
			}
		}

		// Treat the label as a special case, which is always at the last
		// position of the dataset.
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("0");
		labels.add("1");
		weka.core.Attribute cls = new weka.core.Attribute("class", labels);
		attributes.add(cls);

		Instances dataset = new Instances(datasetName, attributes, 0);
		dataset.setClassIndex(attributes.size() - 1);
		return dataset;
	}

	/**
	 * creates the record with the respective features
	 * 
	 * @param record1
	 *            the first record (must not be null)
	 * @param record2
	 *            the second record (must not be null)
	 * @param schemaCorrespondences
	 *            the schema correspondences between the first and the second
	 *            records
	 * @param features
	 *            feature vector (must not be null)
	 * @return the record containing the respective features
	 */

	public Record generateFeatures(RecordType record1, RecordType record2,
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, FeatureVectorDataSet features) {

		Record model = new Record(String.format("%s-%s", record1.getIdentifier(), record2.getIdentifier()),
				this.getClass().getSimpleName());

		// fill one feature attribute value per added comparator
		for (int i = 0; i < comparators.size(); i++) {

			Comparator<RecordType, SchemaElementType> comp = comparators.get(i);

			double similarity = comp.compare(record1, record2, null);

			String attribute1 = "";
			String attribute2 = "";
			try{
				attribute1 = ((RecordComparator)comp).getAttributeRecord1().toString();
				attribute2 = ((RecordComparator)comp).getAttributeRecord2().toString();
			
			} catch (ClassCastException  e) {
				// Not possible to add attribute names
				//e.printStackTrace();
			}
			
			String name = String.format("[%d] %s %s %s", i, comp.getClass().getSimpleName(), attribute1, attribute2);
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
		}

		return model;

	}

	/**
	 * Apply trained model to a candidate record-pair. Therefore a new
	 * FeatureDataSet is created, which is afterwards classified as match or
	 * non-match
	 * 
	 * @param record1
	 *            the first record (must not be null)
	 * @param record2
	 *            the second record (must not be null)
	 * @param schemaCorrespondences
	 *            the schema correspondences between the first and the second
	 *            records
	 * @return A correspondence holding the input parameters plus the
	 *         classification´s result, which is either match (1.0) or
	 *         non-match(0.0).
	 */

	@Override
	public Correspondence<RecordType, SchemaElementType> apply(RecordType record1, RecordType record2,
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {

		FeatureVectorDataSet matchSet = this.initialiseFeatures();
		Record matchRecord = generateFeatures(record1, record2, schemaCorrespondences, matchSet);

		// transform entry for classification.
		matchSet.add(matchRecord);
		Instances matchInstances = this.transformToWeka(matchSet, this.machtSet);
		
		// reduce dimensions if feature subset selection was applied before.
		if((this.backwardSelection|| this.forwardSelection) && this.fs != null)
			try {
				matchInstances = this.fs.reduceDimensionality(matchInstances);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		// Apply matching rule
		try {
			double result = this.classifier.classifyInstance(matchInstances.firstInstance());
			return new Correspondence<RecordType, SchemaElementType>(record1, record2, result, schemaCorrespondences);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Store model in file system
	 * 
	 * @param location
	 *            file location of a model
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.LearnableMatchingRule#readModel(java.io.File)
	 */

	@Override
	public void storeModel(File location) {
		// serialize model
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(location));
			oos.writeObject(this.getClassifier());
			oos.flush();
			oos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Read model from file system
	 * 
	 * @param location
	 *            file location of a model
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.LearnableMatchingRule#readModel(java.io.File)
	 */

	@Override
	public void readModel(File location) {
		// deserialize model

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(location));
			this.setClassifier((Classifier) ois.readObject());
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			try {
				this.setClassifier((Classifier) PMMLFactory.getPMMLModel(location, null));

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	@Override
	public double compare(RecordType record1, RecordType record2,
			Correspondence<SchemaElementType, Matchable> schemaCorrespondence) {
		return 0;
	}

	/**
	 * Create a new FeatureVectorDataSet with the corresponding features, which
	 * result from the added comparators.
	 * 
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.LearnableMatchingRule#initialiseFeatures()
	 */

	@Override
	public FeatureVectorDataSet initialiseFeatures() {
		FeatureVectorDataSet result = new FeatureVectorDataSet();
		// create one feature per comparator
		for (int i = 0; i < comparators.size(); i++) {

			Comparator<RecordType, SchemaElementType> comp = comparators.get(i);
			
			String attribute1 = "";
			String attribute2 = "";
			try{
				attribute1 = ((RecordComparator)comp).getAttributeRecord1().toString();
				attribute2 = ((RecordComparator)comp).getAttributeRecord2().toString();
			
			} catch (ClassCastException  e) {
				// Not possible to add attribute names
				//e.printStackTrace();
			}
			
			String name = String.format("[%d] %s %s %s", i, comp.getClass().getSimpleName(), attribute1, attribute2);

			Attribute att = new Attribute(name);
			result.addAttribute(att);

		}
		// Add label to feature
		result.addAttribute(FeatureVectorDataSet.ATTRIBUTE_LABEL);
		return result;
	}

	public boolean isForwardSelection() {
		return forwardSelection;
	}

	public void setForwardSelection(boolean forwardSelection) {
		this.forwardSelection = forwardSelection;
	}

	public boolean isBackwardSelection() {
		return backwardSelection;
	}

	public void setBackwardSelection(boolean backwardSelection) {
		this.backwardSelection = backwardSelection;
	}

}
