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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import de.uni_mannheim.informatik.dws.winter.clustering.HierarchicalClusterer.LinkageMode;
import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class HierarchicalClustererTest {

	@Test
	public void testCluster() {
        Collection<Triple<String, String, Double>> data = new LinkedList<>();
                data.add(new Triple<>("a", "b", 0.2));
                data.add(new Triple<>("a", "c", 1.0));
                data.add(new Triple<>("b", "d", 0.8));

                HierarchicalClusterer<String> clusterer = new HierarchicalClusterer<>(LinkageMode.Min, 2);
                Map<Collection<String>, String> clustering = clusterer.cluster(data);

                Set<String> clu1 = Q.toSet("a", "c");
                Set<String> clu2 = Q.toSet("b", "d");

                assertTrue(clustering.keySet().contains(clu1));
                assertTrue(clustering.keySet().contains(clu2));
                assertEquals(2, clustering.keySet().size());
                assertTrue(1.0==-clusterer.getIntraClusterDistance(clu1).doubleValue());
                assertTrue(0.8==-clusterer.getIntraClusterDistance(clu2).doubleValue());

                clusterer = new HierarchicalClusterer<>(LinkageMode.Min, 0.5);
                clustering = clusterer.cluster(data);
                assertTrue(clustering.keySet().contains(clu1));
                assertTrue(clustering.keySet().contains(clu2));
                assertEquals(2, clustering.keySet().size());
                assertTrue(1.0==-clusterer.getIntraClusterDistance(clu1).doubleValue());
                assertTrue(0.8==-clusterer.getIntraClusterDistance(clu2).doubleValue());

                data = new LinkedList<>();
                data.add(new Triple<>("a", "b", 0.6));
                data.add(new Triple<>("b", "c", 0.6));
                data.add(new Triple<>("c", "d", 0.6));
                data.add(new Triple<>("a", "c", 0.2));
                data.add(new Triple<>("a", "d", 0.0));
                data.add(new Triple<>("b", "d", 0.2));
                
                clusterer = new HierarchicalClusterer<>(LinkageMode.Max, 2);
                clustering = clusterer.cluster(data);

                clu1 = Q.toSet("a", "b");
                clu2 = Q.toSet("c", "d");

                assertTrue(clustering.keySet().contains(clu1));
                assertTrue(clustering.keySet().contains(clu2));
                assertEquals(2, clustering.keySet().size());
                assertTrue(0.6==-clusterer.getIntraClusterDistance(clu1).doubleValue());
                assertTrue(0.6==-clusterer.getIntraClusterDistance(clu2).doubleValue());
                
                clusterer = new HierarchicalClusterer<>(LinkageMode.Max, 0.5);
                clustering = clusterer.cluster(data);
                assertTrue(clustering.keySet().contains(clu1));
                assertTrue(clustering.keySet().contains(clu2));
                assertEquals(2, clustering.keySet().size());
                assertTrue(0.6==-clusterer.getIntraClusterDistance(clu1).doubleValue());
                assertTrue(0.6==-clusterer.getIntraClusterDistance(clu2).doubleValue());
	}

}
