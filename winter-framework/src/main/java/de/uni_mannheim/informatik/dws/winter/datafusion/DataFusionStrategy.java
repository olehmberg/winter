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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleFactory;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.RecordCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

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
	
	private FusibleHashedDataSet<Record, Attribute> debugFusionResults;
	private boolean collectDebugResults = false;
	private List<Attribute> headerDebugResults;
	private DataSet<RecordType, SchemaElementType> goldStandardForDebug;
	
	private String filePathDebugResults;
	private int	maxDebugLogSize;
	
	
	private static final Logger logger = WinterLogManager.getLogger();
	
	/**
	 * Check whether debug flag is set.
	 * @return	true/false for debug flag
	 */
	public boolean isDebugReportActive() {
		return collectDebugResults;
	}

	/**
	 * Set debug switch and initialize debug results for data fusion.
	 * @param collectDebugResults debug switch
	 */
	private void setCollectDebugResults(boolean collectDebugResults) {
		this.collectDebugResults = collectDebugResults;
		if(this.collectDebugResults){
			initializeFusionResults();
		}
	}
	
	/**
	 * Activates the collection of debug results
	 * 
	 * @param filePath	describes the filePath to the debug results log.
	 * @param maxSize	describes the maximum size of the debug results log.
	 */
	public void activateDebugReport(String filePath, int maxSize){
		activateDebugReport(filePath, maxSize, null);
	}
	
	/**
	 * Activates the collection of debug results
	 * 
	 * @param filePath	describes the filePath to the debug results log.
	 * @param maxSize	describes the maximum size of the debug results log.
	 */
	public void activateDebugReport(String filePath, int maxSize, DataSet<RecordType, SchemaElementType> goldStandard){
		if(filePath != null){
			this.filePathDebugResults = filePath;
			this.maxDebugLogSize = maxSize;
			this.goldStandardForDebug = goldStandard;
			this.setCollectDebugResults(true);
		}
	}

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
	 * Creates a new instance of a {@link FusibleDataSet} and adds attributes for all known attribute fusers.
	 * 
	 * @return the fused data set.
	 */
	public FusibleDataSet<RecordType, SchemaElementType> createFusedDataSet() {
		FusibleDataSet<RecordType, SchemaElementType> fusedDataSet = new FusibleHashedDataSet<>();
		for(SchemaElementType attribute : attributeFusers.keySet()) {
			fusedDataSet.addAttribute(attribute);
		}
		return fusedDataSet;
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
		if(this.collectDebugResults){
			fuser.setCollectDebugResults(true);
		}
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
			if(this.collectDebugResults){
				fillFusionLog(t, group, schemaCorrespondences, fusedRecord);
			}
		}

		return fusedRecord;
	}
	
	/**
	 * returns the fusers specified for this strategy
	 * @param group 	the group, which shall be fused.
	 * @param schemaCorrespondences		the needed schemaCorrespondences
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
	
	/**
	 * Write data fusion debug results to file if logging was enabled via {@link #setCollectDebugResults(boolean) setCollectDebugResults}
	 */
	protected void writeDebugDataFusionResultsToFile(){
		if(this.debugFusionResults != null){
		try {
			new RecordCSVFormatter().writeCSV(new File(this.filePathDebugResults), this.debugFusionResults, this.headerDebugResults);
			logger.info("Debug results written to file: " + this.filePathDebugResults);
		} catch (IOException e) {
			logger.error("Debug results could not be written to file: " + this.filePathDebugResults);
		}
		} else {
			logger.error("No debug results found!");
			logger.error("Is logging enabled?");
		}
	}
	
	/**
	 * Initialize Debug Data Fusion
	 */
	protected void initializeFusionResults() {
		this.debugFusionResults = new FusibleHashedDataSet<Record, Attribute>();
		this.headerDebugResults = new LinkedList<Attribute>();
		
		this.debugFusionResults.addAttribute(AttributeFusionLogger.ATTRIBUTE_NAME);
		this.headerDebugResults.add(AttributeFusionLogger.ATTRIBUTE_NAME);

		this.debugFusionResults.addAttribute(AttributeFusionLogger.CONSISTENCY);
		this.headerDebugResults.add(AttributeFusionLogger.CONSISTENCY);

		this.debugFusionResults.addAttribute(AttributeFusionLogger.VALUEIDS);
		this.headerDebugResults.add(AttributeFusionLogger.VALUEIDS);
		
		this.debugFusionResults.addAttribute(AttributeFusionLogger.VALUES);
		this.headerDebugResults.add(AttributeFusionLogger.VALUES);
		
		this.debugFusionResults.addAttribute(AttributeFusionLogger.FUSEDVALUE);
		this.headerDebugResults.add(AttributeFusionLogger.FUSEDVALUE);
		
		this.debugFusionResults.addAttribute(AttributeFusionLogger.IS_CORRECT);
		this.headerDebugResults.add(AttributeFusionLogger.IS_CORRECT);
	}
	
	/**
	 * Add log entry to debug results log.
	 */
	protected void fillFusionLog(AttributeFusionTask<RecordType, SchemaElementType> t, RecordGroup<RecordType, SchemaElementType> group, Processable<Correspondence<SchemaElementType, Matchable>> schemaCorrespondences, RecordType fusedRecord){
		//for(AttributeFuser<RecordType, SchemaElementType> attFuser : this.attributeFusers.values()){
			AttributeFuser<RecordType, SchemaElementType> attFuser = t.getFuser();
			if(attFuser.getFusionLog() != null && (this.maxDebugLogSize == -1 || this.debugFusionResults.size() < this.maxDebugLogSize)){
				AttributeFusionLogger record = attFuser.getFusionLog();
				record.setAttributeName(t.getSchemaElement().getIdentifier());
				Double consistency = attFuser.getConsistency(group, t.getEvaluationRule(), schemaCorrespondences, t.getSchemaElement());
				if(consistency!=null) {
					record.setConsistency(consistency);
				}
				if(goldStandardForDebug!=null) {
					RecordType fusedInGs = null;
					for(RecordType inputRecord : group.getRecords()) {
						fusedInGs = goldStandardForDebug.getRecord(inputRecord.getIdentifier());
						if(fusedInGs!=null) {
							break;
						}
					}
					if(fusedInGs!=null) {
						record.setIsCorrect(t.getEvaluationRule().isEqual(fusedRecord, fusedInGs, t.getSchemaElement()));
					}
				}
				this.debugFusionResults.add(record);
			}
		//}
	}
	

}
