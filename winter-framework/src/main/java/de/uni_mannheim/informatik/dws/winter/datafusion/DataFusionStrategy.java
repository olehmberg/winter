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
import java.util.*;

import de.uni_mannheim.informatik.dws.winter.model.*;
import org.slf4j.Logger;

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
	 * Return the Debug Fusion Results (Values of the Debug Report)
	 */
	public HashedDataSet<Record, Attribute> getDebugFusionResults(){
		return this.debugFusionResults;
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
	 * Calculate data fusion debug results on record level and write them to file
	 * if logging was enabled via {@link #setCollectDebugResults(boolean) setCollectDebugResults}
	 * @param fusedDataSet: Fused data set
	 */
	protected void calculateRecordLevelDebugResultsAndWriteToFile(FusibleDataSet<RecordType, SchemaElementType> fusedDataSet){
		if(this.debugFusionResults != null) {
			FusibleHashedDataSet<Record, Attribute> debugFusionResultsRecordLevel = new FusibleHashedDataSet<Record, Attribute>();
			List<Attribute> headerDebugResultsRecordLevel = new LinkedList<Attribute>();

			// Initialise Attributes
			Attribute attributeRecordIDS = new Attribute("RecordIDS");
			debugFusionResultsRecordLevel.addAttribute(attributeRecordIDS);
			headerDebugResultsRecordLevel.add(attributeRecordIDS);

			Attribute attributeAvgConsistency = new Attribute("AverageConsistency");
			debugFusionResultsRecordLevel.addAttribute(attributeAvgConsistency);
			headerDebugResultsRecordLevel.add(attributeAvgConsistency);

			Set<String> attributeSet = new HashSet<String>();
			HashMap<String, Attribute> attributeHashMap = new HashMap<String, Attribute>();
			Set<String> recordsIDSet = new HashSet<String>();

			for (Record record : this.debugFusionResults.get()){
				String attributeName = record.getValue(AttributeFusionLogger.ATTRIBUTE_NAME);
				if(!attributeSet.contains(attributeName)){
					attributeSet.add(attributeName);
					Attribute attributeConsistency = new Attribute(attributeName + "-Consistency");
					debugFusionResultsRecordLevel.addAttribute(attributeConsistency);
					headerDebugResultsRecordLevel.add(attributeConsistency);
					attributeHashMap.put(attributeName + "-Consistency", attributeConsistency);

					Attribute attributeValues = new Attribute(attributeName + "-Values");
					debugFusionResultsRecordLevel.addAttribute(attributeValues);
					headerDebugResultsRecordLevel.add(attributeValues);
					attributeHashMap.put(attributeName + "-Values", attributeValues);
				}
				recordsIDSet.add(record.getValue(AttributeFusionLogger.RECORDIDS));
			}

			// Generate Record Level Debug Record
			for (String recordIDs: recordsIDSet){

				//Use original ID to initialize new debug record with full list of identifiers
				String [] originalIDS = recordIDs.split("\\+");
				RecordType fusedRecord = fusedDataSet.getRecord(originalIDS[0]);
				String fusedRecordIdentifier = fusedRecord.getIdentifier();

				Record record = debugFusionResultsRecordLevel.getRecord(fusedRecordIdentifier);
				if (record == null){
					record = new Record(fusedRecord.getIdentifier());
					record.setValue(attributeRecordIDS, fusedRecord.getIdentifier());
				}

				for (String attributeName: attributeSet){
					String recordIdentifier = attributeName + "-{" + recordIDs + "}";
					Record debugRecord = this.debugFusionResults.getRecord(recordIdentifier);
					if(debugRecord != null){
						Attribute attributeConsistency = attributeHashMap.get(attributeName + "-Consistency");
						String consistency = debugRecord.getValue(AttributeFusionLogger.CONSISTENCY);
						record.setValue(attributeConsistency, consistency);

						Attribute attributeValues = attributeHashMap.get(attributeName + "-Values");
						String values = debugRecord.getValue(AttributeFusionLogger.VALUES);
						record.setValue(attributeValues, values);
					}
				}
				debugFusionResultsRecordLevel.add(record);
			}

			//Update Attribute consistencies
			for(Record debugRecord: debugFusionResultsRecordLevel.get()){
				double sumConsistencies = 0;
				int countAttributes = 0;
				for (String attributeName: attributeSet){
					Attribute attributeConsistency = attributeHashMap.get(attributeName + "-Consistency");
					String consistency = debugRecord.getValue(attributeConsistency);
					if (consistency != null){
						sumConsistencies = sumConsistencies + Double.parseDouble(consistency);
						countAttributes++;
					}
				}
				double avgConsistency = sumConsistencies/countAttributes;
				debugRecord.setValue(attributeAvgConsistency, Double.toString(avgConsistency));
			}


			// UPDATE write to file part once new ds is generated
			String debugReportfilePath = this.filePathDebugResults.replace(".csv", "_recordLevel.csv");
			try {
				new RecordCSVFormatter().writeCSV(new File(debugReportfilePath), debugFusionResultsRecordLevel, headerDebugResultsRecordLevel);
				logger.info("Debug results on record level written to file: " + debugReportfilePath);
			} catch (IOException e) {
				logger.error("Debug results on record level could not be written to file: " + debugReportfilePath);
			}
		}
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

		this.debugFusionResults.addAttribute(AttributeFusionLogger.RECORDIDS);
		this.headerDebugResults.add(AttributeFusionLogger.RECORDIDS);
		
		this.debugFusionResults.addAttribute(AttributeFusionLogger.VALUES);
		this.headerDebugResults.add(AttributeFusionLogger.VALUES);
		
		this.debugFusionResults.addAttribute(AttributeFusionLogger.FUSEDVALUE);
		this.headerDebugResults.add(AttributeFusionLogger.FUSEDVALUE);
		
		this.debugFusionResults.addAttribute(AttributeFusionLogger.IS_CORRECT);
		this.headerDebugResults.add(AttributeFusionLogger.IS_CORRECT);

		this.debugFusionResults.addAttribute(AttributeFusionLogger.CORRECT_VALUE);
		this.headerDebugResults.add(AttributeFusionLogger.CORRECT_VALUE);
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
					for (RecordType recordGs : goldStandardForDebug.get()) {
						// Check for record with fused Record ID in Goldstandard
						if(recordGs.getIdentifier().equals(fusedRecord.getIdentifier())){
								fusedInGs = recordGs;
								break;
						}
						else{
							// Check for record with one Record ID from the group of Input Records in Goldstandard
							for(String inputRecordId: group.getRecordIds()){
								if(recordGs.getIdentifier().equals(inputRecordId)){
									fusedInGs = recordGs;
									break;
								}
							}
							if(fusedInGs != null){
								break;
							}
						}
					}
					if(fusedInGs!=null) {
						record.setIsCorrect(t.getEvaluationRule().isEqual(fusedRecord, fusedInGs, t.getSchemaElement()));
						if(attFuser instanceof AttributeValueFuser) {
							AttributeValueFuser avf = (AttributeValueFuser)attFuser;
							Object value = avf.getValue(fusedInGs, null);
							record.setCorrectValue(value);
						}
					}
				}
				this.debugFusionResults.add(record);
			}
		//}
	}
	

}
