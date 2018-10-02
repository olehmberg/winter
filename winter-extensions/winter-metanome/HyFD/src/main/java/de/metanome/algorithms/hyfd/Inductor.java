package de.metanome.algorithms.hyfd;

import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithms.hyfd.structures.FDList;
import de.metanome.algorithms.hyfd.structures.FDSet;
import de.metanome.algorithms.hyfd.structures.FDTree;
import de.metanome.algorithms.hyfd.utils.Logger;

public class Inductor {

	private FDSet negCover;
	private FDTree posCover;
	private MemoryGuardian memoryGuardian;

	public Inductor(FDSet negCover, FDTree posCover, MemoryGuardian memoryGuardian) {
		this.negCover = negCover;
		this.posCover = posCover;
		this.memoryGuardian = memoryGuardian;
	}

	public void updatePositiveCover(FDList nonFds) {
/*		if (nonFds.isEmpty())
			return;
		
		// Sort the negative cover
		Logger.getInstance().writeln("Sorting FD-violations ...");
		Collections.sort(nonFds, new Comparator<OpenBitSet>() {
			@Override
			public int compare(OpenBitSet o1, OpenBitSet o2) {
				return (int)(o1.cardinality() - o2.cardinality());
			}
		});
*/		// THE SORTING IS NOT NEEDED AS THE UCCSet SORTS THE NONUCCS BY LEVEL ALREADY
		
		Logger.getInstance().writeln("Inducing FD candidates ...");
		for (int i = nonFds.getFdLevels().size() - 1; i >= 0; i--) {
			if (i >= nonFds.getFdLevels().size()) // If this level has been trimmed during iteration
				continue;
			
			List<OpenBitSet> nonFdLevel = nonFds.getFdLevels().get(i);
			for (OpenBitSet lhs : nonFdLevel) {
				
				OpenBitSet fullRhs = lhs.clone();
				fullRhs.flip(0, this.posCover.getNumAttributes());
				
				for (int rhs = fullRhs.nextSetBit(0); rhs >= 0; rhs = fullRhs.nextSetBit(rhs + 1))
					this.specializePositiveCover(lhs, rhs, nonFds);
			}
			nonFdLevel.clear();
		}
	}
	
	protected int specializePositiveCover(OpenBitSet lhs, int rhs, FDList nonFds) {
		int numAttributes = this.posCover.getChildren().length;
		int newFDs = 0;
		List<OpenBitSet> specLhss;
		
		if (!(specLhss = this.posCover.getFdAndGeneralizations(lhs, rhs)).isEmpty()) { // TODO: May be "while" instead of "if"?
			for (OpenBitSet specLhs : specLhss) {
				this.posCover.removeFunctionalDependency(specLhs, rhs);
				
				if ((this.posCover.getMaxDepth() > 0) && (specLhs.cardinality() >= this.posCover.getMaxDepth()))
					continue;
				
				for (int attr = numAttributes - 1; attr >= 0; attr--) { // TODO: Is iterating backwards a good or bad idea?
					if (!lhs.get(attr) && (attr != rhs)) {
						specLhs.set(attr);
						if (!this.posCover.containsFdOrGeneralization(specLhs, rhs)) {
							this.posCover.addFunctionalDependency(specLhs, rhs);
							newFDs++;
							
							// If dynamic memory management is enabled, frequently check the memory consumption and trim the positive cover if it does not fit anymore
							this.memoryGuardian.memoryChanged(1);
							this.memoryGuardian.match(this.negCover, this.posCover, nonFds);
						}
						specLhs.clear(attr);
					}
				}
			}
		}
		return newFDs;
	}
}
