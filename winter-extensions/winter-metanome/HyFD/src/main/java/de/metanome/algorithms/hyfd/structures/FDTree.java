package de.metanome.algorithms.hyfd.structures;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.uni_potsdam.hpi.utils.FileUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FDTree extends FDTreeElement {

	private int depth = 0;
	private int maxDepth;
	
	public FDTree(int numAttributes, int maxDepth) {
		super(numAttributes);
		this.maxDepth = maxDepth;
		this.children = new FDTreeElement[numAttributes];
	}

	public int getDepth() {
		return this.depth;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	@Override
	public String toString() {
		return "[" + this.depth + " depth, " + this.maxDepth + " maxDepth]";
	}

	public void trim(int newDepth) {
		this.trimRecursive(0, newDepth);
		this.depth = newDepth;
		this.maxDepth = newDepth;
	}

	public void addMostGeneralDependencies() {
		this.rhsAttributes.set(0, this.numAttributes);
		this.rhsFds.set(0, this.numAttributes);
	}

	public FDTreeElement addFunctionalDependency(OpenBitSet lhs, int rhs) {
		FDTreeElement currentNode = this;
		currentNode.addRhsAttribute(rhs);

		int lhsLength = 0;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			lhsLength++;
			
			if (currentNode.getChildren() == null) {
				currentNode.setChildren(new FDTreeElement[this.numAttributes]);
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
			}
			else if (currentNode.getChildren()[i] == null) {
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
			}
				
			currentNode = currentNode.getChildren()[i];
			currentNode.addRhsAttribute(rhs);
		}
		currentNode.markFd(rhs);
		
		this.depth = Math.max(this.depth, lhsLength);
		return currentNode;
	}

	public FDTreeElement addFunctionalDependency(OpenBitSet lhs, OpenBitSet rhs) {
		FDTreeElement currentNode = this;
		currentNode.addRhsAttributes(rhs);

		int lhsLength = 0;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			lhsLength++;
			
			if (currentNode.getChildren() == null) {
				currentNode.setChildren(new FDTreeElement[this.numAttributes]);
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
			}
			else if (currentNode.getChildren()[i] == null) {
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
			}
				
			currentNode = currentNode.getChildren()[i];
			currentNode.addRhsAttributes(rhs);
		}
		currentNode.markFds(rhs);
		
		this.depth = Math.max(this.depth, lhsLength);
		return currentNode;
	}

	public FDTreeElement addFunctionalDependencyGetIfNew(OpenBitSet lhs, int rhs) {
		FDTreeElement currentNode = this;
		currentNode.addRhsAttribute(rhs);

		boolean isNew = false;
		int lhsLength = 0;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			lhsLength++;
			
			if (currentNode.getChildren() == null) {
				currentNode.setChildren(new FDTreeElement[this.numAttributes]);
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
				isNew = true;
			}
			else if (currentNode.getChildren()[i] == null) {
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
				isNew = true;
			}
				
			currentNode = currentNode.getChildren()[i];
			currentNode.addRhsAttribute(rhs);
		}
		currentNode.markFd(rhs);

		this.depth = Math.max(this.depth, lhsLength);
		if (isNew)
			return currentNode;
		return null;
	}

	public FDTreeElement addFunctionalDependencyIfNotInvalid(OpenBitSet lhs, OpenBitSet rhs) {
		FDTreeElement currentNode = this;
		currentNode.addRhsAttributes(rhs);

		OpenBitSet invalidFds = currentNode.rhsAttributes.clone();
		int lhsLength = 0;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			lhsLength++;
			
			if (currentNode.getChildren() == null) {
				currentNode.setChildren(new FDTreeElement[this.numAttributes]);
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
			}
			else if (currentNode.getChildren()[i] == null) {
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
			}
				
			currentNode = currentNode.getChildren()[i];
			invalidFds.and(currentNode.rhsFds);
			currentNode.addRhsAttributes(rhs);
		}
		
		rhs.andNot(invalidFds);
		currentNode.markFds(rhs);
		rhs.or(invalidFds);

		this.depth = Math.max(this.depth, lhsLength);
		return currentNode;
	}

	public boolean containsFd(OpenBitSet lhs, int rhs) {
		FDTreeElement element = this;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			if ((element.getChildren() == null) || (element.getChildren()[i] == null))
				return false;
			element = element.getChildren()[i];
		}
		return element.isFd(rhs);
	}

	public FDTreeElement addGeneralization(OpenBitSet lhs, int rhs) {
		FDTreeElement currentNode = this;
		currentNode.addRhsAttribute(rhs);

		boolean newElement = false;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			if (currentNode.getChildren() == null) {
				currentNode.setChildren(new FDTreeElement[this.numAttributes]);
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
				newElement = true;
			}
			else if (currentNode.getChildren()[i] == null) {
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
				newElement = true;
			}
			
			currentNode = currentNode.getChildren()[i];
			currentNode.addRhsAttribute(rhs);
		}
		
		if (newElement)
			return currentNode;
		return null;
	}

	public FDTreeElement addGeneralization(OpenBitSet lhs, OpenBitSet rhs) {
		FDTreeElement currentNode = this;
		currentNode.addRhsAttributes(rhs);

		boolean newElement = false;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			if (currentNode.getChildren() == null) {
				currentNode.setChildren(new FDTreeElement[this.numAttributes]);
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
				newElement = true;
			}
			else if (currentNode.getChildren()[i] == null) {
				currentNode.getChildren()[i] = new FDTreeElement(this.numAttributes);
				newElement = true;
			}
				
			currentNode = currentNode.getChildren()[i];
			currentNode.addRhsAttributes(rhs);
		}
		
		if (newElement)
			return currentNode;
		return null;
	}

	public boolean containsFdOrGeneralization(OpenBitSet lhs, int rhs) {
		int nextLhsAttr = lhs.nextSetBit(0);
		return this.containsFdOrGeneralization(lhs, rhs, nextLhsAttr);
	}
	
	public OpenBitSet getFdOrGeneralization(OpenBitSet lhs, int rhs) {
		OpenBitSet foundLhs = new OpenBitSet();
		int nextLhsAttr = lhs.nextSetBit(0);
		if (this.getFdOrGeneralization(lhs, rhs, nextLhsAttr, foundLhs))
			return foundLhs;
		return null;
	}

	public List<OpenBitSet> getFdAndGeneralizations(OpenBitSet lhs, int rhs) {
		List<OpenBitSet> foundLhs = new ArrayList<>();
		OpenBitSet currentLhs = new OpenBitSet();
		int nextLhsAttr = lhs.nextSetBit(0);
		this.getFdAndGeneralizations(lhs, rhs, nextLhsAttr, currentLhs, foundLhs);
		return foundLhs;
	}

	public List<FDTreeElementLhsPair> getLevel(int level) {
		List<FDTreeElementLhsPair> result = new ArrayList<>();
		OpenBitSet currentLhs = new OpenBitSet();
		int currentLevel = 0;
		this.getLevel(level, currentLevel, currentLhs, result);
		return result;
	}

	public void filterGeneralizations() {
		// Traverse the tree depth-first
		// For each node, iterate all FDs
		// For each FD, store the FD, then remove all this.getFdOrGeneralization(lhs, rhs) and add the FD again
		OpenBitSet currentLhs = new OpenBitSet(this.numAttributes);
		this.filterGeneralizations(currentLhs, this);
	}
	
	public void filterGeneralizations(OpenBitSet lhs, int rhs) {
		OpenBitSet currentLhs = new OpenBitSet(this.numAttributes);
		int nextLhsAttr = lhs.nextSetBit(0);
		this.filterGeneralizations(lhs, rhs, nextLhsAttr, currentLhs);
	}

	public void removeFunctionalDependency(OpenBitSet lhs, int rhs) {
		int currentLhsAttr = lhs.nextSetBit(0);
		this.removeRecursive(lhs, rhs, currentLhsAttr);
	}

	public boolean containsFunctionalDependency(OpenBitSet lhs, int rhs) {
		FDTreeElement currentNode = this;

		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			if ((currentNode.getChildren() == null) || (currentNode.getChildren()[i] == null))
				return false;
			
			currentNode = currentNode.getChildren()[i];
		}
		
		return currentNode.isFd(rhs);
	}
	
	public boolean isEmpty() {
		return (this.rhsAttributes.cardinality() == 0);
	}

/*	public void filterSpecializations() {
		OpenBitSet activePath = new OpenBitSet();
		FDTree filteredTree = new FDTree(this.maxAttributeNumber);
		this.filterSpecializations(filteredTree, activePath);

		this.children = filteredTree.children;
		this.rhsFds = filteredTree.rhsFds;
	}

	public void printDependencies() {
		OpenBitSet activePath = new OpenBitSet();
		this.printDependencies(activePath);
	}
*/	
	// FUDEBS
	public class FD {
		public OpenBitSet lhs;
		public int rhs;
		public PositionListIndex pli;
		public FD(OpenBitSet lhs, int rhs, PositionListIndex pli) {
			this.lhs = lhs;
			this.rhs = rhs;
			this.pli = pli;
		}
	}
	
	public void addPrunedElements() {
		int numAttributes = this.numAttributes;
		
		OpenBitSet currentLhs = new OpenBitSet(numAttributes);
		
		if (this.getChildren() == null)
			return;
		
		for (int attr = 0; attr < this.numAttributes; attr++) {
			if (this.getChildren()[attr] != null) {
				currentLhs.set(attr);
				this.getChildren()[attr].addPrunedElements(currentLhs, attr, this);
				currentLhs.clear(attr);
			}
		}
	}
	
	public void growNegative(List<PositionListIndex> plis, int[][] invertedPlis, int numRecords) {
		int numAttributes = plis.size();
		
		OpenBitSet currentLhs = new OpenBitSet(numAttributes);
		
		// As root node, we need to check each rhs
		for (int rhs = 0; rhs < numAttributes; rhs++) {
			if (this.isFd(rhs)) // Is already invalid?
				continue;
			
			if (plis.get(rhs).isConstant(numRecords)) // Is {} -> rhs a valid FD
				continue;
			this.markFd(rhs);
			
			for (int attr = 0; attr < numAttributes; attr++) {
				if (attr == rhs)
					continue;
				
				if ((this.getChildren() != null) && (this.getChildren()[attr] != null) && this.getChildren()[attr].hasRhsAttribute(rhs)) // If there is a child with the current rhs, then currentLhs+attr->rhs must already be a known invalid fd
					continue;
					
				if (!plis.get(attr).refines(invertedPlis[rhs])) {
					// Add a new child representing the newly discovered invalid FD
					if (this.getChildren() == null)
						this.setChildren(new FDTreeElement[this.numAttributes]);
					if (this.getChildren()[attr] == null)
						this.getChildren()[attr] = new FDTreeElement(this.numAttributes);
					this.getChildren()[attr].addRhsAttribute(rhs);
					this.getChildren()[attr].markFd(rhs);
				}
			}
		}
		
		// Recursively call children
		if (this.getChildren() == null)
			return;
		for (int attr = 0; attr < numAttributes; attr++) {
			if (this.getChildren()[attr] != null) {
				currentLhs.set(attr);
				this.getChildren()[attr].growNegative(plis.get(attr), currentLhs, attr, plis, invertedPlis, this);
				currentLhs.clear(attr);
			}
		}
	}

	public void maximizeNegative(List<PositionListIndex> plis, int[][] invertedPlis, int numRecords) {
		// Maximizing negative cover is better than maximizing positive cover, because we do not need to check minimality; inversion does this automatically, i.e., generating a non-FD that creates a valid, non-minimal FD is not possible
		int numAttributes = plis.size();
		OpenBitSet currentLhs = new OpenBitSet(numAttributes);
		
		// Traverse the tree depth-first, left-first
		if (this.getChildren() != null) {
			for (int attr = 0; attr < numAttributes; attr++) {
				if (this.getChildren()[attr] != null) {
					currentLhs.set(attr);
					this.getChildren()[attr].maximizeNegativeRecursive(plis.get(attr), currentLhs, numAttributes, invertedPlis, this);
					currentLhs.clear(attr);
				}
			}
		}

		// Add invalid root FDs {} -/-> rhs to negative cover, because these are seeds for not yet discovered non-FDs
		this.addInvalidRootFDs(plis, numRecords); // TODO: These FDs make the search complex again :-(

		// On the way back, check all rhs-FDs that all their possible supersets are valid FDs; check with refines or, if available, with previously calculated plis
		//     which supersets to consider: add all attributes A with A notIn lhs and A notequal rhs; 
		//         for newLhs->rhs check that no FdOrSpecialization exists, because it is invalid then; this check might be slower than the FD check on high levels but faster on low levels in particular in the root! this check is faster on the negative cover, because we look for a non-FD
		for (int rhs = this.rhsFds.nextSetBit(0); rhs >= 0; rhs = this.rhsFds.nextSetBit(rhs + 1)) {
			OpenBitSet extensions = currentLhs.clone();
			extensions.flip(0, numAttributes);
			extensions.clear(rhs);
			
			// If a superset is a non-FD, mark this as not rhsFD, add the superset as a new node, filterGeneralizations() of the new node, call maximizeNegative() on the new supersets node
			//     if the superset node is in a right node of the tree, it will be checked anyways later; hence, only check supersets that are left or in the same tree path
			for (int extensionAttr = extensions.nextSetBit(0); extensionAttr >= 0; extensionAttr = extensions.nextSetBit(extensionAttr + 1)) {
				currentLhs.set(extensionAttr);
				
				if (this.containsFdOrSpecialization(currentLhs, rhs) || !plis.get(extensionAttr).refines(invertedPlis[rhs])) { // Otherwise, it must be false and a specialization is already contained; Only needed in root node, because we already filtered generalizations of other nodes and use a depth-first search that always removes generalizations when a new node comes in!
					this.rhsFds.clear(rhs);
					
					FDTreeElement newElement = this.addFunctionalDependency(currentLhs, rhs);
					//this.filterGeneralizations(currentLhs, rhs); // TODO: test effect
					newElement.maximizeNegativeRecursive(plis.get(extensionAttr), currentLhs, numAttributes, invertedPlis, this);
				}
				currentLhs.clear(extensionAttr);
			}
		}
	}

	protected void addInvalidRootFDs(List<PositionListIndex> plis, int numRecords) {
		// Root node: Check all attributes if they are unique; if A is not unique, mark this.isFD(A) as a non-FD; we need these seeds for a complete maximization
		for (int rhs = 0; rhs < this.numAttributes; rhs++)
			if (!plis.get(rhs).isConstant(numRecords)) // Is {} -> rhs an invalid FD
				this.markFd(rhs);
	}
	
/*	public void validateFDsFdWise(List<PositionListIndex> plis, Int2ObjectMap<Int2ObjectMap<PositionListIndex>> intersectedPlis, int[][] invertedPlis, int numRecords) {
		List<FDTreeElementLhsPair> currentLevel = new ArrayList<>();
		
		// Initialize first level with root
		currentLevel.add(new FDTreeElementLhsPair(this, new OpenBitSet(this.numAttributes)));
		
		// Start the level-wise validation/discovery
		int level = 0;
		while (!currentLevel.isEmpty()) {
			System.out.print("\tLevel " + level + ": " + currentLevel.size() + " elements; ");
			
System.out.print("(V)");			
			// Validate current level
			int validations = 0;
			List<FD> invalidFDs = new ArrayList<FD>();
			for (FDTreeElementLhsPair elementLhsPair : currentLevel) {
				FDTreeElement element = elementLhsPair.getElement();
				OpenBitSet lhs = elementLhsPair.getLhs();
				
				for (int rhs = element.rhsFds.nextSetBit(0); rhs >= 0; rhs = element.rhsFds.nextSetBit(rhs + 1)) { // TODO: do not do this for all rhs separately!
					validations++;
					
					if (level == 0) {
						// Check if rhs is unique
						if (plis.get(rhs).isConstant(numRecords))
							continue;
					}
					else if (level == 1) {
						// Check if lhs from plis refines rhs
						int lhsAttribute = lhs.nextSetBit(0);
						if (plis.get(lhsAttribute).refines(invertedPlis[rhs]))
							continue;
					}
					else {
						// Check if lhs from intersected plis plus remaining inverted plis refines rhs
						int firstLhsAttribute = lhs.nextSetBit(0); // TODO: We take the pli of the first two attributes here, because this is most likely the smallest (due to the sortation in the beginning); but another combination could be smaller now
						int secondLhsAttribute = lhs.nextSetBit(firstLhsAttribute + 1);
						
						PositionListIndex intersectedPli = intersectedPlis.get(firstLhsAttribute).get(secondLhsAttribute);
						if (intersectedPli == null) {
							int numAdditionalLhsAttributes = (int) lhs.cardinality() - 1;
							
							int[][] lhsInvertedPlis = new int[numAdditionalLhsAttributes][];
							int additionalLhsAttribute = secondLhsAttribute;
							for (int index = 0; index < numAdditionalLhsAttributes; index++) {
								lhsInvertedPlis[index] = invertedPlis[additionalLhsAttribute];
								additionalLhsAttribute = lhs.nextSetBit(additionalLhsAttribute + 1);
							}
							
							if (plis.get(firstLhsAttribute).refines(lhsInvertedPlis, invertedPlis[rhs]))
						//	if (plis.get(firstLhsAttribute).refines(compressedRecords, lhs, invertedPlis[rhs]))
								continue;
						}
						else {
							int numAdditionalLhsAttributes = (int) lhs.cardinality() - 2;
							
							int[][] lhsInvertedPlis = new int[numAdditionalLhsAttributes][];
							int additionalLhsAttribute = lhs.nextSetBit(secondLhsAttribute + 1);
							for (int index = 0; index < numAdditionalLhsAttributes; index++) {
								lhsInvertedPlis[index] = invertedPlis[additionalLhsAttribute];
								additionalLhsAttribute = lhs.nextSetBit(additionalLhsAttribute + 1);
							}
							
							if (intersectedPli.refines(lhsInvertedPlis, invertedPlis[rhs]))
								continue;
						}
					}
					
					element.removeFd(rhs);
					invalidFDs.add(new FD(lhs, rhs, null));
				}
			}
			
System.out.print("(C)");
			// Add all children to the next level
			List<FDTreeElementLhsPair> nextLevel = new ArrayList<>();
			for (FDTreeElementLhsPair elementLhsPair : currentLevel) {
				FDTreeElement element = elementLhsPair.getElement();
				OpenBitSet lhs = elementLhsPair.getLhs();
				
				if (element.getChildren() == null)
					continue;
					
				for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
					FDTreeElement child = element.getChildren()[childAttr];
					
					if (child != null) {
						OpenBitSet childLhs = lhs.clone();
						childLhs.set(childAttr);
						nextLevel.add(new FDTreeElementLhsPair(child, childLhs));
					}
				}
			}
			
System.out.print("(G); ");			
			// Generate new FDs from the invalid FDs and add them to the next level as well
			int candidates = 0;
			for (FD invalidFD : invalidFDs) {
				for (int extensionAttr = 0; extensionAttr < this.numAttributes; extensionAttr++) {
					OpenBitSet childLhs = this.extendWith(invalidFD.lhs, invalidFD.rhs, extensionAttr);
					if (childLhs != null) {
						FDTreeElement child = this.addFunctionalDependency(childLhs, invalidFD.rhs);
						nextLevel.add(new FDTreeElementLhsPair(child, childLhs));
						candidates++;
					}
				}
			}
			
			currentLevel = nextLevel;
			level++;
			System.out.println(validations + " validations; " + invalidFDs.size() + " invalid; " + candidates + " new candidates");
		}
	}
	
//	public void validateFDsElementWise(List<PositionListIndex> plis, int[][] invertedPlis, int numRecords, MemoryGuardian memoryGuardian) {
		int numAttributes = this.numAttributes;
		List<FDTreeElementLhsPair> currentLevel = new ArrayList<>();
		
		// Initialize first level with root
		currentLevel.add(new FDTreeElementLhsPair(this, new OpenBitSet(numAttributes)));
		
		// Start the level-wise validation/discovery
		int level = 0;
		ArrayList<IntegerPair> comparisonSuggestions = new ArrayList<>();
		while (!currentLevel.isEmpty()) {
			System.out.print("\tLevel " + level + ": " + currentLevel.size() + " elements; ");
			
System.out.print("(V)");			
			// Validate current level
			int validations = 0;
			int intersections = 0;
			List<FD> invalidFDs = new ArrayList<FD>();
			for (FDTreeElementLhsPair elementLhsPair : currentLevel) {
				FDTreeElement element = elementLhsPair.getElement();
				OpenBitSet lhs = elementLhsPair.getLhs();
				OpenBitSet rhs = element.rhsFds;
				
				int rhsSize = (int) rhs.cardinality();
				if (rhsSize == 0)
					continue;
				validations = validations + rhsSize;
				
				if (level == 0) {
					// Check if rhs is unique
					for (int rhsAttr = rhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = rhs.nextSetBit(rhsAttr + 1)) {
						if (!plis.get(rhsAttr).isConstant(numRecords)) {
							element.removeFd(rhsAttr);
							invalidFDs.add(new FD(lhs, rhsAttr, null));
						}
					}
				}
				else if (level == 1) {
					// Check if lhs from plis refines rhs
					int lhsAttribute = lhs.nextSetBit(0);
					for (int rhsAttr = rhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = rhs.nextSetBit(rhsAttr + 1)) {
						if (!plis.get(lhsAttribute).refines(invertedPlis[rhsAttr])) {
							element.removeFd(rhsAttr);
							invalidFDs.add(new FD(lhs, rhsAttr, null));
						}
					}
				}
				else {
					// Check if lhs from plis plus remaining inverted plis refines rhs
					int firstLhsAttr = lhs.nextSetBit(0);
					
					lhs.clear(firstLhsAttr);
					OpenBitSet validRhs = plis.get(firstLhsAttr).refines(invertedPlis, lhs, rhs, numAttributes, comparisonSuggestions);
					lhs.set(firstLhsAttr);
					
					intersections++;
					
					rhs.andNot(validRhs); // Now contains all invalid FDs
					element.setFds(validRhs); // Sets the valid FDs in the FD tree
					
					for (int rhsAttr = rhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = rhs.nextSetBit(rhsAttr + 1))
						invalidFDs.add(new FD(lhs, rhsAttr, null));
				}
			}
			
			// If the next level exceeds the predefined maximum lhs size, then we can stop here
			if ((this.maxDepth > -1) && (level >= this.maxDepth))
				break;
			
System.out.print("(C)");
			// Add all children to the next level
			List<FDTreeElementLhsPair> nextLevel = new ArrayList<>();
			for (FDTreeElementLhsPair elementLhsPair : currentLevel) {
				FDTreeElement element = elementLhsPair.getElement();
				OpenBitSet lhs = elementLhsPair.getLhs();

				if (element.getChildren() == null)
					continue;
				
				for (int childAttr = 0; childAttr < numAttributes; childAttr++) {
					FDTreeElement child = element.getChildren()[childAttr];
					
					if (child != null) {
						OpenBitSet childLhs = lhs.clone();
						childLhs.set(childAttr);
						nextLevel.add(new FDTreeElementLhsPair(child, childLhs));
					}
				}
			}
			
System.out.print("(G); ");			
			// Generate new FDs from the invalid FDs and add them to the next level as well
			int candidates = 0;
			for (FD invalidFD : invalidFDs) {
				int newCandidates = 0;
				for (int extensionAttr = 0; extensionAttr < this.numAttributes; extensionAttr++) {
					OpenBitSet childLhs = this.extendWith(invalidFD.lhs, invalidFD.rhs, extensionAttr);
					if (childLhs != null) {
						FDTreeElement child = this.addFunctionalDependencyGetIfNew(childLhs, invalidFD.rhs);
						if (child != null)
							nextLevel.add(new FDTreeElementLhsPair(child, childLhs));
						newCandidates++;
					}
				}
				candidates += newCandidates;
				
				memoryGuardian.match(this);
				if ((this.maxDepth > -1) && (level >= this.maxDepth))
					break;
			}
			
			currentLevel = nextLevel;
			level++;
			System.out.println(intersections + " intersections; " + validations + " validations; " + invalidFDs.size() + " invalid; " + candidates + " new candidates; --> " + (validations - invalidFDs.size()) + " FDs");
		}
	}
	
	private OpenBitSet extendWith(OpenBitSet lhs, int rhs, int extensionAttr) {
		if (lhs.get(extensionAttr) || 											// Triviality: AA->C cannot be valid, because A->C is invalid
			(rhs == extensionAttr) || 											// Triviality: AC->C cannot be valid, because A->C is invalid
			this.containsFdOrGeneralization(lhs, extensionAttr) ||				// Pruning: If A->B, then AB->C cannot be minimal 
			((this.getChildren() != null) && (this.getChildren()[extensionAttr] != null) && this.getChildren()[extensionAttr].isFd(rhs)))	
																				// Pruning: If B->C, then AB->C cannot be minimal
			return null;
		
		OpenBitSet childLhs = lhs.clone(); // TODO: This clone() could be avoided when done externally
		childLhs.set(extensionAttr);
		
		// TODO: Add more pruning here:
		
		// if contains FD: element was a child before and has already been added to the next level
		// if contains Generalization: element cannot be minimal, because generalizations have already been validated
		if (this.containsFdOrGeneralization(childLhs, rhs))										// Pruning: If A->C, then AB->C cannot be minimal
			return null;
		
		return childLhs;
	}
*/
/*	private OpenBitSet lookupAggressively(OpenBitSet lhs, int rhs, PositionListIndex pli, List<PositionListIndex> plis) {
		// Extend the lhs with some attribute
		int extensionAttr = -1;
		OpenBitSet extendedLhs = null;
		while (extendedLhs == null) {
			extensionAttr++;
			if (extensionAttr == this.numAttributes)
				return null;
			
			if (!lhs.get(extensionAttr) &&								// Triviality: AA->C cannot be valid, because A->C is invalid
				(rhs != extensionAttr)) {								// Triviality: AC->C cannot be valid, because A->C is invalid
				extendedLhs = lhs.clone();
				extendedLhs.set(extensionAttr);
			}
		}
		
		// Calculate the pli for the extended lhs
		PositionListIndex extendedPli = pli.intersect(plis.get(extensionAttr));
		
		// Check if the extended fd holds
		if (extendedPli.refines(plis.get(rhs))) {
			return extendedLhs;
		}
		
		// Proceed the aggressive lookup
		return this.lookupAggressively(extendedLhs, rhs, extendedPli, plis);
	}
*/	
	
	public List<FunctionalDependency> getFunctionalDependencies(ObjectArrayList<ColumnIdentifier> columnIdentifiers, List<PositionListIndex> plis) {
		List<FunctionalDependency> functionalDependencies = new ArrayList<FunctionalDependency>();
		this.addFunctionalDependenciesInto(functionalDependencies, new OpenBitSet(), columnIdentifiers, plis);
		return functionalDependencies;
	}
	
	public int addFunctionalDependenciesInto(FunctionalDependencyResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers, List<PositionListIndex> plis) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		return this.addFunctionalDependenciesInto(resultReceiver, new OpenBitSet(), columnIdentifiers, plis);
	}

	public int writeFunctionalDependencies(String outputFilePath, ObjectArrayList<ColumnIdentifier> columnIdentifiers, List<PositionListIndex> plis, boolean writeTableNamePrefix) {
		Writer writer = null;
		int numFDs = 0;
		try {
			writer = FileUtils.buildFileWriter(outputFilePath, false);
			numFDs = this.writeFunctionalDependencies(writer, new OpenBitSet(), columnIdentifiers, plis, writeTableNamePrefix);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return numFDs;
	}

	public void generalize() {
		int maxLevel = this.numAttributes;
		
		// Build an index level->nodes for the top-down, level-wise traversal
		Int2ObjectOpenHashMap<ArrayList<ElementLhsPair>> level2elements = new Int2ObjectOpenHashMap<>(maxLevel);
		for (int level = 0; level < maxLevel; level++)
			level2elements.put(level, new ArrayList<ElementLhsPair>());
		this.addToIndex(level2elements, 0, new OpenBitSet(this.numAttributes));
		
		// Traverse the levels top-down and add all direct generalizations
		for (int level = maxLevel - 1; level >= 0; level--) {
			for (ElementLhsPair pair : level2elements.get(level)) {
				// Remove isFDs, because we will mark valid FDs later on
				pair.element.removeAllFds();
				
				// Generate and add generalizations
				for (int lhsAttr = pair.lhs.nextSetBit(0); lhsAttr >= 0; lhsAttr = pair.lhs.nextSetBit(lhsAttr + 1)) {
					pair.lhs.clear(lhsAttr);
					FDTreeElement generalization = this.addGeneralization(pair.lhs, pair.element.getRhsAttributes());
					if (generalization != null)
						level2elements.get(level - 1).add(new ElementLhsPair(generalization, pair.lhs.clone()));
					pair.lhs.set(lhsAttr);
				}
			}
		}
	}

	public void grow() {
		this.grow(new OpenBitSet(this.numAttributes), this);
	}

	public void minimize() {
		this.minimize(new OpenBitSet(this.numAttributes), this);
	}

/*	public void discover(FDTree validFds, List<PositionListIndex> plis) {
		List<FDTreeElementLhsPair> currentLevel = new ArrayList<>();
		
		// Initialize with first level
		currentLevel.add(new FDTreeElementLhsPair(this, new OpenBitSet(this.numAttributes)));

		// Start the level-wise discovery
		while (!currentLevel.isEmpty()) {
			List<FDTreeElementLhsPair> nextLevel = new ArrayList<>();
			
			// Add all children of the current level to the next level (new children that we create during the following discovery will be added dynamically)
			for (FDTreeElementLhsPair elementLhsPair : currentLevel) {
				FDTreeElement element = elementLhsPair.getElement();
				OpenBitSet lhs = elementLhsPair.getLhs();
				
				for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
					if (element.getChildren()[childAttr] != null) {
						OpenBitSet childLhs = lhs.clone();
						childLhs.set(childAttr);
						nextLevel.add(new FDTreeElementLhsPair(element.getChildren()[childAttr], childLhs));
					}
				}
			}
			
			// Discover FDs with the elements of the current level; do not forget to place new invalid elements into the next level
			for (FDTreeElementLhsPair elementLhsPair : currentLevel) {
				FDTreeElement element = elementLhsPair.getElement();
				OpenBitSet lhs = elementLhsPair.getLhs();
				
				element.discover(lhs, plis, this, validFds, nextLevel);
			}
			
			currentLevel = nextLevel;
		}
	}*/
}
