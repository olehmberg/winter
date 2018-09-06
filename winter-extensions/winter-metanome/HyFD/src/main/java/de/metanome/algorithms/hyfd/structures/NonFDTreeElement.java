package de.metanome.algorithms.hyfd.structures;

import java.util.ArrayList;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithms.hyfd.utils.ValueComparator;

public class NonFDTreeElement {

	protected NonFDTreeElement[] children;
	protected boolean end = false;
	
	public NonFDTreeElement(int numAttributes) {
		this.children = new NonFDTreeElement[numAttributes];
	}

	public boolean addMatches(int[] t1, int[] t2, ValueComparator valueComparator, int attribute, boolean newNonFD) {
		do {
			attribute++;
			if (attribute == t1.length) {
				this.end = true;
				return newNonFD;
			}
		}
		while (valueComparator.isDifferent(t1[attribute], t2[attribute]));
		
		if (this.children[attribute] == null) {
			this.children[attribute] = new NonFDTreeElement(this.children.length);
			newNonFD = true;
		}
		
		return this.children[attribute].addMatches(t1, t2, valueComparator, attribute, newNonFD);
	}

	public void asBitSets(ArrayList<OpenBitSet> bitsets, OpenBitSet bitset, int thisAttribute) {
		bitset.set(thisAttribute);
		
		if (this.end)
			bitsets.add(bitset.clone());
		
		for (int i = thisAttribute; i < this.children.length; i++)
			if (this.children[i] != null)
				this.children[i].asBitSets(bitsets, bitset, i);
		
		bitset.clear(thisAttribute);
	}
	
	
}
