package de.metanome.algorithms.hyfd.structures;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

public class FDList {

	private List<List<OpenBitSet>> fdLevels;
	
	private int depth = 0;
	private int maxDepth;
	
	public FDList(int numAttributes, int maxDepth) {
		this.maxDepth = maxDepth;
		this.fdLevels = new ArrayList<List<OpenBitSet>>(numAttributes);
		for (int i = 0; i <= numAttributes; i++)
			this.fdLevels.add(new ArrayList<OpenBitSet>());
	}

	public List<List<OpenBitSet>> getFdLevels() {
		return this.fdLevels;
	}

	public int getDepth() {
		return this.depth;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public boolean add(OpenBitSet fd) {
		int length = (int) fd.cardinality();
		
		if ((this.maxDepth > 0) && (length > this.maxDepth))
			return false;
		
		this.depth = Math.max(this.depth, length);
		return this.fdLevels.get(length).add(fd);
	}

	public void trim(int newDepth) {
		while (this.fdLevels.size() > (newDepth + 1)) // +1 because uccLevels contains level 0
			this.fdLevels.remove(this.fdLevels.size() - 1);
		
		this.depth = newDepth;
		this.maxDepth = newDepth;
	}

	public void clear() {
		int numLevels = this.fdLevels.size();
		this.fdLevels = new ArrayList<List<OpenBitSet>>(numLevels);
		for (int i = 0; i <= numLevels; i++)
			this.fdLevels.add(new ArrayList<OpenBitSet>());
	}

	public int size() {
		int size = 0;
		for (List<OpenBitSet> uccs : this.fdLevels)
			size += uccs.size();
		return size;
	}
}
