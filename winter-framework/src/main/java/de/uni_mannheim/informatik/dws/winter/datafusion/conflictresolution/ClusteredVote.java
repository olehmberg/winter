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
package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.clustering.CentreClusterer;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * Clustered Vote {@link ConflictResolutionFunction}: Clusters all values and returns the centroid of the largest cluster
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <ValueType>	the type of the values that are fused
 * @param <RecordType>	the type that represents a record
 */
public class ClusteredVote<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> {

	private SimilarityMeasure<ValueType> similarityMeasure;
	private double threshold;
	
	public ClusteredVote(SimilarityMeasure<ValueType> similarityMeasure, double threshold) {
		this.similarityMeasure = similarityMeasure;
		this.threshold = threshold;
	}
	
	@Override
	public FusedValue<ValueType, RecordType, SchemaElementType> resolveConflict(
			Collection<FusibleValue<ValueType, RecordType, SchemaElementType>> values) {
		
		// calculate similarities
		Collection<Triple<FusibleValue<ValueType, RecordType, SchemaElementType>, FusibleValue<ValueType, RecordType, SchemaElementType>, Double>> similarityGraph = new LinkedList<>();
		ArrayList<FusibleValue<ValueType, RecordType, SchemaElementType>> valueList = new ArrayList<>(values);
		for(int i = 0; i < valueList.size(); i++) {
			FusibleValue<ValueType, RecordType, SchemaElementType> v1 = valueList.get(i);
			for(int j = i + 1; j <valueList.size(); j++) {
				FusibleValue<ValueType, RecordType, SchemaElementType> v2 = valueList.get(j);
				
				double similarity = similarityMeasure.calculate(v1.getValue(), v2.getValue());
				
				if(similarity>=threshold) {
					similarityGraph.add(new Triple<>(v1, v2, similarity));
				}
			}
		}
		
		// run clustering
		CentreClusterer<FusibleValue<ValueType, RecordType, SchemaElementType>> clusterer = new CentreClusterer<>();
		Map<Collection<FusibleValue<ValueType, RecordType, SchemaElementType>>,FusibleValue<ValueType, RecordType, SchemaElementType>> clusters = clusterer.cluster(similarityGraph);
		
		// select largest cluster
		FusibleValue<ValueType, RecordType, SchemaElementType> centroid = null;
		Collection<FusibleValue<ValueType, RecordType, SchemaElementType>> largestCluster = null;
		for(Collection<FusibleValue<ValueType, RecordType, SchemaElementType>> clu : clusters.keySet()) {
			FusibleValue<ValueType, RecordType, SchemaElementType> centre = clusters.get(clu);
			if(largestCluster==null || clu.size()>largestCluster.size()) {
				largestCluster = clu;
				centroid = centre;
			}
		}
		
		return new FusedValue<>(centroid);
	}

}
