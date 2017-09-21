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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleFactory;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;

/**
 * Defines which fuser should be applied and which evaluation rules should be
 * used during data fusion process.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type that represents a record
 */
public class DataFusionStrategy<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> {

	private Map<SchemaElementType, AttributeFuser<RecordType, SchemaElementType>> attributeFusers;
	private Map<SchemaElementType, EvaluationRule<RecordType, SchemaElementType>> evaluationRules;
	private FusibleFactory<RecordType, SchemaElementType> factory;

	/**
	 * @return the evaluationRules
	 */
	public Map<SchemaElementType, EvaluationRule<RecordType, SchemaElementType>> getEvaluationRules() {
		return evaluationRules;
	}
	
	/**
	 * Creates a new instance and specifies which factory to use when creating
	 * fused records
	 * 
	 * @param factory	A {@link FusibleFactory} that creates the fused records
	 */
	public DataFusionStrategy(FusibleFactory<RecordType, SchemaElementType> factory) {
		attributeFusers = new HashMap<>();
		evaluationRules = new HashMap<>();
		this.factory = factory;
	}

	/**
	 * Adds a combination of fuser and evaluation rule. The evaluation rule will
	 * be used to evaluate the result of the fuser for the given schema element from the target schema
	 * 
	 * @param schemaElement		the schema element that is fused
	 * @param fuser				the {@link AttributeFuser} that performs the fusion
	 * @param rule				the {@link EvaluationRule} that performs the evaluation
	 */
	public void addAttributeFuser(SchemaElementType schemaElement, AttributeFuser<RecordType, SchemaElementType> fuser, EvaluationRule<RecordType, SchemaElementType> rule) {
		attributeFusers.put(schemaElement, fuser);
		evaluationRules.put(schemaElement, rule);
	}

	/**
	 * Applies the strategy (i.e. all specified fusers) to the given group of
	 * records
	 * 
	 * @param group					the group of records to fused
	 * @param schemaCorrespondences	the schema correspondences
	 * @return The fused record
	 */
	public RecordType apply(RecordGroup<RecordType, SchemaElementType> group, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {
		RecordType fusedRecord = factory.createInstanceForFusion(group);

		for (AttributeFusionTask<RecordType, SchemaElementType> t : getAttributeFusers(group, schemaCorrespondences)) {
			t.execute(group, fusedRecord);
		}

		return fusedRecord;
	}
	
	/**
	 * returns the fusers specified for this strategy
	 * @param group
	 * @param schemaCorrespondences
	 * @return a list of fusion tasks
	 */
	public List<AttributeFusionTask<RecordType, SchemaElementType>> getAttributeFusers(RecordGroup<RecordType, SchemaElementType> group, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {
		List<AttributeFusionTask<RecordType, SchemaElementType>> fusers = new ArrayList<>();

		// if schema correspondences are passed, then we use them
		if(schemaCorrespondences!=null) {
			// collect all correspondences for each element of the target schema 
			Map<SchemaElementType, Processable<Correspondence<SchemaElementType, Matchable>>> byTargetSchema = new HashMap<>();
			
			for(Correspondence<SchemaElementType, Matchable> cor : schemaCorrespondences.get()) {
				
				Processable<Correspondence<SchemaElementType, Matchable>> cors = byTargetSchema.get(cor.getSecondRecord());
				
				if(cors==null) {
					cors = new ProcessableCollection<>();
					byTargetSchema.put(cor.getSecondRecord(), cors);
				}
				
				cors.add(cor);			
			}
			
			for(SchemaElementType elem : byTargetSchema.keySet()) {
				AttributeFusionTask<RecordType, SchemaElementType> t = new AttributeFusionTask<>();
				t.setSchemaElement(elem);
				t.setFuser(attributeFusers.get(elem));
				t.setCorrespondences(byTargetSchema.get(elem));
				t.setEvaluationRule(evaluationRules.get(elem));
				fusers.add(t);
			}
		} else {
			// if no schema correspondences are available (null - not if just no correspondences were generated), we use all available fusers
			for(SchemaElementType elem : attributeFusers.keySet()) {
				AttributeFusionTask<RecordType, SchemaElementType> t = new AttributeFusionTask<>();
				t.setSchemaElement(elem);
				t.setFuser(attributeFusers.get(elem));
				t.setEvaluationRule(evaluationRules.get(elem));
				fusers.add(t);
			}
		}
		
		return fusers;
	}

	/**
	 * calculates the number of non-conflicting values for the given group of
	 * records, according the fusers of this strategy
	 * 
	 * @param group							the group of records
	 * @param schemaCorrespondences			the schema correspondences
	 * @return A map with the attribute consistency values
	 */
	public Map<String, Double> getAttributeConsistency(
			RecordGroup<RecordType, SchemaElementType> group, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences) {
		Map<String, Double> consistencies = new HashMap<>();
		
		List<AttributeFusionTask<RecordType, SchemaElementType>> tasks = getAttributeFusers(group, schemaCorrespondences);

		for (AttributeFusionTask<RecordType, SchemaElementType> fuserTask : tasks) {
			
			AttributeFuser<RecordType, SchemaElementType> fuser = fuserTask.getFuser();
			
			EvaluationRule<RecordType, SchemaElementType> rule = fuserTask.getEvaluationRule();

			// skip if there is no fuser or evaluation rule defined
			if(fuser!=null || rule!=null) {
				
				Double consistency = fuser.getConsistency(group, rule, fuserTask.getCorrespondences(), fuserTask.getSchemaElement());
				
				if(consistency!=null) {
					consistencies.put(fuserTask.getSchemaElement().getIdentifier(), consistency);
				}
			}
		}

		return consistencies;
	}

}
