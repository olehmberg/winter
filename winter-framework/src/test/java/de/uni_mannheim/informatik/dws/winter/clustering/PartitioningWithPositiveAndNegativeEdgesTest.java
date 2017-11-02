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

import org.apache.commons.lang.StringUtils;

import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class PartitioningWithPositiveAndNegativeEdgesTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.clustering.PartitioningWithPositiveAndNegativeEdges#createResult()}.
	 */
	public void testCreateResult() {
		
		PartitioningWithPositiveAndNegativeEdges<String> clusterer = new PartitioningWithPositiveAndNegativeEdges<>(0.0);
		clusterer.setLog(true);
		clusterer.addEdge(new Triple<String, String, Double>("1", "2", 0.67));
		clusterer.addEdge(new Triple<String, String, Double>("1", "3", 0.5));
		clusterer.addEdge(new Triple<String, String, Double>("1", "3", -0.5));
		clusterer.addEdge(new Triple<String, String, Double>("1", "4", -0.7));
		clusterer.addEdge(new Triple<String, String, Double>("2", "3", 0.33));
		clusterer.addEdge(new Triple<String, String, Double>("2", "3", -0.33));
		clusterer.addEdge(new Triple<String, String, Double>("2", "5", 0.67));
		clusterer.addEdge(new Triple<String, String, Double>("3", "4", 0.7));
		clusterer.addEdge(new Triple<String, String, Double>("3", "5", 0.8));
		clusterer.addEdge(new Triple<String, String, Double>("4", "5", 0.6));
		
		Map<Collection<String>, String> clustering = clusterer.createResult();
		
		for(Collection<String> cluster : clustering.keySet()) {
			System.out.println(StringUtils.join(cluster, ","));
		}
		
		assertTrue(Q.toSet(Q.toSet("1", "2"), Q.toSet("3", "4", "5")).equals(clustering.keySet()));
	}

}
