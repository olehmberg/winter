package de.metanome.algorithms.hyfd.structures;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import org.apache.lucene.util.OpenBitSet;

public class ClusterTree {

	protected Int2ObjectOpenHashMap<ClusterTreeElement> children = new Int2ObjectOpenHashMap<ClusterTreeElement>();
	
	public boolean add(int[][] compressedRecords, OpenBitSet lhs, int recordId, int content) {
		int firstLhsAttr = lhs.nextSetBit(0);
		int firstCluster = compressedRecords[recordId][firstLhsAttr];
		if (firstCluster < 0)
			return true;
		
		ClusterTreeElement child = this.children.get(firstCluster);
		if (child == null) {
			child = new ClusterTreeElement(compressedRecords, lhs, lhs.nextSetBit(firstLhsAttr + 1), recordId, content);
			this.children.put(firstCluster, child);
			return true;
		}
		
		return child.add(compressedRecords, lhs, lhs.nextSetBit(firstLhsAttr + 1), recordId, content);
	}
}
