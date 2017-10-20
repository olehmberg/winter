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
package de.uni_mannheim.informatik.dws.winter.matching.blockers;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceSimilarity;

/**
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>			the type of records which are the input for the blocking operation
 * @param <SchemaElementType>	the type of schema elements that are used in the schema of RecordType 
 * @param <BlockedType>			the type of record which is actually blocked
 */
public class InstanceBasedBlockingKeyIndexer<RecordType extends Matchable, SchemaElementType extends Matchable, BlockedType extends Matchable> 
	extends BlockingKeyIndexer<RecordType, SchemaElementType, BlockedType, MatchableValue>
{

	public InstanceBasedBlockingKeyIndexer(
			BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> blockingFunction,
			BlockingKeyGenerator<RecordType, MatchableValue, BlockedType> secondBlockingFunction,
			VectorSpaceSimilarity similarityFunction,
			de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer.VectorCreationMethod vectorCreationMethod, 
			double similarityThreshold) {
		super(blockingFunction, secondBlockingFunction, similarityFunction, vectorCreationMethod, similarityThreshold);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer#createCausalCorrespondences(de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer.BlockingVector, de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer.BlockingVector)
	 */
	@Override
	protected Processable<Correspondence<MatchableValue, Matchable>> createCausalCorrespondences(BlockedType record1,
			BlockedType record2,
			BlockingKeyIndexer<RecordType, SchemaElementType, BlockedType, MatchableValue>.BlockingVector vector1,
			BlockingKeyIndexer<RecordType, SchemaElementType, BlockedType, MatchableValue>.BlockingVector vector2) {

		Processable<Correspondence<MatchableValue, Matchable>> causes = new ProcessableCollection<>();
		
		for(String s : vector1.keySet()) {
			Double v1 = vector1.get(s);
			Double v2 = vector2.get(s);
			
			if(v2!=null) {
				MatchableValue v = new MatchableValue(s, "", "");
				Correspondence<MatchableValue, Matchable> cor = new Correspondence<>(v, v, getSimilarityFunction().calculateDimensionScore(v1, v2));
				causes.add(cor);
			}
		}
		
		return causes;
		
	}

}
