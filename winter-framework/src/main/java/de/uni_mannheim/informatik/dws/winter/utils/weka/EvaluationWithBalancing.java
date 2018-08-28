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
package de.uni_mannheim.informatik.dws.winter.utils.weka;

import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.core.Instances;
import weka.filters.Filter;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class EvaluationWithBalancing extends Evaluation {

	private Filter trainingDataFilter;
	
	/**
	 * @param data
	 * @throws Exception
	 */
	public EvaluationWithBalancing(Instances data, Filter trainingDataFilter) throws Exception {
		super(data);
		this.trainingDataFilter = trainingDataFilter;
	}

	private static final long serialVersionUID = 1L;
	
	/* (non-Javadoc)
	 * @see weka.classifiers.evaluation.Evaluation#crossValidateModel(weka.classifiers.Classifier, weka.core.Instances, int, java.util.Random, java.lang.Object[])
	 */
	@Override
	public void crossValidateModel(Classifier classifier, Instances data, int numFolds, Random random,
			Object... forPredictionsPrinting) throws Exception {
	    // Make a copy of the data we can reorder
	    data = new Instances(data);
	    data.randomize(random);
	    if (data.classAttribute().isNominal()) {
	      data.stratify(numFolds);
	    }

	    // We assume that the first element is a
	    // weka.classifiers.evaluation.output.prediction.AbstractOutput object
	    AbstractOutput classificationOutput = null;
	    if (forPredictionsPrinting.length > 0) {
	      // print the header first
	      classificationOutput = (AbstractOutput) forPredictionsPrinting[0];
	      classificationOutput.setHeader(data);
	      classificationOutput.printHeader();
	    }

	    // Do the folds
	    for (int i = 0; i < numFolds; i++) {
	      Instances train = data.trainCV(numFolds, i, random);
	      train = Filter.useFilter(train, trainingDataFilter);
	      setPriors(train);
	      Classifier copiedClassifier = AbstractClassifier.makeCopy(classifier);
	      copiedClassifier.buildClassifier(train);
	      Instances test = data.testCV(numFolds, i);
	      evaluateModel(copiedClassifier, test, forPredictionsPrinting);
	    }
	    m_NumFolds = numFolds;

	    if (classificationOutput != null) {
	      classificationOutput.printFooter();
	    }
	}

}
