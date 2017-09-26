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
package de.uni_mannheim.informatik.dws.winter.datafusion;

import java.util.HashMap;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroupFactory;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Evaluates a data fusion result based on a given {@link DataFusionStrategy}
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type that represents a record
 */
public class DataFusionEvaluator<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> {

	private DataFusionStrategy<RecordType, SchemaElementType> strategy;
	private RecordGroupFactory<RecordType, SchemaElementType> groupFactory;
	
	private boolean verbose = false;

	/**
	 * 
	 * @return Returns whether additional information will be written to the console
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * Sets whether additional information will be written to the console
	 * 
	 * @param verbose	whether there should be additional information written to the console
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Creates a new instance with the provided strategy
	 * 
	 * @param strategy		the fusion strategy
	 * @param factory		the factory for record grous
	 */
	public DataFusionEvaluator(DataFusionStrategy<RecordType, SchemaElementType> strategy, RecordGroupFactory<RecordType, SchemaElementType> factory) {
		this.strategy = strategy;
		this.groupFactory = factory;
	}

	/**
	 * Evaluates the the data fusion result against a gold standard
	 * 
	 * @param dataset			the fused data
	 * @param goldStandard		the gold standard
	 * @param schemaCorrespondences	the schema correspondences
	 * @return the accuracy of the data fusion result
	 */
	public double evaluate(FusibleDataSet<RecordType, SchemaElementType> dataset,
			DataSet<RecordType, SchemaElementType> goldStandard, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {

		int correctValues = 0;
		int totalValues = goldStandard.size()
				* strategy.getAttributeFusers(null, schemaCorrespondences).size();
		HashMap<SchemaElementType, Integer> attributeCount = new HashMap<SchemaElementType, Integer>();
		for (AttributeFusionTask<RecordType, SchemaElementType> fusionTask : strategy.getAttributeFusers(null, schemaCorrespondences)) {
			attributeCount.put(fusionTask.getSchemaElement(), 0);
		}

		for (RecordType record : goldStandard.get()) {
			RecordType fused = dataset.getRecord(record.getIdentifier());

			if (fused != null) {
				
				// ask strategy to compare record and fused based on schema correspondences
				RecordGroup<RecordType, SchemaElementType> g = groupFactory.createRecordGroup();
				g.addRecord(record.getIdentifier(), dataset);
				
				for (AttributeFusionTask<RecordType, SchemaElementType> fusionTask : strategy.getAttributeFusers(g, schemaCorrespondences)) {
					EvaluationRule<RecordType, SchemaElementType> r = fusionTask.getEvaluationRule();

					if (r.isEqual(fused, record, fusionTask.getSchemaElement())) { 
						correctValues++;
						attributeCount.put(fusionTask.getSchemaElement(),
								attributeCount.get(fusionTask.getSchemaElement()) + 1);
					} else if (verbose) {
						System.out.println(String.format(
								"[error] %s: %s <> %s", r.getClass()
										.getSimpleName(), fused.toString(),
								record.toString()));
					}
				}
			}
		}
		if (verbose) {
			System.out.println("Attribute-specific Accuracy:");
			for (AttributeFusionTask<RecordType, SchemaElementType> fusionTask : strategy.getAttributeFusers(null, schemaCorrespondences)) {
				double acc = (double) attributeCount.get(fusionTask.getSchemaElement())
						/ (double) goldStandard.size();
				System.out.println(String.format("	%s: %.2f", fusionTask.getSchemaElement().getIdentifier(), acc));

			}
		}

		return (double) correctValues / (double) totalValues;
	}
}
