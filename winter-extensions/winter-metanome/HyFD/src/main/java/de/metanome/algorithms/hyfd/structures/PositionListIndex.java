/*
 * Copyright 2014 by the Metanome project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.metanome.algorithms.hyfd.structures;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.uni_potsdam.hpi.utils.CollectionUtils;

/**
 * Position list indices (or stripped partitions) are an index structure that
 * stores the positions of equal values in a nested list. A column with the
 * values a, a, b, c, b, c transfers to the position list index ((0, 1), (2, 4),
 * (3, 5)). Clusters of size 1 are discarded. A position list index should be
 * created using the {@link PLIBuilder}.
 */
public class PositionListIndex {

	protected final int attribute;
	protected final List<IntArrayList> clusters;
	protected final int numNonUniqueValues;
	
	public int getAttribute() {
		return this.attribute;
	}
	
	public List<IntArrayList> getClusters() {
		return this.clusters;
	}

	public int getNumNonUniqueValues() {
		return this.numNonUniqueValues;
	}
	
	public PositionListIndex(int attribute, List<IntArrayList> clusters) {
		this.attribute = attribute;
		this.clusters = clusters;
		this.numNonUniqueValues = this.countNonUniqueValuesIn(clusters);
	}
	
	protected int countNonUniqueValuesIn(List<IntArrayList> clusters) {
		int numNonUniqueValues = 0;
		for (IntArrayList cluster : clusters)
			numNonUniqueValues += cluster.size();
		return numNonUniqueValues;
	}

	/**
	 * Returns the number of non unary clusters.
	 *
	 * @return the number of clusters in the {@link PositionListIndex}
	 */
	public long size() {
		return this.clusters.size();
	}

	/**
	 * @return the column represented by the {@link PositionListIndex} is unique.
	 */
	public boolean isUnique() {
		return this.size() == 0;
	}
	
	public boolean isConstant(int numRecords) {
		if (numRecords <= 1)
			return true;
		if ((this.clusters.size() == 1) && (this.clusters.get(0).size() == numRecords))
			return true;
		return false;
	}

/*	public PositionListIndex intersect(PositionListIndex otherPLI) {
		Int2IntOpenHashMap hashedPLI = otherPLI.asHashMap();		
		Int2ObjectMap<Int2ObjectMap<IntArrayList>> intersectMap = this.buildIntersectMap(this, hashedPLI);
		
		List<IntArrayList> clusters = new ArrayList<>();
		for (Int2ObjectMap<IntArrayList> cluster1 : intersectMap.values())
			for (IntArrayList cluster2 : cluster1.values())
				if (cluster2.size() > 1)
					clusters.add(cluster2);
		
		return new PositionListIndex(clusters);
	}
*/
	
/*	public Int2IntOpenHashMap asHashMap() {
		Int2IntOpenHashMap hashedPLI = new Int2IntOpenHashMap(this.clusters.size());		
		int clusterId = 0;
		for (IntArrayList cluster : this.clusters) {
			for (int recordId : cluster)
				hashedPLI.put(recordId, clusterId);
			
			clusterId++;
		}
		return hashedPLI;
	}
	
	protected Int2ObjectMap<Int2ObjectMap<IntArrayList>> buildIntersectMap(PositionListIndex testPLI, Int2IntOpenHashMap hashedPLI) {
		Int2ObjectMap<Int2ObjectMap<IntArrayList>> intersectMap = new Int2ObjectOpenHashMap<>();
		for (int cluster1Id = 0; cluster1Id < testPLI.clusters.size(); cluster1Id++) {
			IntArrayList cluster = testPLI.clusters.get(cluster1Id);
			for (int recordId : cluster) {
				if (hashedPLI.containsKey(recordId)) {
					int cluster2Id = hashedPLI.get(recordId);
					
					Int2ObjectMap<IntArrayList> cluster1 = intersectMap.get(cluster1Id);
					if (cluster1 == null) {
						cluster1 = new Int2ObjectOpenHashMap<IntArrayList>();
						intersectMap.put(cluster1Id, cluster1);
					}
						
					IntArrayList cluster2 = cluster1.get(cluster2Id);
					if (cluster2 == null) {
						cluster2 = new IntArrayList();
						cluster1.put(cluster2Id, cluster2);
					}
					
					cluster2.add(recordId);
				}
			}
		}
		return intersectMap;
	}
*/	
	
	public PositionListIndex intersect(int[]... plis) {
		List<IntArrayList> clusters = new ArrayList<>();
		for (IntArrayList pivotCluster : this.clusters) {
			HashMap<IntArrayList, IntArrayList> clustersMap = new HashMap<IntArrayList, IntArrayList>(pivotCluster.size());
			
			for (int recordId : pivotCluster) {
				IntArrayList subClusters = new IntArrayList(plis.length);
				
				boolean isUnique = false;
				for (int i = 0; i < plis.length; i++) {
					if (plis[i][recordId] == -1) {
						isUnique = true;
						break;
					}	
					subClusters.add(plis[i][recordId]);
				}
				if (isUnique)
					continue;
				
				if (!clustersMap.containsKey(subClusters))
					clustersMap.put(subClusters, new IntArrayList());
				
				clustersMap.get(subClusters).add(recordId);
			}
			
			for (IntArrayList cluster : clustersMap.values())
				if (cluster.size() > 1)
					clusters.add(cluster);
		}
		return new PositionListIndex(-1, clusters);
	}
	
	public PositionListIndex intersect(int[] otherPLI) {
		Int2ObjectMap<Int2ObjectMap<IntArrayList>> intersectMap = this.buildIntersectMap(otherPLI);
		
		List<IntArrayList> clusters = new ArrayList<>();
		for (Int2ObjectMap<IntArrayList> cluster1 : intersectMap.values())
			for (IntArrayList cluster2 : cluster1.values())
				if (cluster2.size() > 1)
					clusters.add(cluster2);
		
		return new PositionListIndex(-1, clusters);
	}

	protected Int2ObjectMap<Int2ObjectMap<IntArrayList>> buildIntersectMap(int[] hashedPLI) {
		Int2ObjectMap<Int2ObjectMap<IntArrayList>> intersectMap = new Int2ObjectOpenHashMap<>();
		for (int cluster1Id = 0; cluster1Id < this.clusters.size(); cluster1Id++) {
			IntArrayList cluster = this.clusters.get(cluster1Id);
			for (int recordId : cluster) {
				if (hashedPLI[recordId] >= 0) {
					int cluster2Id = hashedPLI[recordId];
					
					Int2ObjectMap<IntArrayList> cluster1 = intersectMap.get(cluster1Id);
					if (cluster1 == null) {
						cluster1 = new Int2ObjectOpenHashMap<IntArrayList>();
						intersectMap.put(cluster1Id, cluster1);
					}
						
					IntArrayList cluster2 = cluster1.get(cluster2Id);
					if (cluster2 == null) {
						cluster2 = new IntArrayList();
						cluster1.put(cluster2Id, cluster2);
					}
					
					cluster2.add(recordId);
				}
			}
		}
		return intersectMap;
	}

/*	public long getRawKeyError() {
		if (this.rawKeyError == -1) {
			this.rawKeyError = this.calculateRawKeyError();
		}

		return this.rawKeyError;
	}

	protected long calculateRawKeyError() {
		long sumClusterSize = 0;

		for (LongArrayList cluster : this.clusters) {
			sumClusterSize += cluster.size();
		}

		return sumClusterSize - this.clusters.size();
	}
*/
/*	public boolean refines(PositionListIndex otherPLI) {
		Int2IntOpenHashMap hashedPLI = otherPLI.asHashMap();
		
		for (IntArrayList cluster : this.clusters) {
			int otherClusterId = hashedPLI.get(cluster.getInt(0));
			
			for (int recordId : cluster)
				if ((!hashedPLI.containsKey(recordId)) || (hashedPLI.get(recordId) != otherClusterId))
					return false;
		}
		
		return true;
	}
*/	
	public boolean refines(int[][] compressedRecords, int rhsAttr) {
		for (IntArrayList cluster : this.clusters)
			if (!this.probe(compressedRecords, rhsAttr, cluster))
				return false;
		return true;
	}
	
	protected boolean probe(int[][] compressedRecords, int rhsAttr, IntArrayList cluster) {
		int rhsClusterId = compressedRecords[cluster.getInt(0)][rhsAttr];
		
		// If otherClusterId < 0, then this cluster must point into more than one other clusters
		if (rhsClusterId == -1)
			return false;
		
		// Check if all records of this cluster point into the same other cluster
		for (int recordId : cluster)
			if (compressedRecords[recordId][rhsAttr] != rhsClusterId)
				return false;
		
		return true;
	}
	
	public boolean refines(int[] rhsInvertedPli) {
		for (IntArrayList cluster : this.clusters)
			if (!this.probe(rhsInvertedPli, cluster))
				return false;
		return true;
	}

	protected boolean probe(int[] rhsInvertedPli, IntArrayList cluster) {
		int rhsClusterId = rhsInvertedPli[cluster.getInt(0)];
		
		// If otherClusterId < 0, then this cluster must point into more than one other clusters
		if (rhsClusterId == -1)
			return false;
		
		// Check if all records of this cluster point into the same other cluster
		for (int recordId : cluster)
			if (rhsInvertedPli[recordId] != rhsClusterId)
				return false;
		
		return true;
	}
	
/*	public OpenBitSet refines(int[][] invertedPlis, OpenBitSet lhs, OpenBitSet rhs) {
		// Returns the rhs attributes that are refined by the lhs
		OpenBitSet refinedRhs = rhs.clone();
		
		// TODO: Check if it is technically possible that this fd holds, i.e., if A1 has 2 clusters of size 10 and A2 has 2 clusters of size 10, then the intersection can have at most 4 clusters of size 5 (see join cardinality estimation)
		
		OpenBitSet invalidRhs = new OpenBitSet(rhs.cardinality());
		for (IntArrayList cluster : this.clusters) {
			// Build the intersection of lhs attribute clusters
			Object2ObjectOpenHashMap<IntArrayList, IntArrayList> subClusters = new Object2ObjectOpenHashMap<>(cluster.size());
			for (int recordId : cluster) {
				IntArrayList subClusterIdentifier = this.buildClusterIdentifier(recordId, invertedPlis, lhs);
				if (subClusterIdentifier == null)
					continue;
				
				if (!subClusters.containsKey(subClusterIdentifier))
					subClusters.put(subClusterIdentifier, new IntArrayList());
				subClusters.get(subClusterIdentifier).add(recordId);
			}
			
			// Probe the rhs attributes against the lhs intersection
			for (int rhsAttr = refinedRhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = refinedRhs.nextSetBit(rhsAttr + 1)) {	// Put the rhs loop on the top level, because we usually have only one rhs attribute
				for (IntArrayList subCluster : subClusters.values()) {
					if (subCluster.size() == 1) // TODO: remove the clusters of size 1 before these loops?
						continue;
					
					if (!this.probe(invertedPlis[rhsAttr], subCluster)) {
						invalidRhs.set(rhsAttr);
						break;
					}
				}
			}
			refinedRhs.andNot(invalidRhs);
			if (refinedRhs.isEmpty())
				break;
		}
		return refinedRhs;
	}
*/
	public OpenBitSet refines(int[][] compressedRecords, OpenBitSet lhs, OpenBitSet rhs, List<IntegerPair> comparisonSuggestions) {
		int rhsSize = (int) rhs.cardinality();
		int lhsSize = (int) lhs.cardinality();
		
		// Returns the rhs attributes that are refined by the lhs
		OpenBitSet refinedRhs = rhs.clone();
		
		// TODO: Check if it is technically possible that this fd holds, i.e., if A1 has 2 clusters of size 10 and A2 has 2 clusters of size 10, then the intersection can have at most 4 clusters of size 5 (see join cardinality estimation)
		
		int[] rhsAttrId2Index = new int[compressedRecords[0].length];
		int[] rhsAttrIndex2Id = new int[rhsSize];
		int index = 0;
		for (int rhsAttr = refinedRhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = refinedRhs.nextSetBit(rhsAttr + 1)) {
			rhsAttrId2Index[rhsAttr] = index;
			rhsAttrIndex2Id[index] = rhsAttr;
			index++;
		}
		
		for (IntArrayList cluster : this.clusters) {
			Object2ObjectOpenHashMap<ClusterIdentifier, ClusterIdentifierWithRecord> subClusters = new Object2ObjectOpenHashMap<>(cluster.size());
			for (int recordId : cluster) {
				ClusterIdentifier subClusterIdentifier = this.buildClusterIdentifier(lhs, lhsSize, compressedRecords[recordId]);
				if (subClusterIdentifier == null)
					continue;
				
				if (subClusters.containsKey(subClusterIdentifier)) {
					ClusterIdentifierWithRecord rhsClusters = subClusters.get(subClusterIdentifier);
					
					for (int rhsAttr = refinedRhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = refinedRhs.nextSetBit(rhsAttr + 1)) {
						int rhsCluster = compressedRecords[recordId][rhsAttr];
						if ((rhsCluster == -1) || (rhsCluster != rhsClusters.get(rhsAttrId2Index[rhsAttr]))) {
							comparisonSuggestions.add(new IntegerPair(recordId, rhsClusters.getRecord()));
							
							refinedRhs.clear(rhsAttr);
							if (refinedRhs.isEmpty())
								return refinedRhs;
						}
					}
				}
				else {
					int[] rhsClusters = new int[rhsSize];
					for (int rhsAttr = 0; rhsAttr < rhsSize; rhsAttr++)
						rhsClusters[rhsAttr] = compressedRecords[recordId][rhsAttrIndex2Id[rhsAttr]];
					subClusters.put(subClusterIdentifier, new ClusterIdentifierWithRecord(rhsClusters, recordId));
				}
			}
		}
		return refinedRhs;
	}
	
	public OpenBitSet refines(int[][] invertedPlis, OpenBitSet lhs, OpenBitSet rhs, int numAttributes, ArrayList<IntegerPair> comparisonSuggestions) {
		int rhsSize = (int) rhs.cardinality();
		int lhsSize = (int) lhs.cardinality();
		
		// Returns the rhs attributes that are refined by the lhs
		OpenBitSet refinedRhs = rhs.clone();
		
		// TODO: Check if it is technically possible that this fd holds, i.e., if A1 has 2 clusters of size 10 and A2 has 2 clusters of size 10, then the intersection can have at most 4 clusters of size 5 (see join cardinality estimation)
		
		int[] rhsAttrId2Index = new int[numAttributes];
		int[] rhsAttrIndex2Id = new int[rhsSize];
		int index = 0;
		for (int rhsAttr = refinedRhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = refinedRhs.nextSetBit(rhsAttr + 1)) {
			rhsAttrId2Index[rhsAttr] = index;
			rhsAttrIndex2Id[index] = rhsAttr;
			index++;
		}
		
		for (IntArrayList cluster : this.clusters) {
			Object2ObjectOpenHashMap<ClusterIdentifier, ClusterIdentifierWithRecord> subClusters = new Object2ObjectOpenHashMap<>(cluster.size());
			for (int recordId : cluster) {
				ClusterIdentifier subClusterIdentifier = this.buildClusterIdentifier(recordId, invertedPlis, lhs, lhsSize);
				if (subClusterIdentifier == null)
					continue;
				
				if (subClusters.containsKey(subClusterIdentifier)) {
					ClusterIdentifierWithRecord rhsClusters = subClusters.get(subClusterIdentifier);
					
					for (int rhsAttr = refinedRhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = refinedRhs.nextSetBit(rhsAttr + 1)) {
						int rhsCluster = invertedPlis[rhsAttr][recordId];
						if ((rhsCluster == -1) || (rhsCluster != rhsClusters.get(rhsAttrId2Index[rhsAttr]))) {
							comparisonSuggestions.add(new IntegerPair(recordId, rhsClusters.getRecord()));
							
							refinedRhs.clear(rhsAttr);
							if (refinedRhs.isEmpty())
								return refinedRhs;
						}
					}
				}
				else {
					int[] rhsClusters = new int[rhsSize];
					for (int rhsAttr = 0; rhsAttr < rhsSize; rhsAttr++)
						rhsClusters[rhsAttr] = invertedPlis[rhsAttrIndex2Id[rhsAttr]][recordId];
					subClusters.put(subClusterIdentifier, new ClusterIdentifierWithRecord(rhsClusters, recordId));
				}
			}
		}
		return refinedRhs;
	}

	public boolean refines(int[][] compressedRecords, OpenBitSet lhs, int[] rhs) {
		for (IntArrayList cluster : this.clusters) {
			ClusterTree clusterTree = new ClusterTree();
			
			// Check if all subclusters of this cluster point into the same other clusters
			for (int recordId : cluster)
				if (!clusterTree.add(compressedRecords, lhs, recordId, rhs[recordId]))
					return false;
		}
		return true;
	}

	public boolean refines(int[][] lhsInvertedPlis, int[] rhs) {
		for (IntArrayList cluster : this.clusters) {
			Object2IntOpenHashMap<IntArrayList> clustersMap = new Object2IntOpenHashMap<>(cluster.size());
			
			// Check if all subclusters of this cluster point into the same other clusters
			for (int recordId : cluster) {
				IntArrayList additionalLhsCluster = this.buildClusterIdentifier(recordId, lhsInvertedPlis);
				if (additionalLhsCluster == null)
					continue;
				
				if (clustersMap.containsKey(additionalLhsCluster)) {
					if ((rhs[recordId] == -1) || (clustersMap.getInt(additionalLhsCluster) != rhs[recordId]))
						return false;
				}
				else {
					clustersMap.put(additionalLhsCluster, rhs[recordId]);
				}
			}
		}
		return true;
	}

	protected ClusterIdentifier buildClusterIdentifier(OpenBitSet lhs, int lhsSize, int[] record) { 
		int[] cluster = new int[lhsSize];
		
		int index = 0;
		for (int lhsAttr = lhs.nextSetBit(0); lhsAttr >= 0; lhsAttr = lhs.nextSetBit(lhsAttr + 1)) {
			int clusterId = record[lhsAttr];
			
			if (clusterId < 0)
				return null;
			
			cluster[index] = clusterId;
			index++;
		}
		
		return new ClusterIdentifier(cluster);
	}

	protected ClusterIdentifier buildClusterIdentifier(int recordId, int[][] invertedPlis, OpenBitSet lhs, int lhsSize) { 
		int[] cluster = new int[lhsSize];
		
		int index = 0;
		for (int lhsAttr = lhs.nextSetBit(0); lhsAttr >= 0; lhsAttr = lhs.nextSetBit(lhsAttr + 1)) {
			int clusterId = invertedPlis[lhsAttr][recordId];
			
			if (clusterId < 0)
				return null;
			
			cluster[index] = clusterId;
			index++;
		}
		
		return new ClusterIdentifier(cluster);
	}

	protected IntArrayList buildClusterIdentifier(int recordId, int[][] lhsInvertedPlis) { 
		IntArrayList clusterIdentifier = new IntArrayList(lhsInvertedPlis.length);
		
		for (int attributeIndex = 0; attributeIndex < lhsInvertedPlis.length; attributeIndex++) {
			int clusterId = lhsInvertedPlis[attributeIndex][recordId];
			
			if (clusterId < 0)
				return null;
			
			clusterIdentifier.add(clusterId);
		}
		return clusterIdentifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		List<IntOpenHashSet> setCluster = this.convertClustersToSets(this.clusters);

		Collections.sort(setCluster, new Comparator<IntSet>() {
			@Override
			public int compare(IntSet o1, IntSet o2) {
				return o1.hashCode() - o2.hashCode();
			}
		});
		result = prime * result + (setCluster.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		PositionListIndex other = (PositionListIndex) obj;
		if (this.clusters == null) {
			if (other.clusters != null) {
				return false;
			}
		} else {
			List<IntOpenHashSet> setCluster = this.convertClustersToSets(this.clusters);
			List<IntOpenHashSet> otherSetCluster = this.convertClustersToSets(other.clusters);

			for (IntOpenHashSet cluster : setCluster) {
				if (!otherSetCluster.contains(cluster)) {
					return false;
				}
			}
			for (IntOpenHashSet cluster : otherSetCluster) {
				if (!setCluster.contains(cluster)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{ ");
		for (IntArrayList cluster : this.clusters) {
			builder.append("{");
			builder.append(CollectionUtils.concat(cluster, ","));
			builder.append("} ");
		}
		builder.append("}");
		return builder.toString();
	}

	protected List<IntOpenHashSet> convertClustersToSets(List<IntArrayList> listCluster) {
		List<IntOpenHashSet> setClusters = new LinkedList<>();
		for (IntArrayList cluster : listCluster) {
			setClusters.add(new IntOpenHashSet(cluster));
		}

		return setClusters;
	}

}
