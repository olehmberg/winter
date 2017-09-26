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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.clustering.CentreClusterer;
import de.uni_mannheim.informatik.dws.winter.model.Triple;
import junit.framework.TestCase;

public class CentreClustererTest extends TestCase {

	public void testCluster() {

		ArrayList<Triple<String, String, Double>> similarityGraph = new ArrayList<Triple<String, String, Double>>();
		similarityGraph.add(new Triple<String, String, Double>("hello",
				"hello1", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hello",
				"hello2", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hello", "hi",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello", "hi1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello", "hi2",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello1",
				"hello2", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hello1", "hi",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello1", "hi1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello1", "hi2",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello2", "hi",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello2", "hi1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello2", "hi2",
				0.1));
		similarityGraph
				.add(new Triple<String, String, Double>("hi", "hi1", 0.9));
		similarityGraph
				.add(new Triple<String, String, Double>("hi", "hi2", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hi1", "hi2",
				0.9));

		CentreClusterer<String> cc = new CentreClusterer<String>();
		Map<Collection<String>,String> cluster = cc.cluster(similarityGraph);
		assertEquals(2, cluster.size());
	}
	
	public void testCluster2() {

		ArrayList<Triple<String, String, Double>> similarityGraph = new ArrayList<Triple<String, String, Double>>();
		similarityGraph.add(new Triple<String, String, Double>("hello",
				"hello1", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hello",
				"hello2", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hello", "hi",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello", "hi1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello", "hi2",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello1",
				"hello2", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hello1", "hi",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello1", "hi1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello1", "hi2",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello2", "hi",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello2", "hi1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello2", "hi2",
				0.1));
		similarityGraph
				.add(new Triple<String, String, Double>("hi", "hi1", 0.9));
		similarityGraph
				.add(new Triple<String, String, Double>("hi", "hi2", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hi1", "hi2",
				0.9));
		
		similarityGraph.add(new Triple<String, String, Double>("hello1",
				"hello", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hello2",
				"hello", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hi", "hello",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hi1", "hello",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hi2", "hello",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hello2",
				"hello1", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hi", "hello1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hi1", "hello1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hi2", "hello1",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hi", "hello2",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hi1", "hello2",
				0.1));
		similarityGraph.add(new Triple<String, String, Double>("hi2", "hello2",
				0.1));
		similarityGraph
				.add(new Triple<String, String, Double>("hi1", "hi", 0.9));
		similarityGraph
				.add(new Triple<String, String, Double>("hi2", "hi", 0.9));
		similarityGraph.add(new Triple<String, String, Double>("hi2", "hi1",
				0.9));

		CentreClusterer<String> cc = new CentreClusterer<String>();
		Map<Collection<String>,String> cluster = cc.cluster(similarityGraph);
		assertEquals(2, cluster.size());
	}

}
