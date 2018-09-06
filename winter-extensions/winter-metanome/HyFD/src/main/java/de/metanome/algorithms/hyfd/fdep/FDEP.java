package de.metanome.algorithms.hyfd.fdep;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.hyfd.structures.FDTree;
import de.metanome.algorithms.hyfd.structures.FDTreeElement;
import de.metanome.algorithms.hyfd.utils.ValueComparator;

public class FDEP {
	
	protected int numAttributes;
	protected ValueComparator valueComparator;
	
	public FDEP(int numAttributes, ValueComparator valueComparator) {
		this.numAttributes = numAttributes;
		this.valueComparator = valueComparator;
	}

	public FDTree execute(int[][] records) throws AlgorithmExecutionException {
		FDTree negCoverTree = this.calculateNegativeCover(records);
		//negCoverTree.filterGeneralizations(); // TODO: (= remove all generalizations) Not necessary for correctness because calculating the positive cover does the filtering automatically if there are generalizations in the negCover, but for maybe for performance (?)
		records = null;
		
		//long t = System.currentTimeMillis();
		FDTree posCoverTree = this.calculatePositiveCover(negCoverTree);
		negCoverTree = null;
		//Logger.getInstance().writeln("t = " + (System.currentTimeMillis() - t));
		
		//posCoverTree.filterDeadElements();
		
		return posCoverTree;
	}
	
	protected FDTree calculateNegativeCover(int[][] records) {
		FDTree negCoverTree = new FDTree(this.numAttributes, -1);
		for (int i = 0; i < records.length; i++)
			for (int j = i + 1; j < records.length; j++)
				this.addViolatedFdsToCover(records[i], records[j], negCoverTree);
		return negCoverTree;
	}
	
	/**
	 * Find the least general functional dependencies violated by t1 and t2 and update the negative cover accordingly.
	 * Note: t1 and t2 must have the same length.
	 */
	protected void addViolatedFdsToCover(int[] t1, int[] t2, FDTree negCoverTree) {
		OpenBitSet equalAttrs = new OpenBitSet(t1.length);
		for (int i = 0; i < t1.length; i++)
			if (this.valueComparator.isEqual(t1[i], t2[i]))
				equalAttrs.set(i);
		
		OpenBitSet diffAttrs = new OpenBitSet(t1.length);
		diffAttrs.set(0, this.numAttributes);
		diffAttrs.andNot(equalAttrs);
		
		negCoverTree.addFunctionalDependency(equalAttrs, diffAttrs);
	}

	protected FDTree calculatePositiveCover(FDTree negCoverTree) {
		FDTree posCoverTree = new FDTree(this.numAttributes, -1);
		posCoverTree.addMostGeneralDependencies();
		OpenBitSet activePath = new OpenBitSet();
		
		this.calculatePositiveCover(posCoverTree, negCoverTree, activePath);
		
		return posCoverTree;
	}
	
	protected void calculatePositiveCover(FDTree posCoverTree, FDTreeElement negCoverSubtree, OpenBitSet activePath) {
		OpenBitSet fds = negCoverSubtree.getFds();
		for (int rhs = fds.nextSetBit(0); rhs >= 0; rhs = fds.nextSetBit(rhs + 1))
			this.specializePositiveCover(posCoverTree, activePath, rhs);
		
		if (negCoverSubtree.getChildren() != null) {
			for (int attr = 0; attr < this.numAttributes; attr++) {
				if (negCoverSubtree.getChildren()[attr] != null) {
					activePath.set(attr);
					this.calculatePositiveCover(posCoverTree, negCoverSubtree.getChildren()[attr], activePath);
					activePath.clear(attr);
				}
			}
		}
	}

	protected void specializePositiveCover(FDTree posCoverTree, OpenBitSet lhs, int rhs) {
		List<OpenBitSet> specLhss = null;
		specLhss = posCoverTree.getFdAndGeneralizations(lhs, rhs);
		for (OpenBitSet specLhs : specLhss) {
			posCoverTree.removeFunctionalDependency(specLhs, rhs);
			for (int attr = this.numAttributes - 1; attr >= 0; attr--) {
				if (!lhs.get(attr) && (attr != rhs)) {
					specLhs.set(attr);
					if (!posCoverTree.containsFdOrGeneralization(specLhs, rhs))
						posCoverTree.addFunctionalDependency(specLhs, rhs);
					specLhs.clear(attr);
				}
			}
		}
	}
	
	public Set<OpenBitSet> bitSets(FDTree negCoverTree) {
		Set<OpenBitSet> posCoverTree = new HashSet<>(this.numAttributes);
		OpenBitSet activePath = new OpenBitSet();
		
		this.fetchBitSets(posCoverTree, negCoverTree, activePath);
		
		return posCoverTree;
	}

	protected void fetchBitSets(Set<OpenBitSet> posCoverTree, FDTreeElement negCoverSubtree, OpenBitSet activePath) {
		posCoverTree.add(activePath.clone());
		
		if (negCoverSubtree.getChildren() != null) {
			for (int attr = 0; attr < this.numAttributes; attr++) {
				if (negCoverSubtree.getChildren()[attr] != null) {
					activePath.set(attr);
					this.fetchBitSets(posCoverTree, negCoverSubtree.getChildren()[attr], activePath);
					activePath.clear(attr);
				}
			}
		}
	}
	
}
