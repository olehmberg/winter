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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * 
 * Hierarchical Clustering.
 * 
 * 
 * Implementation adapted from https://elki-project.github.io/tutorial/hierarchical_clustering
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class HierarchicalClusterer<T> extends GraphBasedClusteringAlgorithm<T> {

    public enum LinkageMode {
        Min,
        Max,
        Avg
    }

    private LinkageMode linkage;

    private double[][] sim;
    private double[] height;
    private int size;
    private List<T> objects;
    Map<T, Integer> objectToIndex;
    private int[] parent;
    private Map<Integer, Set<T>> clusters;

    private Integer numClusters;
    private Double minSimilarity;

    private Map<Set<T>, Double> intraClusterDistance;

    public Double getIntraClusterDistance(Collection<T> cluster) {
        return intraClusterDistance.get(cluster);
    }

    public HierarchicalClusterer(LinkageMode linkage, int numClusters) {
        this.linkage = linkage;
        this.numClusters = numClusters;
    }

    public HierarchicalClusterer(LinkageMode linkage, double minSimilarity) {
        this.linkage = linkage;
        this.minSimilarity = minSimilarity;
    }

    @Override
    public Map<Collection<T>, T> cluster(Collection<Triple<T, T, Double>> similarityGraph) {
        initialise(similarityGraph);

        if(numClusters!=null) {
            for(int i = 0; i < (size - numClusters); i++) {
                step();
            }
        } else {
            while(step()) ;
        }

        return createClusters();
    }

    private void initialise(Collection<Triple<T, T, Double>> similarityGraph) {
        objectToIndex = new HashMap<>();
        objects = new LinkedList<>();
        int idx = 0;
        for(Triple<T, T, Double> t : similarityGraph) {
            if(!objectToIndex.containsKey(t.getFirst())) {
                objectToIndex.put(t.getFirst(),idx++);
                objects.add(t.getFirst());
            }
            if(!objectToIndex.containsKey(t.getSecond())) {
                objectToIndex.put(t.getSecond(), idx++);
                objects.add(t.getSecond());
            }
        }

        this.objects = new ArrayList<>(objects);
        this.clusters = new HashMap<>();

        size = objectToIndex.size();
        sim = new double[size][size];
        height = new double[size];

        for(Triple<T, T, Double> t : similarityGraph) {
            int i = objectToIndex.get(t.getFirst());
            int j = objectToIndex.get(t.getSecond());

            // translate similarities into distances
            sim[i][j] = -t.getThird();
            sim[j][i] = -t.getThird();
        }

        Arrays.fill(height, Double.POSITIVE_INFINITY);

        parent = new int[size];
        for(int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    private boolean step() {

        // find edge to merge
        double min = Double.POSITIVE_INFINITY;
        int minx = -1, miny = -1;
        for (int x = 0; x < size; x++) {
          if (height[x] < Double.POSITIVE_INFINITY) {
            continue;
          }
          for (int y = 0; y < x; y++) {
            if (height[y] < Double.POSITIVE_INFINITY) {
              continue;
            }
            if (sim[x][y] < min) {
              min = sim[x][y];
              minx = x;
              miny = y;
            }
          }
        }

        if(minSimilarity==null || -min >= minSimilarity) {
            height[minx] = min;
            parent[minx] = miny;

            // merge clusters
            Set<T> cx = clusters.get(minx);
            Set<T> cy = clusters.get(miny);
            if(cy==null) {
                cy = new HashSet<>();
                cy.add(objects.get(miny));
            }
            if(cx==null) {
                cy.add(objects.get(minx));
            } else {
                cy.addAll(cx);
                clusters.remove(minx);
            }
            clusters.put(miny, cy);
            
            // update similarity matrix
            for(int i = 0; i < size; i++) {
                switch(linkage) {
                    case Min:
                        sim[i][miny] = Math.min(sim[i][minx], sim[i][miny]);
                        sim[i][minx] = Math.min(sim[minx][i], sim[miny][i]);
                        break;
                    case Max:
                        sim[i][miny] = Math.max(sim[i][minx], sim[i][miny]);
                        sim[i][minx] = Math.max(sim[minx][i], sim[miny][i]);
                        break;
                    case Avg:
                        return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public Map<Collection<T>, T> createClusters() {
        Map<Collection<T>, T> finalClusters = new HashMap<>();
        intraClusterDistance = new HashMap<>();

        for(int x = 0; x < size; x++) {
            if(height[x] < Double.POSITIVE_INFINITY) {
                continue;
            }

            Set<T> clu = clusters.get(x);
            if(clu==null) {
                clu = Q.toSet(objects.get(x));
            }

            finalClusters.put(clu, null);
            
            double maxIntraDistance = Double.NEGATIVE_INFINITY;
            for(T t : clu) {
                double h = height[objectToIndex.get(t)];
                if(h < Double.POSITIVE_INFINITY) {
                    maxIntraDistance = Math.max(h, maxIntraDistance);
                }
            }
            if(maxIntraDistance==Double.NEGATIVE_INFINITY) {
                maxIntraDistance = 0;
            }
            intraClusterDistance.put(clu, maxIntraDistance);
        }
        return finalClusters;
    }

}