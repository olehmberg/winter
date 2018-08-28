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
package de.uni_mannheim.informatik.dws.winter.matching.algorithms;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.Blocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.MatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * 
 * Implementation of rule-based matching. Applicable for identity resolution and schema matching using matching rules.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RuleBasedMatchingAlgorithm<RecordType extends Matchable, SchemaElementType extends Matchable, CorrespondenceType extends Matchable> implements MatchingAlgorithm<RecordType, CorrespondenceType> {

	private DataSet<RecordType, SchemaElementType> dataset1;
	private DataSet<RecordType, SchemaElementType> dataset2;
	private Processable<Correspondence<CorrespondenceType, Matchable>> correspondences;
	private MatchingRule<RecordType, CorrespondenceType> rule;
	private Blocker<RecordType, SchemaElementType, RecordType, CorrespondenceType> blocker;
	private Processable<Correspondence<RecordType, CorrespondenceType>> result;
	private String taskName = "Matching";
	private static final Logger logger = WinterLogManager.getLogger();
	
	/**
	 * @param dataset1
	 * 				the first dataset
	 * @param dataset2
	 * 				the second dataset
	 * @param correspondences
	 * 				correspondences between the two datasets
	 * @param rule
	 * 				the matching rule
	 * @param blocker
	 * 				the blocker
	 */
	public RuleBasedMatchingAlgorithm(DataSet<RecordType, SchemaElementType> dataset1,
			DataSet<RecordType, SchemaElementType> dataset2,
			Processable<Correspondence<CorrespondenceType, Matchable>> correspondences,
			MatchingRule<RecordType, CorrespondenceType> rule, 
			Blocker<RecordType, SchemaElementType, RecordType, CorrespondenceType> blocker) {
		super();
		this.dataset1 = dataset1;
		this.dataset2 = dataset2;
		this.correspondences = correspondences;
		this.rule = rule;
		this.blocker = blocker;
	}
	public DataSet<RecordType, SchemaElementType> getDataset1() {
		return dataset1;
	}
	public DataSet<RecordType, SchemaElementType> getDataset2() {
		return dataset2;
	}
	public Processable<Correspondence<CorrespondenceType, Matchable>> getCorrespondences() {
		return correspondences;
	}
	public MatchingRule<RecordType, CorrespondenceType> getRule() {
		return rule;
	}
	public Blocker<RecordType, SchemaElementType, RecordType, CorrespondenceType> getBlocker() {
		return blocker;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	@Override
	public Processable<Correspondence<RecordType, CorrespondenceType>> getResult() {
		return result;
	}
	
	public Processable<Correspondence<RecordType, CorrespondenceType>> runBlocking(DataSet<RecordType, SchemaElementType> dataset1, DataSet<RecordType, SchemaElementType> dataset2, Processable<Correspondence<CorrespondenceType, Matchable>> correspondences) {
		return blocker.runBlocking(getDataset1(), getDataset2(), getCorrespondences());
	}
	
	public double getReductionRatio() {
		return getBlocker().getReductionRatio();
	}
	
	public void run() {
		LocalDateTime start = LocalDateTime.now();

		logger.info(String.format("Starting %s", getTaskName()));

		logger.info(String.format("Blocking %,d x %,d elements", getDataset1().size(), getDataset2().size()));
		
		// use the blocker to generate pairs
		Processable<Correspondence<RecordType, CorrespondenceType>> allPairs = runBlocking(getDataset1(), getDataset2(), getCorrespondences());
		
		logger.info(String
						.format("Matching %,d x %,d elements; %,d blocked pairs (reduction ratio: %s)",
								getDataset1().size(), getDataset2().size(),
								allPairs.size(), Double.toString(getReductionRatio())));
		
		if(blocker.isMeasureBlockSizes()){
			blocker.writeDebugBlockingResultsToFile();
		}
		
		// compare the pairs using the matching rule
		Processable<Correspondence<RecordType, CorrespondenceType>> result = allPairs.map(rule);
		
		// report total matching time
		LocalDateTime end = LocalDateTime.now();
		
		logger.info(String.format(
				"%s finished after %s; found %,d correspondences.",
				getTaskName(), DurationFormatUtils.formatDurationHMS(Duration.between(start, end).toMillis()), result.size()));
		
		if(rule.isCollectDebugResults()){
			rule.writeDebugMatchingResultsToFile();
		}
		
		this.result = result;
	}
}
