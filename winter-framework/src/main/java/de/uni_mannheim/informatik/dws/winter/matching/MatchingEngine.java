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
package de.uni_mannheim.informatik.dws.winter.matching;

import de.uni_mannheim.informatik.dws.winter.matching.aggregators.CorrespondenceAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.aggregators.TopKCorrespondencesAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.aggregators.TopKVotesAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.DuplicateBasedMatchingAlgorithm;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.MaximumBipartiteMatchingAlgorithm;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleBasedDuplicateDetectionAlgorithm;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleBasedMatchingAlgorithm;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.VectorSpaceIdentityResolutionAlgorithm;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.VectorSpaceInstanceBasedSchemaMatchingAlgorithm;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.AbstractBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.Blocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer.VectorCreationMethod;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.NoSchemaBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.SymmetricBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.AggregateByFirstRecordRule;
import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.FlattenAggregatedCorrespondencesRule;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.matching.rules.MatchingRule;
import de.uni_mannheim.informatik.dws.winter.matching.rules.VotingMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.processing.FlattenAggregationResultMapper;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceSimilarity;

/**
 * The matching engine provides access to the matching algorithms for schema matching and identity resolution.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type of records that are matched
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType
 */
public class MatchingEngine<RecordType extends Matchable, SchemaElementType extends Matchable>  {
	
	public MatchingEngine() {
	}
	
	/**
	 * Runs the Duplicate Detection on a given {@link DataSet}. In order to
	 * reduce the number of internally compared {@link AbstractRecord}s the functions
	 * can be executed in a <i>symmetric</i>-mode. Here it will be assumed, that
	 * that the {@link MatchingRule} is symmetric, meaning that score(a,b) =
	 * score(b,a). Therefore the pair (b,a) can be omitted. Normally, this
	 * option can be set to <b>true</b> in most of the cases, as most of the
	 * common similarity functions (e.g. {@link LevenshteinSimilarity}, and
	 * {@link TokenizingJaccardSimilarity}) are symmetric, meaning sim(a,b) =
	 * sim(b,a).
	 * 
	 * @param dataset
	 *            The data set
	 * @param rule
	 * 			  The {@link MatchingRule} that is used to compare the records.
	 * @param blocker
	 * 			  The blocker to use 
	 * @return A list of correspondences
	 */
	public Processable<Correspondence<RecordType, SchemaElementType>> runDuplicateDetection(
			DataSet<RecordType, SchemaElementType> dataset, 
			MatchingRule<RecordType, SchemaElementType> rule, 
			SymmetricBlocker<RecordType, SchemaElementType, RecordType, SchemaElementType> blocker) {

		RuleBasedDuplicateDetectionAlgorithm<RecordType, SchemaElementType> algorithm = new RuleBasedDuplicateDetectionAlgorithm<>(dataset, rule, blocker);
		algorithm.setTaskName("Duplicate Detection");
		
		algorithm.run();
		
		return algorithm.getResult();
	}
	
	/**
	 * Runs the Duplicate Detection on a given {@link DataSet} with additional schema correspondences. In order to
	 * reduce the number of internally compared {@link AbstractRecord}s the functions
	 * can be executed in a <i>symmetric</i>-mode. Here it will be assumed, that
	 * that the {@link MatchingRule} is symmetric, meaning that score(a,b) =
	 * score(b,a). Therefore the pair (b,a) can be omitted. Normally, this
	 * option can be set to <b>true</b> in most of the cases, as most of the
	 * common similarity functions (e.g. {@link LevenshteinSimilarity}, and
	 * {@link TokenizingJaccardSimilarity}) are symmetric, meaning sim(a,b) =
	 * sim(b,a).
	 * 
	 * @param dataset
	 *            The data set
	 * @param schemaCorrespondences
	 * 			  The schema correspondences            
	 * @param rule
	 * 			  The {@link MatchingRule} that is used to compare the records.
	 * @param blocker
	 * 			  The blocker to use 
	 * @return A list of correspondences
	 */
	public Processable<Correspondence<RecordType, SchemaElementType>> runDuplicateDetection(
			DataSet<RecordType, SchemaElementType> dataset,
			Processable<? extends Correspondence<SchemaElementType, ?>> schemaCorrespondences,
			MatchingRule<RecordType, SchemaElementType> rule, 
			SymmetricBlocker<RecordType, SchemaElementType, RecordType, SchemaElementType> blocker) {

		RuleBasedDuplicateDetectionAlgorithm<RecordType, SchemaElementType> algorithm = new RuleBasedDuplicateDetectionAlgorithm<>(dataset, Correspondence.toMatchable(schemaCorrespondences), rule, blocker);
		algorithm.setTaskName("Duplicate Detection");
		
		algorithm.run();
		
		return algorithm.getResult();
	}

	/**
	 * Runs identity resolution on the given data sets using the provided matching rule and blocker.
	 * 
	 * @param dataset1
	 *            The first data set
	 * @param dataset2
	 *            The second data set
	 * @param schemaCorrespondences
	 * 			 (Optional) Schema correspondences between the data sets that tell the matching rule which attribute combinations to compare.
	 * @param rule
	 * 			  The {@link MatchingRule} that is used to compare the records.
	 * @param blocker
	 * 			  The {@link AbstractBlocker} that generates the pairs, which are then checked by the {@link MatchingRule}.
	 * @return A list of correspondences
	 */
	public Processable<Correspondence<RecordType, SchemaElementType>> runIdentityResolution(
			DataSet<RecordType, SchemaElementType> dataset1, 
			DataSet<RecordType, SchemaElementType> dataset2, 
			Processable<? extends Correspondence<SchemaElementType, ?>> schemaCorrespondences,
			MatchingRule<RecordType, SchemaElementType> rule,
			Blocker<RecordType, SchemaElementType, RecordType, SchemaElementType> blocker) {

		RuleBasedMatchingAlgorithm<RecordType, SchemaElementType, SchemaElementType> algorithm = new RuleBasedMatchingAlgorithm<>(dataset1, dataset2, Correspondence.toMatchable(schemaCorrespondences), rule, blocker);
		algorithm.setTaskName("Identity Resolution");
		
		algorithm.run();
		
		return algorithm.getResult();
		
	}
	
	/**
	 * 
	 * Runs identity resolution on the provided datasets. The blocking functions generate the terms which are used to calculate similarity using the vector space model. 
	 * 
	 * @param dataset1
	 * 			the first dataset
	 * @param dataset2
	 * 			the second dataset
	 * @param blockingfunction1
	 * 			the blocking function for the first dataset
	 * @param blockingfunction2
	 * 			the blocking function for the second dataset
	 * @param vectorCreation
	 * 			the vector creation method
	 * @param similarity
	 * 			the similarity function
	 * @param similarityThreshold
	 * 			the similarity threshold
	 * @return
	 * 			the resulting correspondences
	 */
	public Processable<Correspondence<RecordType, MatchableValue>> runVectorBasedIdentityResolution(
			DataSet<RecordType, SchemaElementType> dataset1, 
			DataSet<RecordType, SchemaElementType> dataset2,
			BlockingKeyGenerator<RecordType, MatchableValue, RecordType> blockingfunction1,
			BlockingKeyGenerator<RecordType, MatchableValue, RecordType> blockingfunction2,
			VectorCreationMethod vectorCreation,
			VectorSpaceSimilarity similarity,
			double similarityThreshold) {

		VectorSpaceIdentityResolutionAlgorithm<RecordType, SchemaElementType> algorithm = new VectorSpaceIdentityResolutionAlgorithm<>(dataset1, dataset2, blockingfunction1, blockingfunction2, vectorCreation, similarity, similarityThreshold);
		
		algorithm.run();
		
		return algorithm.getResult();
		
	}

	/**
	 * Runs schema matchign on the given data sets using the provided matching rule and blocker.
	 * The data sets must contain the attribute features as records.
	 * 
	 * @param schema1
	 *            The first data set
	 * @param schema2
	 *            The second data set
	 * @param instanceCorrespondences
	 * 			 (Optional) Instance correspondences between the data sets that tell the matching rule which instance values to compare.
	 * @param rule
	 * 			  The {@link MatchingRule} that is used to compare the records.
	 * @param blocker
	 * 			  The {@link AbstractBlocker} that generates the pairs, which are then checked by the {@link MatchingRule}.
	 * @return A list of correspondences
	 */
	public Processable<Correspondence<SchemaElementType, RecordType>> runSchemaMatching(
			DataSet<SchemaElementType, SchemaElementType> schema1, 
			DataSet<SchemaElementType, SchemaElementType> schema2,
			Processable<? extends Correspondence<RecordType, ?>> instanceCorrespondences,
			MatchingRule<SchemaElementType, RecordType> rule,
			Blocker<SchemaElementType, SchemaElementType, SchemaElementType, RecordType> blocker) {

		RuleBasedMatchingAlgorithm<SchemaElementType, SchemaElementType, RecordType> algorithm = new RuleBasedMatchingAlgorithm<>(schema1, schema2, Correspondence.toMatchable(instanceCorrespondences), rule, blocker);
		algorithm.setTaskName("Schema Matching");
		
		algorithm.run();
		
		return algorithm.getResult();
		
	}
	
	/**
	 * 
	 * Runs label-based schema matching on the provided datasets using the specified comparator.
	 * 
	 * @param schema1
	 * 			The first schema
	 * @param schema2
	 * 			The second schema
	 * @param labelComparator
	 * 			The comparator that compares the labels of two attributes
	 * @param similarityThreshold
	 * 			The similarity threshold for creating correspondences
	 * @return
	 * 			The discovered schema correspondences
	 * @throws Exception
	 * 			From LinearCombinationMatchingRule
	 */
	public Processable<Correspondence<SchemaElementType, SchemaElementType>> runLabelBasedSchemaMatching(
			DataSet<SchemaElementType, SchemaElementType> schema1, 
			DataSet<SchemaElementType, SchemaElementType> schema2,
			Comparator<SchemaElementType, SchemaElementType> labelComparator,
			double similarityThreshold) throws Exception {

		Blocker<SchemaElementType, SchemaElementType, SchemaElementType, SchemaElementType> blocker = new NoSchemaBlocker<>();
		
		LinearCombinationMatchingRule<SchemaElementType, SchemaElementType> rule = new LinearCombinationMatchingRule<>(similarityThreshold);
		rule.addComparator(labelComparator, 1.0);
		
		RuleBasedMatchingAlgorithm<SchemaElementType, SchemaElementType, SchemaElementType> algorithm = new RuleBasedMatchingAlgorithm<>(schema1, schema2, null, rule, blocker);
		algorithm.setTaskName("Schema Matching");
		
		algorithm.run();
		
		return algorithm.getResult();
		
	}

	/**
	 * 
	 * Runs label-based schema matching on the provided dataset using the specified comparator.
	 * 
	 * @param schema
	 * 			The schema
	 * @param labelComparator
	 * 			The comparator that compares the labels of two attributes
	 * @param similarityThreshold
	 * 			The similarity threshold for creating correspondences
	 * @return
	 * 			The discovered schema correspondences
	 * @throws Exception
	 * 			From LinearCombinationMatchingRule
	 */
	public Processable<Correspondence<SchemaElementType, SchemaElementType>> runLabelBasedSchemaMatching(
			DataSet<SchemaElementType, SchemaElementType> schema, 
			Comparator<SchemaElementType, SchemaElementType> labelComparator,
			double similarityThreshold) throws Exception {

		SymmetricBlocker<SchemaElementType, SchemaElementType, SchemaElementType, SchemaElementType> blocker = new NoSchemaBlocker<>();
		
		LinearCombinationMatchingRule<SchemaElementType, SchemaElementType> rule = new LinearCombinationMatchingRule<>(similarityThreshold);
		rule.addComparator(labelComparator, 1.0);
		
		RuleBasedDuplicateDetectionAlgorithm<SchemaElementType, SchemaElementType> algorithm = new RuleBasedDuplicateDetectionAlgorithm<SchemaElementType, SchemaElementType>(schema, rule, blocker);

		algorithm.setTaskName("Schema Matching");
		
		algorithm.run();
		
		return algorithm.getResult();
	}
	
	/**
	 * 
	 * Runs instance-based schema matching on the provided datasets. The blocking functions generate the terms which are used to calculate similarity using the vector space model. 
	 * 
	 * @param dataset1
	 * 			the first dataset
	 * @param dataset2
	 * 			the second dataset
	 * @param blockingfunction1
	 * 			the blocking function for the first dataset
	 * @param blockingfunction2
	 * 			the blocking function for the second dataset
	 * @param vectorCreation
	 * 			the vector creation method
	 * @param similarity
	 * 			the similarity function
	 * @param similarityThreshold
	 * 			the similarity threshold
	 * @return
	 * 			the resulting correspondences
	 */
	public Processable<Correspondence<SchemaElementType, MatchableValue>> runInstanceBasedSchemaMatching(
			DataSet<RecordType, SchemaElementType> dataset1, 
			DataSet<RecordType, SchemaElementType> dataset2,
			BlockingKeyGenerator<RecordType, MatchableValue, SchemaElementType> blockingfunction1,
			BlockingKeyGenerator<RecordType, MatchableValue, SchemaElementType> blockingfunction2,
			VectorCreationMethod vectorCreation,
			VectorSpaceSimilarity similarity,
			double similarityThreshold) {

		VectorSpaceInstanceBasedSchemaMatchingAlgorithm<RecordType, SchemaElementType> algorithm = new VectorSpaceInstanceBasedSchemaMatchingAlgorithm<>(dataset1, dataset2, blockingfunction1, blockingfunction2, vectorCreation, similarity, similarityThreshold);
		
		algorithm.run();
		
		return algorithm.getResult();
		
	}
	
	/**
	 * 
	 * Runs duplicate-based schema matching between the provided schemas.
	 * First, the {@link VotingMatchingRule} rule is evaluated for all provided instance correspondences to create votes.
	 * Optionally, these votes can be filtered by the voteFilter, i.e. to limit the number of votes that each value can cast.
	 * Then, the {@link CorrespondenceAggregator} voteAggregator combines the votes into final correspondences. 
	 * 
	 * @param schema1
	 * 			The first schema
	 * @param schema2
	 * 			The second schema
	 * @param instanceCorrespondences
	 * 			The instance correspondences (=duplicates)
	 * @param rule
	 * 			The rule to cast votes from instance correspondences
	 * @param voteFilter
	 * 			The filtering rule for votes (optional)
	 * @param voteAggregator
	 * 			The aggregation from votes to final correspondences
	 * @param schemaBlocker
	 * 			The blocker that creates potential pairs of attributes. Use {@link NoSchemaBlocker} is all combinations should be considered.
	 * @return The found correspondences
	 */
	public Processable<Correspondence<SchemaElementType, RecordType>> runDuplicateBasedSchemaMatching(
			DataSet<SchemaElementType, SchemaElementType> schema1, 
			DataSet<SchemaElementType, SchemaElementType> schema2,
			Processable<? extends Correspondence<RecordType, ?>> instanceCorrespondences,
			VotingMatchingRule<SchemaElementType, RecordType> rule,
			TopKVotesAggregator<SchemaElementType, RecordType> voteFilter,
			CorrespondenceAggregator<SchemaElementType, RecordType> voteAggregator,
			Blocker<SchemaElementType, SchemaElementType, SchemaElementType, RecordType> schemaBlocker) {
		
		DuplicateBasedMatchingAlgorithm<RecordType, SchemaElementType> algorithm = new DuplicateBasedMatchingAlgorithm<>(schema1, schema2, Correspondence.toMatchable(instanceCorrespondences), rule, voteFilter, voteAggregator, schemaBlocker);

		algorithm.run();
		
		return algorithm.getResult();
	}
	
	/**
	 * 
	 * Returns the k correspondences for each record on the left-hand side with the highest similarity score
	 * 
	 * @param correspondences
	 * 			The correspondences that should be filtered
	 * @param k
	 * 			The desired number of correspondences for each record on the left-hand side
	 * @param similarityThreshold
	 * 			The minimum similarity for a correspondence to appear in the result
	 * @return The top k correspondences
	 */
	public Processable<Correspondence<RecordType, SchemaElementType>> getTopKInstanceCorrespondences(
			Processable<Correspondence<RecordType, SchemaElementType>> correspondences,
			int k,
			double similarityThreshold) {
		TopKCorrespondencesAggregator<RecordType, SchemaElementType> aggregator = new TopKCorrespondencesAggregator<>(k);
		
		return correspondences
				.aggregate(new AggregateByFirstRecordRule<>(similarityThreshold), aggregator)
				.map(new FlattenAggregatedCorrespondencesRule<>());
	}
	
	/**
	 * 
	 * Returns the k correspondences for each attribute on the left-hand side with the highest similarity score
	 * 
	 * @param correspondences
	 * 			The correspondences that should be filtered
	 * @param k
	 * 			The desired number of correspondences for each attribute on the left-hand side
	 * @param similarityThreshold
	 * 			The minimum similarity for a correspondence to appear in the result
	 * @return The top k correspondences
	 */	
	public Processable<Correspondence<SchemaElementType, RecordType>> getTopKSchemaCorrespondences(
			Processable<Correspondence<SchemaElementType, RecordType>> correspondences,
			int k,
			double similarityThreshold) {
		TopKCorrespondencesAggregator<SchemaElementType, RecordType> aggregator = new TopKCorrespondencesAggregator<>(k);
		
		return correspondences
				.aggregate(new AggregateByFirstRecordRule<>(similarityThreshold), aggregator)
				.map(new FlattenAggregationResultMapper<>());
	}

	public Processable<Correspondence<RecordType, SchemaElementType>> getMaximumWeightGlobalInstanceMatching(
			Processable<Correspondence<RecordType, SchemaElementType>> correspondences) {
		
		MaximumBipartiteMatchingAlgorithm<RecordType, SchemaElementType> maximumMatching = new MaximumBipartiteMatchingAlgorithm<>(correspondences);
		
		maximumMatching.run();
		
		return maximumMatching.getResult();
		
	}
	
	public Processable<Correspondence<SchemaElementType, RecordType>> getMaximumWeightGlobalSchemaMatching(
			Processable<Correspondence<SchemaElementType, RecordType>> correspondences) {
		
		MaximumBipartiteMatchingAlgorithm<SchemaElementType, RecordType> maximumMatching = new MaximumBipartiteMatchingAlgorithm<>(correspondences);
		
		maximumMatching.run();
		
		return maximumMatching.getResult();
		
	}
}
