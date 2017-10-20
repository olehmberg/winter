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
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.ProgressReporter;

/**
 * Executer class to run the data fusion based on a selected
 * {@link DataFusionStrategy}.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type that represents a record
 */
public class DataFusionEngine<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> {

	private DataFusionStrategy<RecordType, SchemaElementType> strategy;

	/**
	 * @return the strategy
	 */
	public DataFusionStrategy<RecordType, SchemaElementType> getStrategy() {
		return strategy;
	}
	
	/**
	 * Creates a new instance that uses the specified data fusion strategy.
	 * 
	 * @param strategy	the fusion strategy
	 */
	public DataFusionEngine(DataFusionStrategy<RecordType, SchemaElementType> strategy) {
		this.strategy = strategy;
	}

	/**
	 * Runs the data fusion process on the provided set of correspondences and returns a fused data set
	 * 
	 * @param correspondences	correspondences between the records
	 * @param schemaCorrespondences	optional schema correspondences between the records
	 * @return a {@link FusibleDataSet} based on the RecordType of the
	 *         {@link CorrespondenceSet}
	 */
	public FusibleDataSet<RecordType, SchemaElementType> run(
			CorrespondenceSet<RecordType, SchemaElementType> correspondences,
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {
		FusibleDataSet<RecordType, SchemaElementType> fusedDataSet = new FusibleHashedDataSet<>();

		// iterate over all correspondence groups (all records mapped to the same target)
		for (RecordGroup<RecordType, SchemaElementType> clu : correspondences.getRecordGroups()) {
			
			// apply the data fusion strategy
			RecordType fusedRecord = strategy.apply(clu, schemaCorrespondences);
			
			// add the fused record to the fused dataset
			fusedDataSet.add(fusedRecord);

			// keep track of record provenance
			for (RecordType record : clu.getRecords()) {
				fusedDataSet.addOriginalId(fusedRecord, record.getIdentifier());
			}
		}

		return fusedDataSet;
	}

	/**
	 * Calculates the consistencies of the attributes of the records in the
	 * given correspondence set according to the data fusion strategy
	 * 
	 * @param correspondences	correspondences between the records
	 * @param schemaCorrespondences	correspondences between the schema elements
	 * @return A map with the attribute consistency values ("attribute" -&gt; consistency)
	 */
	public Map<String, Double> getAttributeConsistencies(
			CorrespondenceSet<RecordType, SchemaElementType> correspondences,
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {
		Map<String, Double> consistencySums = new HashMap<>(); // = sum of consistency values
		Map<String, Integer> consistencyCounts = new HashMap<>(); // = number of instances

		ProgressReporter progress = new ProgressReporter(correspondences.getRecordGroups().size(), "Calculating consistencies");
		
		// changed to calculation as follows:
		// degree of consistency per instance = percentage of most frequent value
		// consistency = average of degree of consistency per instance
		
		// for each record group (=instance in the target dataset), calculate the degree of consistency for each attribute
		for (RecordGroup<RecordType, SchemaElementType> clu : correspondences.getRecordGroups()) {

			Map<String, Double> values = strategy
					.getAttributeConsistency(clu, schemaCorrespondences);

			for (String att : values.keySet()) {
				Double consistencyValue = values.get(att);
				
				if(consistencyValue!=null) {
					Integer cnt = consistencyCounts.get(att);
					if (cnt == null) {
						cnt = 0;
					}
					consistencyCounts.put(att, cnt + 1);
					
					Double sum = consistencySums.get(att);
					if(sum == null) {
						sum = 0.0;
					}
					consistencySums.put(att, sum + consistencyValue);
				}
			}

			progress.incrementProgress();
			progress.report();
		}

		Map<String, Double> result = new HashMap<>();
		for (String att : consistencySums.keySet()) {
			if(consistencySums.get(att)!=null) {
				// divide by count, not total number of record groups as we only consider groups that actually have a value
				double consistency = consistencySums.get(att)
						/ (double) consistencyCounts.get(att);
				
				result.put(att, consistency);
			}
		}

		return result;
	}

	/**
	 * Calculates the consistencies of the attributes of the records in the
	 * given correspondence set according to the data fusion strategy and prints
	 * the results to the console
	 * 
	 * @param correspondences		correspondences between the records
	 * @param schemaCorrespondences	correspondences between the schema elements
	 */
	public void printClusterConsistencyReport(
			CorrespondenceSet<RecordType, SchemaElementType> correspondences,
			Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {
		System.out.println("Attribute Consistencies:");
		Map<String, Double> consistencies = getAttributeConsistencies(correspondences, schemaCorrespondences);
		for (String att : consistencies.keySet()) {
			System.out.println(String.format("\t%s: %.2f", att,
					consistencies.get(att)));
		}
	}
}
