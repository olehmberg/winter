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
import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.RecordCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * Super class for all matching rules.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type of records that are matched with this rule
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType
 */
public abstract class MatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable> 
	implements Comparator<RecordType,SchemaElementType>,
	RecordMapper<Correspondence<RecordType, SchemaElementType>, Correspondence<RecordType, SchemaElementType>>
{

	private static final long serialVersionUID = 1L;
	private double finalThreshold;
	
	private int resultSize;
	
	private String[] headerComparatorLog = {"MatchingRule", "Record1Identifier", "Record2Identifier", "TotalSimilarity"};
	private FusibleHashedDataSet<Record, Attribute> comparatorLog;
	private boolean collectDebugResults = false;
	
	private static final Logger logger = WinterLogManager.getLogger();

	public double getFinalThreshold() {
		return finalThreshold;
	}

	public void setFinalThreshold(double finalThreshold) {
		this.finalThreshold = finalThreshold;
	}

	public MatchingRule(double finalThreshold) {
		this.finalThreshold = finalThreshold;
	}

	public void setResultSize(int size) {
		this.resultSize = size;
	}
	
	public boolean isCollectDebugResults() {
		return collectDebugResults;
	}

	public void setCollectDebugResults(boolean collectDebugResults) {
		this.collectDebugResults = collectDebugResults;
		if(this.collectDebugResults){
			initialiseBlockingResults();	
		}
	}
	
	public FusibleHashedDataSet<Record, Attribute> getComparatorLog() {
		return comparatorLog;
	}

	public Correspondence<SchemaElementType, Matchable> getCorrespondenceForComparator(
			Processable<Correspondence<SchemaElementType, Matchable>> correspondences,
			RecordType record1,
			RecordType record2,
			Comparator<RecordType, SchemaElementType> comparator) {
		if(correspondences!=null) {
			Processable<Correspondence<SchemaElementType, Matchable>> matchingSchemaCorrespondences = correspondences
					// first filter correspondences to make sure we only use correspondences between the data sources of record1 and record2
				.where((c)->
					c.getFirstRecord().getDataSourceIdentifier()==record1.getDataSourceIdentifier()
					&&
					c.getSecondRecord().getDataSourceIdentifier()==record2.getDataSourceIdentifier()
					)
					// then filter the remaining correspondences based on the comparators arguments, if present
				.where((c)->
					(comparator.getFirstSchemaElement(record1)==null || comparator.getFirstSchemaElement(record1).equals(c.getFirstRecord()))
					&&
					(comparator.getSecondSchemaElement(record2)==null || comparator.getSecondSchemaElement(record2).equals(c.getSecondRecord()))
					);
			// after the filtering, there should only be one correspondence left (if not, the mapping is ambiguous)
			return matchingSchemaCorrespondences.firstOrNull();
		} else {
			return null;
		}
	}
	
	public void writeDebugMatchingResultsToFile(String path) throws IOException{
		new RecordCSVFormatter().writeCSV(
				new File(path), this.comparatorLog);
		logger.info("Debug Matching Results written to file!");
	}

	public void initialiseBlockingResults() {
		FusibleHashedDataSet<Record, Attribute> result = new FusibleHashedDataSet<Record, Attribute>();
		
		for(int i = 0; i < this.headerComparatorLog.length; i++){
			Attribute att = new Attribute(this.headerComparatorLog[i]);
			result.addAttribute(att);
		}
	
		this.comparatorLog = result;
	}
	
	public void addComparatorToLog(){
		
		int counter = 0;
		String compIdentifier = counter + ComparatorLogger.COMPARATORNAME.getIdentifier();
		while(this.comparatorLog.getSchema().getRecord(compIdentifier) != null){
			counter += 1;
			compIdentifier = counter + ComparatorLogger.COMPARATORNAME.getIdentifier();
		}
		
		this.comparatorLog.getSchema().add(new Attribute(counter + ComparatorLogger.COMPARATORNAME.getIdentifier()));
		this.comparatorLog.getSchema().add(new Attribute(counter + ComparatorLogger.RECORD1VALUE.getIdentifier()));
		this.comparatorLog.getSchema().add(new Attribute(counter + ComparatorLogger.RECORD2VALUE.getIdentifier()));
		this.comparatorLog.getSchema().add(new Attribute(counter + ComparatorLogger.RECORD1PREPROCESSEDVALUE.getIdentifier()));
		this.comparatorLog.getSchema().add(new Attribute(counter + ComparatorLogger.RECORD2PREPROCESSEDVALUE.getIdentifier()));
		this.comparatorLog.getSchema().add(new Attribute(counter + ComparatorLogger.SIMILARITY.getIdentifier()));
		this.comparatorLog.getSchema().add(new Attribute(counter + ComparatorLogger.POSTPROCESSEDSIMILARITY.getIdentifier()));
	}
}
