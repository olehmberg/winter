package de.metanome.algorithms.hyfd.structures;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

public class LhsTrie extends LhsTrieElement {

	protected int numAttributes;
	
	public LhsTrie(int numAttributes) {
		this.numAttributes = numAttributes;
	}

	public LhsTrieElement addLhs(OpenBitSet lhs) {
		LhsTrieElement currentNode = this;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			if (currentNode.getChildren()[i] != null)
				currentNode.setChild(this.numAttributes, i, new LhsTrieElement());
			currentNode = currentNode.getChildren()[i];
		}
		return currentNode;
	}

	public void removeLhs(OpenBitSet lhs) {
		LhsTrieElement[] path = new LhsTrieElement[(int)lhs.cardinality()];
		int currentPathIndex = 0;
		
		LhsTrieElement currentNode = this;
		path[currentPathIndex] = currentNode;
		currentPathIndex++;
		
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			currentNode = currentNode.getChildren()[i];
			path[currentPathIndex] = currentNode;
			currentPathIndex++;
		}
		
		for (int i = path.length - 1; i >= 0; i --) {
			path[i].removeChild(i);
			if (path[i].getChildren() != null)
				break;
		}
	}
	
	public List<OpenBitSet> getLhsAndGeneralizations(OpenBitSet lhs) {
		List<OpenBitSet> foundLhs = new ArrayList<>();
		OpenBitSet currentLhs = new OpenBitSet();
		int nextLhsAttr = lhs.nextSetBit(0);
		this.getLhsAndGeneralizations(lhs, nextLhsAttr, currentLhs, foundLhs);
		return foundLhs;
	}

	public boolean containsLhsOrGeneralization(OpenBitSet lhs) {
		int nextLhsAttr = lhs.nextSetBit(0);
		return this.containsLhsOrGeneralization(lhs, nextLhsAttr);
	}

	public List<OpenBitSet> asBitSetList() {
		List<OpenBitSet> foundLhs = new ArrayList<>();
		OpenBitSet currentLhs = new OpenBitSet();
		int nextLhsAttr = 0;
		this.asBitSetList(currentLhs, nextLhsAttr, foundLhs);
		return foundLhs;
	}
}
