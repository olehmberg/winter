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
package de.uni_mannheim.informatik.dws.winter.clustering;

import java.util.Collection;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.Triple;

/**
 * 
 * Super class for graph-based clustering algorithms
 *
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class GraphBasedClusteringAlgorithm<T> {

	/**
	 * Applies the clustering algorithm and returns a map of cluster -&gt; centroid, if no centroid is available it returns a map of cluster -&gt; null 
	 * 
	 * @param similarityGraph	the similarity graph that should be clustered
	 * @return 	the clustering
	 */
	public abstract Map<Collection<T>, T> cluster(
			Collection<Triple<T, T, Double>> similarityGraph);
}
