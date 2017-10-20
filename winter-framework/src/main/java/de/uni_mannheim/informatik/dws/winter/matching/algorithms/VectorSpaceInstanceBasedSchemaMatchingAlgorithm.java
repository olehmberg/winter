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

import de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer.VectorCreationMethod;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.InstanceBasedBlockingKeyIndexer;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceSimilarity;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class VectorSpaceInstanceBasedSchemaMatchingAlgorithm<RecordType extends Matchable, SchemaElementType extends Matchable> implements MatchingAlgorithm<SchemaElementType, MatchableValue> {

	private DataSet<RecordType, SchemaElementType> dataset1;
	private DataSet<RecordType, SchemaElementType> dataset2;
	private BlockingKeyGenerator<RecordType, MatchableValue, SchemaElementType> blockingfunction1;
	private BlockingKeyGenerator<RecordType, MatchableValue, SchemaElementType> blockingfunction2;
	private VectorCreationMethod vectorCreation;
	private VectorSpaceSimilarity similarity;
	private double similarityThreshold;
	private Processable<Correspondence<SchemaElementType, MatchableValue>> result;
	
	public VectorSpaceInstanceBasedSchemaMatchingAlgorithm(
			DataSet<RecordType, SchemaElementType> dataset1, 
			DataSet<RecordType, SchemaElementType> dataset2,
			BlockingKeyGenerator<RecordType, MatchableValue, SchemaElementType> blockingfunction1,
			BlockingKeyGenerator<RecordType, MatchableValue, SchemaElementType> blockingfunction2,
			VectorCreationMethod vectorCreation,
			VectorSpaceSimilarity similarity, 
			double similarityThreshold) {
		this.dataset1 = dataset1;
		this.dataset2 = dataset2;
		this.blockingfunction1 = blockingfunction1;
		this.blockingfunction2 = blockingfunction2;
		this.vectorCreation = vectorCreation;
		this.similarity = similarity;
		this.similarityThreshold = similarityThreshold;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.algorithms.MatchingAlgorithm#run()
	 */
	@Override
	public void run() {
		
		InstanceBasedBlockingKeyIndexer<RecordType, SchemaElementType, SchemaElementType> blocker = new InstanceBasedBlockingKeyIndexer<>(blockingfunction1, blockingfunction2, similarity, vectorCreation, similarityThreshold);
		
		result = blocker.runBlocking(dataset1, dataset2, null);
		
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.algorithms.MatchingAlgorithm#getResult()
	 */
	@Override
	public Processable<Correspondence<SchemaElementType, MatchableValue>> getResult() {
		return result;
	}

}
