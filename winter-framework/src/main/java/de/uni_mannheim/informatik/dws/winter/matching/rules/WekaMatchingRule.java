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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.FeatureVectorDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.RecordCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.utils.weka.EvaluationWithBalancing;
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
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;

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
	private static final Logger logger = WinterLogManager.getLogger();

	// Handling of feature subset selection
	private boolean forwardSelection = false;
	private boolean backwardSelection = false;
	private AttributeSelection fs;
	private boolean balanceTrainingData = false;

	public final String trainingSet = "trainingSet";
	public final String matchSet = "matchSet";

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

		this.initialiseClassifier(classifierName, parameters);

		// create list for comparators
		this.comparators = new LinkedList<>();
	}

	/**
	 * Create an empty MatchingRule without any classifier. The classifier has
	 * to be added later on, which can be trained using the Weka library for
	 * identity resolution.
	 * 
	 * @param finalThreshold
	 *            determines the confidence level, which needs to be exceeded by
	 *            the classifier, so that it can classify a record as match.
	 */

	public WekaMatchingRule(double finalThreshold) {
		super(finalThreshold);

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

	public void initialiseClassifier(String classifierName, String parameters[]) {
		this.parameters = parameters;

		// create classifier
		try {
			this.classifier = (Classifier) Utils.forName(Classifier.class, classifierName, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a comparator with the specified weight to this rule.
	 * 
	 * @param comparator
	 *            Hold the new to be added comparator
	 */

	public void addComparator(Comparator<RecordType, SchemaElementType> comparator) {
		comparators.add(comparator);
		if (this.isCollectDebugResults()) {
			comparator.setComparisonLog(new ComparatorLogger(comparator.getClass().getName()));
			addComparatorToLog(comparator);
		}
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
		if (this.classifier != null) {
			// create training
			Instances trainingData = transformToWeka(features, this.trainingSet);

			try {
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
				Evaluation eval = new Evaluation(trainingData);

				if (balanceTrainingData) {
					Resample filter = new Resample();
					filter.setBiasToUniformClass(1.0);
					filter.setInputFormat(trainingData);
					filter.setSampleSizePercent(100);
					eval = new EvaluationWithBalancing(trainingData, filter);
				}

				eval.crossValidateModel(this.classifier, trainingData, Math.min(10, trainingData.size()),
						new Random(1));

				for (String line : eval.toSummaryString("\nResults\n\n", false).split("\n")) {
					logger.info(line);
				}

				for (String line : eval.toClassDetailsString().split("\n")) {
					logger.info(line);
				}

				for (String line : eval.toMatrixString().split("\n")) {
					logger.info(line);
				}

				if (balanceTrainingData) {
					Resample filter = new Resample();
					filter.setBiasToUniformClass(1.0);
					filter.setInputFormat(trainingData);
					filter.setSampleSizePercent(100);
					trainingData = Filter.useFilter(trainingData, filter);
				}

				this.classifier.buildClassifier(trainingData);

				int positiveClassIndex = trainingData.attribute(trainingData.classIndex()).indexOfValue("1");

				int truePositive = (int) eval.numTruePositives(positiveClassIndex);
				int falsePositive = (int) eval.numFalsePositives(positiveClassIndex);
				int falseNegative = (int) eval.numFalseNegatives(positiveClassIndex);
				Performance performance = new Performance(truePositive, truePositive + falsePositive,
						truePositive + falseNegative);

				return performance;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			logger.error("Please initialise a classifier!");
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
		labels.add("1");
		labels.add("0");
		weka.core.Attribute cls = new weka.core.Attribute(FeatureVectorDataSet.ATTRIBUTE_LABEL.getIdentifier(), labels);
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
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences,
			FeatureVectorDataSet features) {

		Record model = new Record(String.format("%s-%s", record1.getIdentifier(), record2.getIdentifier()),
				this.getClass().getSimpleName());

		Record debug = null;
		if (this.isCollectDebugResults() && this.continueCollectDebugResults()) {
			debug = initializeDebugRecord(record1, record2, -1);
		}

		// fill one feature attribute value per added comparator
		for (int i = 0; i < comparators.size(); i++) {

			Comparator<RecordType, SchemaElementType> comp = comparators.get(i);

			// check if there is a schema correspondence that we can pass on to
			// the comparator
			Correspondence<SchemaElementType, Matchable> schemaCorrespondence = null;
			if (schemaCorrespondences != null) {
				schemaCorrespondence = getCorrespondenceForComparator(schemaCorrespondences, record1, record2, comp);
			}

			double similarity = comp.compare(record1, record2, schemaCorrespondence);

			String attribute1 = "";
			String attribute2 = "";
			try {
				attribute1 = ((RecordComparator) comp).getAttributeRecord1().toString();
				attribute2 = ((RecordComparator) comp).getAttributeRecord2().toString();

			} catch (ClassCastException e) {
				// Not possible to add attribute names
				// e.printStackTrace();
			}

			String name = String.format("[%d] %s %s %s", i, getComparatorName(comp), attribute1, attribute2).trim();
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

			if (this.isCollectDebugResults() && this.continueCollectDebugResults()) {
				debug = fillDebugRecord(debug, comp, i);
				addDebugRecordShort(record1, record2, comp, i);
			}
		}

		if (this.isCollectDebugResults() && this.continueCollectDebugResults()) {
			fillSimilarity(debug, null);
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

		if (this.classifier == null) {
			logger.error("Please initialise a classifier!");
			return null;
		} else {
			FeatureVectorDataSet matchSet = this.initialiseFeatures();
			Record matchRecord = generateFeatures(record1, record2, schemaCorrespondences, matchSet);

			// transform entry for classification.
			matchSet.add(matchRecord);
			Instances matchInstances = this.transformToWeka(matchSet, this.matchSet);

			// reduce dimensions if feature subset selection was applied before.
			if ((this.backwardSelection || this.forwardSelection) && this.fs != null)
				try {
					matchInstances = this.fs.reduceDimensionality(matchInstances);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			// Apply matching rule
			try {
				double[] distribution = this.classifier.distributionForInstance(matchInstances.firstInstance());
				int positiveClassIndex = matchInstances.attribute(matchInstances.classIndex()).indexOfValue("1");
				double matchConfidence = distribution[positiveClassIndex];
				if (this.isCollectDebugResults()) {
					fillSimilarity(record1, record2, matchConfidence);
				}
				return new Correspondence<RecordType, SchemaElementType>(record1, record2, matchConfidence,
						schemaCorrespondences);

			} catch (Exception e) {
				e.printStackTrace();
				logger.error(String.format("Classifier Exception for Record '%s': %s",
						matchRecord == null ? "null" : matchRecord.toString(), e.getMessage()));
			}
			return null;
		}
	}

	/**
	 * Store model in file system
	 * 
	 * @param location
	 *            file location of a model
	 * @see de.uni_mannheim.informatik.dws.winter.matching.rules.LearnableMatchingRule#readModel(java.io.File)
	 */

	@Override
	public void exportModel(File location) {
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
				if(e1.getMessage().contains("[TargetMetaInfo]")){
					this.transformPMMLModel(location);
					readModel(location);
				}
				else{
					e1.printStackTrace();
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Transforms the pmml-xml (generated by Rapidminer) such that WEKA can interpret them appropriately.
	 * 
	 * @param location file location
	 */
	private void transformPMMLModel(File location){
		try {
	         SAXReader reader = new SAXReader();
	         Document document = reader.read( location );

	         @SuppressWarnings("unchecked")
			List<Node> nodes = document.selectNodes("//*");

	         for (Node node : nodes) {
	            Element element = (Element)node;
	            
	            
	            /*
	             *  WEKA expects the TargetValue to have the attribute priorProbability.
	             *  Rapidminer does not provide this information, that's why a dummy value is added for the attribute priorProbability. 
	             */
	            
	            if(element.getQualifiedName().equals("TargetValue") && element.attribute("priorProbability") == null){
	            	element.addAttribute("priorProbability", "0.50");
	            }

	            /*
	             * Rapidminer adds the value "MISSING_VALUE" to the class label.
	             * The WEKA rule cannot deal with this third value, as it changes the confidence distribution to three classes instead of two.
	             * Thus, the value "MISSING_VALUE" is removed.
	             */
	            if(element.getQualifiedName().equals("Value") && element.attributeValue("value").equals("MISSING_VALUE")){
	            	element.detach();
	            }  
	         }
	     
	         OutputFormat format = OutputFormat.createPrettyPrint();
	         XMLWriter writer;
	         FileOutputStream ous = new FileOutputStream(location);
	         writer = new XMLWriter( ous, format );
	         writer.write( document );
	         logger.info("PPML model transformed!");
	         
	      } catch (DocumentException e) {
	         e.printStackTrace();
	      } 
		
		catch (UnsupportedEncodingException e) {         
	         e.printStackTrace();
	    } catch (IOException e) {
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
			try {
				attribute1 = ((RecordComparator) comp).getAttributeRecord1().toString();
				attribute2 = ((RecordComparator) comp).getAttributeRecord2().toString();

			} catch (ClassCastException e) {
				// Not possible to add attribute names
				// e.printStackTrace();
			}

			String name = String.format("[%d] %s %s %s", i, getComparatorName(comp), attribute1, attribute2).trim();

			Attribute att = new Attribute(name);
			result.addAttribute(att);

		}
		// Add label to feature
		result.addAttribute(FeatureVectorDataSet.ATTRIBUTE_LABEL);
		return result;
	}

	protected String getComparatorName(Comparator<RecordType, SchemaElementType> comp) {
		return comp.getClass().getSimpleName();
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

	public void setBalanceTrainingData(boolean balanceTrainingData) {
		this.balanceTrainingData = balanceTrainingData;
	}

	public String getModelDescription() {
		return String.format("%s", classifier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("WekaMatchingRule: p(match|%s)", StringUtils.join(Q.project(comparators, (c) -> c), ", "));
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

	}
}
