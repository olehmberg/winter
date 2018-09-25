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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
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
 * @param <RecordType>
 *            the type of records that are matched with this rule
 * @param <SchemaElementType>
 *            the type of schema elements that are used in the schema of
 *            RecordType
 */
public abstract class MatchingRule<RecordType extends Matchable, SchemaElementType extends Matchable>
		implements Comparator<RecordType, SchemaElementType>,
		RecordMapper<Correspondence<RecordType, SchemaElementType>, Correspondence<RecordType, SchemaElementType>> {

	private static final long serialVersionUID = 1L;
	private double finalThreshold;

	private FusibleHashedDataSet<Record, Attribute> comparatorLog;
	private FusibleHashedDataSet<Record, Attribute> comparatorLogShort;
	private boolean collectDebugResults = false;
	private HashMap<Attribute, Attribute> resultToComparatorLog;
	private HashMap<String, Attribute> comparatorToResultLog;
	private List<Attribute> headerDebugResults;
	private List<Attribute> headerDebugResultsShort;
	private String filePathDebugResults;
	private int	maxDebugLogSize;
	private MatchingGoldStandard debugGoldStandard;

	private ComparatorLogger comparisonLog;

	private static final Logger logger = WinterLogManager.getLogger();

	public final Attribute MATCHINGRULE = new Attribute("MatchingRule");
	public final Attribute RECORD1IDENTIFIER = new Attribute("Record1Identifier");
	public final Attribute RECORD2IDENTIFIER = new Attribute("Record2Identifier");
	public final Attribute TOTALSIMILARITY = new Attribute("TotalSimilarity");
	public final Attribute ATTRIBUTE_IS_MATCH = new Attribute("IsMatch");

	public double getFinalThreshold() {
		return finalThreshold;
	}

	public void setFinalThreshold(double finalThreshold) {
		this.finalThreshold = finalThreshold;
	}

	public MatchingRule(double finalThreshold) {
		this.finalThreshold = finalThreshold;
	}

	/**
	 * Switch to collect debug results
	 * 
	 * @return
	 */
	public boolean isDebugReportActive() {
		return collectDebugResults;
	}
	
	/**
	 * Continue to collect debug results if result log shorter than maxDebugLogSize
	 * 
	 * @return
	 */
	protected boolean continueCollectDebugResults() {
		if(this.maxDebugLogSize == -1 || this.comparatorLog.size() < this.maxDebugLogSize){
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Set switch to collect debug results and initialize corresponding schema.
	 */
	private void setCollectDebugResults(boolean collectDebugResults) {
		this.collectDebugResults = collectDebugResults;
		if (this.collectDebugResults) {
			initializeMatchingResults();
		}
	}

	/**
	 * Returns the comparator comparison log
	 * 
	 * @return
	 */
	protected HashMap<Attribute, Attribute> getResultToComparatorLog() {
		return resultToComparatorLog;
	}

	public Correspondence<SchemaElementType, Matchable> getCorrespondenceForComparator(
			Processable<Correspondence<SchemaElementType, Matchable>> correspondences, RecordType record1,
			RecordType record2, Comparator<RecordType, SchemaElementType> comparator) {
		if (correspondences != null) {
			Processable<Correspondence<SchemaElementType, Matchable>> matchingSchemaCorrespondences = correspondences
					// first filter correspondences to make sure we only use
					// correspondences between the data sources of record1 and
					// record2
					.where((c) -> c.getFirstRecord().getDataSourceIdentifier() == record1.getDataSourceIdentifier()
							&& c.getSecondRecord().getDataSourceIdentifier() == record2.getDataSourceIdentifier())
					// then filter the remaining correspondences based on the
					// comparators arguments, if present
					.where((c) -> (comparator.getFirstSchemaElement(record1) == null
							|| comparator.getFirstSchemaElement(record1).equals(c.getFirstRecord()))
							&& (comparator.getSecondSchemaElement(record2) == null
									|| comparator.getSecondSchemaElement(record2).equals(c.getSecondRecord())));
			// after the filtering, there should only be one correspondence left
			// (if not, the mapping is ambiguous)
			return matchingSchemaCorrespondences.firstOrNull();
		} else {
			return null;
		}
	}

	/**
	 * Initialize Debug Matching Results.
	 */
	protected void initializeMatchingResults() {
		this.comparatorLog = new FusibleHashedDataSet<Record, Attribute>();
		this.comparatorLogShort = new FusibleHashedDataSet<Record, Attribute>();
		this.headerDebugResults = new LinkedList<Attribute>();
		this.headerDebugResultsShort = new LinkedList<Attribute>();

		this.comparatorLog.addAttribute(this.MATCHINGRULE);
		this.comparatorLogShort.addAttribute(this.MATCHINGRULE);
		this.headerDebugResults.add(this.MATCHINGRULE);
		this.headerDebugResultsShort.add(this.MATCHINGRULE);

		this.comparatorLog.addAttribute(this.RECORD1IDENTIFIER);
		this.comparatorLogShort.addAttribute(this.RECORD1IDENTIFIER);
		this.headerDebugResults.add(this.RECORD1IDENTIFIER);
		this.headerDebugResultsShort.add(this.RECORD1IDENTIFIER);

		this.comparatorLog.addAttribute(this.RECORD2IDENTIFIER);
		this.comparatorLogShort.addAttribute(this.RECORD2IDENTIFIER);
		this.headerDebugResults.add(this.RECORD2IDENTIFIER);
		this.headerDebugResultsShort.add(this.RECORD2IDENTIFIER);

		this.comparatorLog.addAttribute(this.TOTALSIMILARITY);
		this.headerDebugResults.add(this.TOTALSIMILARITY);

		this.comparatorLog.addAttribute(this.ATTRIBUTE_IS_MATCH);
		this.headerDebugResults.add(this.ATTRIBUTE_IS_MATCH);

		this.comparatorLogShort.addAttribute(ComparatorLogger.COMPARATORNAME);
		this.headerDebugResultsShort.add(ComparatorLogger.COMPARATORNAME);

		this.comparatorLogShort.addAttribute(ComparatorLogger.RECORD1VALUE);
		this.headerDebugResultsShort.add(ComparatorLogger.RECORD1VALUE);

		this.comparatorLogShort.addAttribute(ComparatorLogger.RECORD2VALUE);
		this.headerDebugResultsShort.add(ComparatorLogger.RECORD2VALUE);

		this.comparatorLogShort.addAttribute(ComparatorLogger.RECORD1PREPROCESSEDVALUE);
		this.headerDebugResultsShort.add(ComparatorLogger.RECORD1PREPROCESSEDVALUE);

		this.comparatorLogShort.addAttribute(ComparatorLogger.RECORD2PREPROCESSEDVALUE);
		this.headerDebugResultsShort.add(ComparatorLogger.RECORD2PREPROCESSEDVALUE);

		this.comparatorLogShort.addAttribute(ComparatorLogger.SIMILARITY);
		this.headerDebugResultsShort.add(ComparatorLogger.SIMILARITY);

		this.comparatorLogShort.addAttribute(ComparatorLogger.POSTPROCESSEDSIMILARITY);
		this.headerDebugResultsShort.add(ComparatorLogger.POSTPROCESSEDSIMILARITY);

		this.resultToComparatorLog = new HashMap<Attribute, Attribute>();
		this.comparatorToResultLog = new HashMap<String, Attribute>();

	}

	/**
	 * Enhances the schema of the comparator logs (long/short) to collect
	 * results for each comparator.
	 * 
	 * @param comparator
	 *            The comparator for which the log`s schema shall be enhanced.
	 */
	protected void addComparatorToLog(Comparator<RecordType, SchemaElementType> comparator) {

		// 4 fix attributes as defined in initialiseMatchingResults().
		int position = (this.comparatorLog.getSchema().size() - 4) / ComparatorLogger.COMPARATORLOG.length;

		for (Attribute att : ComparatorLogger.COMPARATORLOG) {
			String schemaIdentifier = Integer.toString(position) + '-' + comparator.getClass().getSimpleName() + '-'
					+ att.getIdentifier();
			Attribute schemaAttribute = new Attribute(schemaIdentifier);
			this.resultToComparatorLog.put(schemaAttribute, att);
			this.comparatorToResultLog.put(schemaIdentifier, schemaAttribute);
			this.comparatorLog.getSchema().add(schemaAttribute);
			if (!att.getIdentifier().equals(ComparatorLogger.COMPARATORNAME.getIdentifier())) {
				this.headerDebugResults.add(schemaAttribute);
			}
		}
	}

	/**
	 * Initializes a new record for a debug result based on the input records
	 * and the position of the corresponding comparator.
	 * 
	 * @param record1
	 *            Original data record
	 * @param record2
	 *            Original data record
	 * @param position
	 *            Position of the corresponding comparator when called for a
	 *            short debug log entry.
	 * @return New debug results record.
	 */
	protected Record initializeDebugRecord(RecordType record1, RecordType record2, int position) {

		String identifier = record1.getIdentifier() + "-" + record2.getIdentifier();
		if (position != -1) {
			identifier = Integer.toString(position) + identifier;
		}
		Record debug = new Record(identifier);
		debug.setValue(this.MATCHINGRULE, getClass().getSimpleName());
		debug.setValue(this.RECORD1IDENTIFIER, record1.getIdentifier());
		debug.setValue(this.RECORD2IDENTIFIER, record2.getIdentifier());

		return debug;
	}

	/**
	 * Fills a debug result from the corresponding comparators log.
	 * 
	 * @param debug
	 *            Debug record
	 * @param comparator
	 *            Source comparator.
	 * @param position
	 *            Comparator's position
	 * @return Filled debug record.
	 */
	protected Record fillDebugRecord(Record debug, Comparator<RecordType, SchemaElementType> comparator, int position) {
		ComparatorLogger compLog = comparator.getComparisonLog();
		if (compLog != null) {
			for (Attribute att : ComparatorLogger.COMPARATORLOG) {
				String identifier = Integer.toString(position) + '-' + comparator.getClass().getSimpleName() + '-'
						+ att.getIdentifier();
				Attribute schemaAtt = comparatorToResultLog.get(identifier);

				if (att == ComparatorLogger.RECORD1PREPROCESSEDVALUE) {
					debug.setValue(schemaAtt, compLog.getRecord1PreprocessedValue());
				} else if (att == ComparatorLogger.RECORD2PREPROCESSEDVALUE) {
					debug.setValue(schemaAtt, compLog.getRecord2PreprocessedValue());
				} else if (att == ComparatorLogger.POSTPROCESSEDSIMILARITY) {
					debug.setValue(schemaAtt, compLog.getPostprocessedSimilarity());
				} else {
					debug.setValue(schemaAtt, compLog.getValue(att));
				}
			}
		} else {
			logger.error("A comparator's log is not defined!");
			logger.error(
					"Please check whether logging was enabled before the comparators were added to the matching rule!");
		}
		return debug;
	}

	/**
	 * Adds a new record to the short debug log for a candidate match
	 * (record1-record2) based on a comparator and its position.
	 * 
	 * @param record1
	 *            Original record 1
	 * @param record2
	 *            Original record 2
	 * @param comparator
	 *            Corresponding comparator
	 * @param position
	 *            Position of the corresponding comparator
	 */
	protected void addDebugRecordShort(RecordType record1, RecordType record2,
			Comparator<RecordType, SchemaElementType> comparator, int position) {
		Record debug = initializeDebugRecord(record1, record2, position);
		ComparatorLogger compLog = comparator.getComparisonLog();
		if (compLog != null) {
			debug.setValue(ComparatorLogger.COMPARATORNAME, compLog.getComparatorName());
			debug.setValue(ComparatorLogger.RECORD1VALUE, compLog.getRecord1Value());
			debug.setValue(ComparatorLogger.RECORD2VALUE, compLog.getRecord2Value());
			debug.setValue(ComparatorLogger.RECORD1PREPROCESSEDVALUE, compLog.getRecord1PreprocessedValue());
			debug.setValue(ComparatorLogger.RECORD2PREPROCESSEDVALUE, compLog.getRecord2PreprocessedValue());
			debug.setValue(ComparatorLogger.SIMILARITY, compLog.getPostprocessedSimilarity());
			debug.setValue(ComparatorLogger.POSTPROCESSEDSIMILARITY, compLog.getPostprocessedSimilarity());
			
			this.comparatorLogShort.add(debug);
			
		} else {
			logger.error("A comparator's log is not defined!");
			logger.error(
					"Please check whether logging was enabled before the comparators were added to the matching rule!");
		}
	}

	/**
	 * Fills the similarity value of a debug record based on its identifier.
	 * 
	 * @param record1
	 *            Original Record1
	 * @param record2
	 *            Original Record2
	 * @param similarity
	 *            Similarity value
	 */
	protected void fillSimilarity(RecordType record1, RecordType record2, double similarity) {
		String identifier = record1.getIdentifier() + "-" + record2.getIdentifier();
		Record debug = this.comparatorLog.getRecord(identifier);
		if(debug != null){
			debug.setValue(TOTALSIMILARITY, Double.toString(similarity));
		}
	}
	
	/**
	 * Activates the collection of debug results
	 * 
	 * @param filePath	describes the filePath to the debug results log.
	 * @param maxSize	describes the maximum size of the debug results log.
	 * @param debugGoldstandard	can be used to annotate the debug results log with matching information from a goldstandard
	 */
	public void activateDebugReport(String filePath, int maxSize, MatchingGoldStandard debugGoldstandard){
		if(filePath != null){
			this.filePathDebugResults = filePath;
			this.maxDebugLogSize = maxSize;
			this.setCollectDebugResults(true);
			this.debugGoldStandard = debugGoldstandard;
		}
	}
	
	/**
	 * Activates the collection of debug results
	 * 
	 * @param filePath	describes the filePath to the debug results log.
	 * @param maxSize	describes the maximum size of the debug results log.
	 */
	public void activateDebugReport(String filePath, int maxSize){
		this.activateDebugReport(filePath, maxSize, null);
	}

	/**
	 * Fills the similarity value of a debug record. This debug record is added to the list of
	 * debug results if the list's length does not exceed the maximum number of result entries yet.
	 * Additionally, the filling status is measured in percentage to be more restricted when adding new entry over time.
	 * 
	 * @param debug
	 *            Debug record
	 * @param similarity
	 *            Similarity value
	 */
	protected void fillSimilarity(Record debug, Double similarity) {
		if (similarity != null) {
			debug.setValue(TOTALSIMILARITY, Double.toString(similarity));
		}
		
		this.comparatorLog.add(debug);
		
	}

	/**
	 * Write data matching debug results to file if logging was enabled via
	 * {@link #setCollectDebugResults(boolean) setCollectDebugResults}
	 */
	public void writeDebugMatchingResultsToFile(){
		if(this.debugGoldStandard != null){
			addGoldstandardToDebugResults();
		}
		if (this.comparatorLog != null && this.comparatorLogShort != null  && this.filePathDebugResults != null) {
			try {
				new RecordCSVFormatter().writeCSV(new File(this.filePathDebugResults), this.comparatorLog, this.headerDebugResults);
			logger.info("Debug results written to file: " + this.filePathDebugResults);
			new RecordCSVFormatter().writeCSV(new File(this.filePathDebugResults + "_short"), this.comparatorLogShort,
					this.headerDebugResultsShort);
			logger.info("Debug results written to file: " + this.filePathDebugResults + "_short");
			} catch (IOException e) {
				logger.error("Debug results could not be written to file: " + this.filePathDebugResults);
			}
		} else {
			logger.error("No debug results found!");
			logger.error("Is logging enabled?");
		}
	}

	/**
	 * Annotate debug results with goldStandard and write data matching debug
	 * results to file if logging was enabled via
	 * {@link #setCollectDebugResults(boolean) setCollectDebugResults}
	 * 
	 */
	protected void addGoldstandardToDebugResults(){
		if (this.comparatorLog != null && this.comparatorLogShort != null && debugGoldStandard != null) {
			for(Pair<String, String> pair: this.debugGoldStandard.getPositiveExamples()){
				String identifier = pair.getFirst()+ "-" + pair.getSecond();
				Record debug = this.comparatorLog.getRecord(identifier);
				if(debug != null){
					debug.setValue(ATTRIBUTE_IS_MATCH, "1");
				}
			}
			
			for(Pair<String, String> pair: this.debugGoldStandard.getNegativeExamples()){
				String identifier = pair.getFirst()+ "-" + pair.getSecond();
				Record debug = this.comparatorLog.getRecord(identifier);
				if(debug != null){
					debug.setValue(ATTRIBUTE_IS_MATCH, "0");
				}
			}
		}
	}

	@Override
	public ComparatorLogger getComparisonLog() {
		return this.comparisonLog;
	}

	@Override
	public void setComparisonLog(ComparatorLogger comparatorLog) {
		this.comparisonLog = comparatorLog;
	}
}
