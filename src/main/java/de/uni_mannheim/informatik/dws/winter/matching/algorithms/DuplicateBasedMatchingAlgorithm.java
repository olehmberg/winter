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

import de.uni_mannheim.informatik.dws.winter.matching.aggregators.CorrespondenceAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.aggregators.TopKVotesAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.Blocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.VotingMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * 
 * Implementation of duplicate-based matching: the values of the given duplicates are compared and vote for correspondences.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class DuplicateBasedMatchingAlgorithm<RecordType extends Matchable, SchemaElementType extends Matchable> implements MatchingAlgorithm<SchemaElementType, RecordType> {

	private DataSet<SchemaElementType, SchemaElementType> dataset1; 
	private DataSet<SchemaElementType, SchemaElementType> dataset2;
	private Processable<Correspondence<RecordType, Matchable>> correspondences;
	private VotingMatchingRule<SchemaElementType, RecordType> rule;
	private TopKVotesAggregator<SchemaElementType, RecordType> voteFilter;
	private CorrespondenceAggregator<SchemaElementType, RecordType> voting;
	private Blocker<SchemaElementType, SchemaElementType, SchemaElementType, RecordType> blocker;
	private Processable<Correspondence<SchemaElementType, RecordType>> result;

	/**
	 * 
	 * Constructor without filtering: all votes are used.
	 * 
	 * @param dataset1
	 * 		The first schema
	 * @param dataset2
	 * 		The second schema
	 * @param instanceCorrespondences
	 * 		The duplicates
	 * @param rule
	 * 		The voting rule
	 * @param voting
	 * 		The voting (=aggregation) method
	 * @param schemaBlocker
	 * 		The schema blocker
	 */
	public DuplicateBasedMatchingAlgorithm(DataSet<SchemaElementType, SchemaElementType> dataset1,
			DataSet<SchemaElementType, SchemaElementType> dataset2,
			Processable<Correspondence<RecordType, Matchable>> instanceCorrespondences,
			VotingMatchingRule<SchemaElementType, RecordType> rule,
			CorrespondenceAggregator<SchemaElementType, RecordType> voting,
			Blocker<SchemaElementType, SchemaElementType, SchemaElementType, RecordType> schemaBlocker) {
		super();
		this.dataset1 = dataset1;
		this.dataset2 = dataset2;
		this.correspondences = instanceCorrespondences;
		this.rule = rule;
		this.voting = voting;
		this.blocker = schemaBlocker;
	}
	
	/**
	 * 
	 * Constructor with filtering: a voteFilter is applied before aggregating the votes. For example, only the top 3 votes by similarity value get to vote.
	 * 
	 * @param dataset1
	 * 		The first schema
	 * @param dataset2
	 * 		The second schema
	 * @param instanceCorrespondences
	 * 		The duplicates
	 * @param rule
	 * 		The voting rule
	 * @param voteFilter
	 * 		The filter rule for votes
	 * @param voting
	 * 		The voting (=aggregation) methods
	 * @param schemaBlocker
	 * 		The schema blocker
	 */
	public DuplicateBasedMatchingAlgorithm(DataSet<SchemaElementType, SchemaElementType> dataset1,
			DataSet<SchemaElementType, SchemaElementType> dataset2,
			Processable<Correspondence<RecordType, Matchable>> instanceCorrespondences,
			VotingMatchingRule<SchemaElementType, RecordType> rule,
			TopKVotesAggregator<SchemaElementType, RecordType> voteFilter,
			CorrespondenceAggregator<SchemaElementType, RecordType> voting,
			Blocker<SchemaElementType, SchemaElementType, SchemaElementType, RecordType> schemaBlocker) {
		super();
		this.dataset1 = dataset1;
		this.dataset2 = dataset2;
		this.correspondences = instanceCorrespondences;
		this.rule = rule;
		this.voteFilter = voteFilter;
		this.voting = voting;
		this.blocker = schemaBlocker;
	}
	
	public DataSet<SchemaElementType, SchemaElementType> getDataset1() {
		return dataset1;
	}
	public DataSet<SchemaElementType, SchemaElementType> getDataset2() {
		return dataset2;
	}
	public Processable<Correspondence<RecordType, Matchable>> getCorrespondences() {
		return correspondences;
	}
	public VotingMatchingRule<SchemaElementType, RecordType> getRule() {
		return rule;
	}

	public Blocker<SchemaElementType, SchemaElementType, SchemaElementType, RecordType> getBlocker() {
		return blocker;
	}
	
	@Override
	public Processable<Correspondence<SchemaElementType, RecordType>> getResult() {
		return result;
	}
	
	public Processable<Correspondence<SchemaElementType, RecordType>> runBlocking(DataSet<SchemaElementType, SchemaElementType> dataset1, DataSet<SchemaElementType, SchemaElementType> dataset2, Processable<Correspondence<RecordType, Matchable>> correspondences) {
		return getBlocker().runBlocking(getDataset1(), getDataset2(), getCorrespondences());
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.algorithms.MatchingAlgorithm#run()
	 */
	@Override
	public void run() {
		LocalDateTime start = LocalDateTime.now();

		System.out.println(String.format("[%s] Starting Duplicate-based Schema Matching",
				start.toString()));

		System.out.println(String.format("Blocking %,d x %,d elements", getDataset1().size(), getDataset2().size()));
		
		// run the blocking
		Processable<Correspondence<SchemaElementType, RecordType>> blocked = runBlocking(getDataset1(), getDataset2(), Correspondence.toMatchable(getCorrespondences()));
		
		System.out
		.println(String
				.format("Matching %,d x %,d elements; %,d blocked pairs (reduction ratio: %s)",
						getDataset1().size(), getDataset2().size(),
						blocked.size(), Double.toString(getBlocker().getReductionRatio())));

		// compare the pairs using the matching rule, filter and aggregate the votes
		Processable<Pair<Pair<SchemaElementType, SchemaElementType>, Correspondence<SchemaElementType, RecordType>>> aggregatedVotes;
		if(voteFilter==null) {
			// vote and aggregate without filtering
			aggregatedVotes = blocked.aggregate(rule, voting);
		} else {
			// vote
			Processable<Correspondence<SchemaElementType, RecordType>> votes = blocked.map(rule);
			
			Processable<Pair<Pair<SchemaElementType, RecordType>, Processable<Correspondence<SchemaElementType, RecordType>>>> filteredVotes = votes.aggregate((r,c) -> 
			{
				Correspondence<RecordType, Matchable> cause = Q.firstOrDefault(r.getCausalCorrespondences().get());
				if(cause!=null) {
					c.next(new Pair<Pair<SchemaElementType, RecordType>, Correspondence<SchemaElementType, RecordType>>(
							new Pair<SchemaElementType, RecordType>(r.getFirstRecord(), cause.getFirstRecord()),
							r
							));
				}
			}, voteFilter);
			
			// aggregate the votes
			aggregatedVotes = filteredVotes.aggregate((r,c) -> {
				if(r!=null) {
					for(Correspondence<SchemaElementType, RecordType> cor : r.getSecond().get()) {
						c.next(new Pair<>(new Pair<>(cor.getFirstRecord(), cor.getSecondRecord()), cor));
					}
				}
			}, voting);
			
		}

		result = aggregatedVotes.map((p,c) -> {
			if(p.getSecond()!=null) {
				c.next(p.getSecond());
			}
		});
		
		// report total matching time
		LocalDateTime end = LocalDateTime.now();
		
		System.out.println(String.format(
				"[%s] Duplicate-based Schema Matching finished after %s; found %d correspondences from %,d duplicates.",
				end.toString(),
				DurationFormatUtils.formatDurationHMS(Duration.between(start, end).toMillis()), result.size(), getCorrespondences().size()));

	}

	
}
