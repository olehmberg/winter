package de.metanome.algorithms.hyfd.structures;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.uni_potsdam.hpi.utils.CollectionUtils;

public class FDTreeElement {

	protected FDTreeElement[] children;
	protected OpenBitSet rhsAttributes;
	protected OpenBitSet rhsFds;
	protected int numAttributes;
	
	public FDTreeElement(int numAttributes) {
		this.rhsAttributes = new OpenBitSet(numAttributes);
		this.rhsFds = new OpenBitSet(numAttributes);
		this.numAttributes = numAttributes;
	}

	public int getNumAttributes() {
		return this.numAttributes;
	}
	
	// children
	
	public FDTreeElement[] getChildren() {
		return this.children;
	}
	
	public void setChildren(FDTreeElement[] children) {
		this.children = children;
	}

	// rhsAttributes

	public OpenBitSet getRhsAttributes() {
		return this.rhsAttributes;
	}

	public void addRhsAttribute(int i) {
		this.rhsAttributes.set(i);
	}

	public void addRhsAttributes(OpenBitSet other) {
		this.rhsAttributes.or(other);
	}
	
	public void removeRhsAttribute(int i) {
		this.rhsAttributes.clear(i);
	}

	public boolean hasRhsAttribute(int i) {
		return this.rhsAttributes.get(i);
	}
	
	// rhsFds

	public OpenBitSet getFds() {
		return this.rhsFds;
	}

	public void markFd(int i) {
		this.rhsFds.set(i);
	}

	public void markFds(OpenBitSet other) {
		this.rhsFds.or(other);
	}

	public void removeFd(int i) {
		this.rhsFds.clear(i);
	}

	public void retainFds(OpenBitSet other) {
		this.rhsFds.and(other);
	}

	public void setFds(OpenBitSet other) {
		this.rhsFds = other;
	}

	public void removeAllFds() {
		this.rhsFds.clear(0, this.numAttributes);
	}

	public boolean isFd(int i) {
		return this.rhsFds.get(i);
	}

	protected void trimRecursive(int currentDepth, int newDepth) {
		if (currentDepth == newDepth) {
			this.children = null;
			this.rhsAttributes.and(this.rhsFds);
			return;
		}
		
		if (this.children != null)
			for (FDTreeElement child : this.children)
				if (child != null)
					child.trimRecursive(currentDepth + 1, newDepth);
	}

	protected void filterGeneralizations(OpenBitSet currentLhs, FDTree tree) {
		if (this.children != null) {		
			for (int attr = 0; attr < this.numAttributes; attr++) {
				if (this.children[attr] != null) {
					currentLhs.set(attr);
					this.children[attr].filterGeneralizations(currentLhs, tree);
					currentLhs.clear(attr);
				}
			}
		}
		for (int rhs = this.rhsFds.nextSetBit(0); rhs >= 0; rhs = this.rhsFds.nextSetBit(rhs + 1))
			tree.filterGeneralizations(currentLhs, rhs);
	}
	
	protected void filterGeneralizations(OpenBitSet lhs, int rhs, int currentLhsAttr, OpenBitSet currentLhs) {
		if (currentLhs.equals(lhs))
			return;
		
		this.rhsFds.clear(rhs);
		
		// Is the dependency already read and we have not yet found a generalization?
		if (currentLhsAttr < 0)
			return;
		
		if (this.children != null) {
			for (int nextLhsAttr = lhs.nextSetBit(currentLhsAttr); nextLhsAttr >= 0; nextLhsAttr = lhs.nextSetBit(nextLhsAttr + 1)) {
				if ((this.children[nextLhsAttr] != null) && (this.children[nextLhsAttr].hasRhsAttribute(rhs))) {
					currentLhs.set(nextLhsAttr);
					this.children[nextLhsAttr].filterGeneralizations(lhs, rhs, lhs.nextSetBit(nextLhsAttr + 1), currentLhs);
					currentLhs.clear(nextLhsAttr);
				}
			}
		}
	}
	
	protected boolean containsFdOrGeneralization(OpenBitSet lhs, int rhs, int currentLhsAttr) {
		if (this.isFd(rhs))
			return true;

		// Is the dependency already read and we have not yet found a generalization?
		if (currentLhsAttr < 0)
			return false;
		
		int nextLhsAttr = lhs.nextSetBit(currentLhsAttr + 1);
		
		if ((this.children != null) && (this.children[currentLhsAttr] != null) && (this.children[currentLhsAttr].hasRhsAttribute(rhs)))
			if (this.children[currentLhsAttr].containsFdOrGeneralization(lhs, rhs, nextLhsAttr))
				return true;
		
		return this.containsFdOrGeneralization(lhs, rhs, nextLhsAttr);
	}

	protected boolean getFdOrGeneralization(OpenBitSet lhs, int rhs, int currentLhsAttr, OpenBitSet foundLhs) {
		if (this.isFd(rhs))
			return true;

		// Is the dependency already read and we have not yet found a generalization?
		if (currentLhsAttr < 0)
			return false;
		
		int nextLhsAttr = lhs.nextSetBit(currentLhsAttr + 1);
		
		if ((this.children != null) && (this.children[currentLhsAttr] != null) && (this.children[currentLhsAttr].hasRhsAttribute(rhs))) {
			if (this.children[currentLhsAttr].getFdOrGeneralization(lhs, rhs, nextLhsAttr, foundLhs)) {
				foundLhs.set(currentLhsAttr);
				return true;
			}
		}
		
		return this.getFdOrGeneralization(lhs, rhs, nextLhsAttr, foundLhs);
	}

	protected void getFdAndGeneralizations(OpenBitSet lhs, int rhs, int currentLhsAttr, OpenBitSet currentLhs, List<OpenBitSet> foundLhs) {
		if (this.isFd(rhs))
			foundLhs.add(currentLhs.clone());

		if (this.children == null)
			return;
		
		while (currentLhsAttr >= 0) {
			int nextLhsAttr = lhs.nextSetBit(currentLhsAttr + 1);
			
			if ((this.children[currentLhsAttr] != null) && (this.children[currentLhsAttr].hasRhsAttribute(rhs))) {
				currentLhs.set(currentLhsAttr);
				this.children[currentLhsAttr].getFdAndGeneralizations(lhs, rhs, nextLhsAttr, currentLhs, foundLhs);
				currentLhs.clear(currentLhsAttr);
			}
			
			currentLhsAttr = nextLhsAttr;
		}
	}

	public void getLevel(int level, int currentLevel, OpenBitSet currentLhs, List<FDTreeElementLhsPair> result) {
		if (level == currentLevel) {
			result.add(new FDTreeElementLhsPair(this, currentLhs.clone()));
		}
		else {
			currentLevel++;
			if (this.children == null)
				return;
			
			for (int child = 0; child < this.numAttributes; child++) {
				if (this.children[child] == null)
					continue;
				
				currentLhs.set(child);
				this.children[child].getLevel(level, currentLevel, currentLhs, result);
				currentLhs.clear(child);
			}
		}
	}

	/**
	 * Return, whether the tree element contains a specialization of the
	 * functional dependency lhs -> rhs. </br>
	 * 
	 * @param lhs
	 *            The left-hand-side attribute set of the functional dependency.
	 * @param rhs
	 *            The dependent attribute.
	 * @param currentAttr
	 *            The last attribute from the left-hand-side attributes, which
	 *            has already been checked. This attribute and all smaller ones
	 *            have already been found in the path. </br> Only use values
	 *            from 0 to maxAttributeNumber. 0 is only used if no attribute
	 *            is checked yet.
	 * @return true, if the element contains a specialization of the functional
	 *         dependency lhs -> a. false otherwise.
	 */
	public boolean containsFdOrSpecialization(OpenBitSet lhs, int rhs) {
		int currentLhsAttr = lhs.nextSetBit(0);
		return this.containsFdOrSpecialization(lhs, rhs, currentLhsAttr);
	}
	
	protected boolean containsFdOrSpecialization(OpenBitSet lhs, int rhs, int currentLhsAttr) {
		if (!this.hasRhsAttribute(rhs))
			return false;
		
		// Is the dependency already covered?
		if (currentLhsAttr < 0)
			return true; // TODO: unsafe if fds can be removed from the tree without adding a specialization of the removed fd. Then, we cannot be sure here: maybe the attributes of the lhs are covered but the current lhs is not part of a valid fd if no isFd() is set for larger lhs in the tree (this might occur if the fd has been removed from the tree)
		
		if (this.children == null)
			return false;
		
		for (int child = 0; child < this.numAttributes; child++) {
			if (this.children[child] == null)
				continue;
			
			if (child == currentLhsAttr) {
				if (this.children[child].containsFdOrSpecialization(lhs, rhs, lhs.nextSetBit(currentLhsAttr + 1)))
					return true;
			}
			else {
				if (this.children[child].containsFdOrSpecialization(lhs, rhs, currentLhsAttr))
					return true;
			}	
		}
		return false;
	}

/*	public boolean getSpecialization(OpenBitSet lhs, int rhs, int currentAttr, OpenBitSet specLhsOut) { // TODO: difference to containsSpecialization() ?
		boolean found = false;
		// if (!specLhsOut.isEmpty()) {
		// specLhsOut.clear(0, this.maxAttributeNumber);
		// }

		if (!this.hasRhsAttribute(rhs)) {
			return false;
		}

		int attr = currentAttr; // Math.max(currentAttr, 1); TODO

		int nextSetAttr = lhs.nextSetBit(currentAttr); //TODO
		if (nextSetAttr < 0) {
			while (!found && (attr < this.maxAttributeNumber)) {
				if (this.children[attr] != null) {
					if (this.children[attr].hasRhsAttribute(rhs)) {
						found = this.children[attr].getSpecialization(lhs, rhs, currentAttr, specLhsOut);
					}
				}
				attr++;
			}
			if (found) {
				specLhsOut.set(attr);
			}
			return true;
		}

		while (!found && (attr <= nextSetAttr)) {
			if (this.children[attr] != null) {
				if (this.children[attr].hasRhsAttribute(rhs)) {
					if (attr < nextSetAttr) {
						found = this.children[attr].getSpecialization(lhs, rhs, currentAttr, specLhsOut);
					} else {
						found = this.children[nextSetAttr].getSpecialization(lhs, rhs, nextSetAttr, specLhsOut);
					}
				}
			}
			attr++;
		}

		if (found) {
			specLhsOut.set(attr);
		}

		return found;
	}
*/
	protected boolean removeRecursive(OpenBitSet lhs, int rhs, int currentLhsAttr) {
		// If this is the last attribute of lhs, remove the fd-mark from the rhs
		if (currentLhsAttr < 0) {
			this.removeFd(rhs);
			this.removeRhsAttribute(rhs);
			return true;
		}
		
		if ((this.children != null) && (this.children[currentLhsAttr] != null)) {
			// Move to the next child with the next lhs attribute
			if (!this.children[currentLhsAttr].removeRecursive(lhs, rhs, lhs.nextSetBit(currentLhsAttr + 1)))
				return false; // This is a shortcut: if the child was unable to remove the rhs, then this node can also not remove it
				
			// Delete the child node if it has no rhs attributes any more
			if (this.children[currentLhsAttr].getRhsAttributes().cardinality() == 0)
				this.children[currentLhsAttr] = null;
		}
		
		// Check if another child requires the rhs and if not, remove it from this node
		if (this.isLastNodeOf(rhs)) {
			this.removeRhsAttribute(rhs);
			return true;
		}
		return false;
	}

	protected boolean isLastNodeOf(int rhs) {
		if (this.children == null)
			return true;
		for (FDTreeElement child : this.children)
			if ((child != null) && child.hasRhsAttribute(rhs))
				return false;
		return true;
	}

/*	public void filterSpecializations(FDTree filteredTree, OpenBitSet activePath) {
		for (int attr = 0; attr < this.numAttributes; attr++) {
			if (this.children[attr] != null) {
				activePath.set(attr);
				this.children[attr].filterSpecializations(filteredTree, activePath);
				activePath.clear(attr);
			}
		}

		for (int attr = 0; attr < this.numAttributes; attr++)
			if (this.isFd(attr) && !filteredTree.containsFdOrSpecialization(activePath, attr))
				filteredTree.addFunctionalDependency(activePath, attr);
	}
*/
/*	// Only keep the most general dependencies in the tree
	public void filterGeneralizations(FDTree filteredTree, OpenBitSet activePath) {
		for (int attr = 0; attr < this.maxAttributeNumber; attr++) {
			if (this.isFd(attr)) {
				if (!filteredTree.containsFdOrGeneralization(activePath, attr, 0)) {
					filteredTree.addFunctionalDependency(activePath, attr);
				}
			}
		}
		for (int attr = this.maxAttributeNumber - 1; attr >= 0; attr--) {
			if (this.children[attr] != null) {
				activePath.set(attr);
				this.children[attr].filterGeneralizations(filteredTree, activePath);
				activePath.clear(attr);
			}
		}
	}
*/
/*	public void printDependencies(OpenBitSet activePath) {
		for (int attr = 0; attr < this.maxAttributeNumber; attr++) {
			if (this.isFd(attr)) {
				String out = "{";
				for (int i = activePath.nextSetBit(0); i >= 0; i = activePath.nextSetBit(i + 1)) {
					out += i + ",";
				}
				if (out.length() > 1) {
					out = out.substring(0, out.length() - 1);
				}

				out += "} -> " + attr;
				System.out.println(out);
			}
		}

		for (int attr = 0; attr < maxAttributeNumber; attr++) {
			if (this.children[attr] != null) {
				activePath.set(attr);
				this.children[attr].printDependencies(activePath);
				activePath.clear(attr);
			}
		}
	}
*/
	/**
	 * Checks, whether the dependent attribute ends in the current tree element.
	 * 
	 * @param rhs
	 *            the i'th dependent attribute.
	 * @return true, if the tree element does not have any children with the
	 *         same dependent attribute. false, otherwise.
	 */
/*	public boolean isLastNodeOf(int rhs) {
		if (!this.hasRhsAttribute(rhs))
			return false;
		
		// Check all children for the rhs
		for (int attr = 0; attr < this.maxAttributeNumber; attr++)
			if ((this.children[attr] != null) && (this.children[attr].hasRhsAttribute(rhs)))
				return false;
		
		return true;
	}
*/
	// FUDEBS
	protected void addOneSmallerGeneralizations(OpenBitSet currentLhs, int maxCurrentLhsAttribute, int rhs, FDTree tree) {
		for (int lhsAttribute = currentLhs.nextSetBit(0); lhsAttribute != maxCurrentLhsAttribute; lhsAttribute = currentLhs.nextSetBit(lhsAttribute + 1)) {
			currentLhs.clear(lhsAttribute);
			tree.addGeneralization(currentLhs, rhs);
			currentLhs.set(lhsAttribute);
		}
	}
	
	protected void addOneSmallerGeneralizations(OpenBitSet currentLhs, int maxCurrentLhsAttribute, OpenBitSet rhs, FDTree tree) {
		for (int lhsAttribute = currentLhs.nextSetBit(0); lhsAttribute != maxCurrentLhsAttribute; lhsAttribute = currentLhs.nextSetBit(lhsAttribute + 1)) {
			currentLhs.clear(lhsAttribute);
			tree.addGeneralization(currentLhs, rhs);
			currentLhs.set(lhsAttribute);
		}
	}
	
	public void addPrunedElements(OpenBitSet currentLhs, int maxCurrentLhsAttribute, FDTree tree) {
		this.addOneSmallerGeneralizations(currentLhs, maxCurrentLhsAttribute, this.rhsAttributes, tree);
		
		if (this.children == null)
			return;
		
		for (int attr = 0; attr < this.numAttributes; attr++) {
			if (this.children[attr] != null) {
				currentLhs.set(attr);
				this.children[attr].addPrunedElements(currentLhs, attr, tree);
				currentLhs.clear(attr);
			}
		}
	}
	
	public void growNegative(PositionListIndex currentPli, OpenBitSet currentLhs, int maxCurrentLhsAttribute, List<PositionListIndex> plis, int[][] rhsPlis, FDTree invalidFds) {
		int numAttributes = plis.size();

		PositionListIndex[] childPlis = new PositionListIndex[numAttributes];
		
		// I know that I am negative, but I have to check if I am a maximum-negative
		for (int rhs = this.rhsAttributes.nextSetBit(0); rhs >= 0; rhs = this.rhsAttributes.nextSetBit(rhs + 1)) { // For each non-set rhs, we know that a subset of the current lhs must have been a valid fd
			for (int attr = maxCurrentLhsAttribute + 1; attr < numAttributes; attr++) {
				if (attr == rhs)
					continue;
				
				if ((this.children != null) && (this.children[attr] != null) && this.children[attr].hasRhsAttribute(rhs)) // If there is a child with the current rhs, then currentLhs+attr->rhs must already be a known invalid fd
					continue;
				
				if (childPlis[attr] == null)
					childPlis[attr] = currentPli.intersect(rhsPlis[attr]);
				
				if (!childPlis[attr].refines(rhsPlis[rhs])) {
					// Add a new child representing the newly discovered invalid FD
					if (this.children == null)
						this.children = new FDTreeElement[this.numAttributes];
					if (this.children[attr] == null)
						this.children[attr] = new FDTreeElement(this.numAttributes);// Interesting question: Can I generate a new non-FD behind my current position in the tree? Check if attr > allOtherAttr in lhs
					this.children[attr].addRhsAttribute(rhs);
					this.children[attr].markFd(rhs);
					
					// Add all fds of newLhs-size -1 that include the new attribute, because these must also be invalid; use only size -1, because smaller sizes should already exist
					currentLhs.set(attr);
					this.addOneSmallerGeneralizations(currentLhs, attr, rhs, invalidFds);
					currentLhs.clear(attr);
				}
			}
		}
		
		if (this.children != null) {
			// Remove the plis for which no child exists
			for (int i = 0; i < numAttributes; i++)
				if (this.children[i] == null)
					childPlis[i] = null;
		
			// Recursively call children
			for (int attr = 0; attr < numAttributes; attr++) {
				if (this.children[attr] != null) {
					if (childPlis[attr] == null)
						childPlis[attr] = currentPli.intersect(rhsPlis[attr]);
					
					currentLhs.set(attr);
					this.children[attr].growNegative(childPlis[attr], currentLhs, attr, plis, rhsPlis, invalidFds);
					currentLhs.clear(attr);
					
					childPlis[attr] = null;
				}
			}
		}
	}

	protected void maximizeNegativeRecursive(PositionListIndex currentPli, OpenBitSet currentLhs, int numAttributes, int[][] rhsPlis, FDTree invalidFds) {
		PositionListIndex[] childPlis = new PositionListIndex[numAttributes];
		
		// Traverse the tree depth-first, left-first; generate plis for children and pass them over; store the child plis locally to reuse them for the checking afterwards
		if (this.children != null) {
			for (int attr = 0; attr < numAttributes; attr++) {
				if (this.children[attr] != null) {
					childPlis[attr] = currentPli.intersect(rhsPlis[attr]);
					
					currentLhs.set(attr);
					this.children[attr].maximizeNegativeRecursive(childPlis[attr], currentLhs, numAttributes, rhsPlis, invalidFds);
					currentLhs.clear(attr);
				}
			}
		}
		
		// On the way back, check all rhs-FDs that all their possible supersets are valid FDs; check with refines or, if available, with previously calculated plis
		//     which supersets to consider: add all attributes A with A notIn lhs and A notequal rhs; 
		//         for newLhs->rhs check that no FdOrSpecialization exists, because it is invalid then; this check might be slower than the FD check on high levels but faster on low levels in particular in the root! this check is faster on the negative cover, because we look for a non-FD
		for (int rhs = this.rhsFds.nextSetBit(0); rhs >= 0; rhs = this.rhsFds.nextSetBit(rhs + 1)) {
			OpenBitSet extensions = currentLhs.clone();
			extensions.flip(0, numAttributes);
			extensions.clear(rhs);
			
			for (int extensionAttr = extensions.nextSetBit(0); extensionAttr >= 0; extensionAttr = extensions.nextSetBit(extensionAttr + 1)) {
				currentLhs.set(extensionAttr);
				if (childPlis[extensionAttr] == null)
					childPlis[extensionAttr] = currentPli.intersect(rhsPlis[extensionAttr]);
				
				// If a superset is a non-FD, mark this as not rhsFD, add the superset as a new node, filterGeneralizations() of the new node, call maximizeNegative() on the new supersets node
				//     if the superset node is in a right node of the tree, it will be checked anyways later; hence, only check supersets that are left or in the same tree path
				if (!childPlis[extensionAttr].refines(rhsPlis[rhs])) {
					this.rhsFds.clear(rhs);

					FDTreeElement newElement = invalidFds.addFunctionalDependency(currentLhs, rhs);
					//invalidFds.filterGeneralizations(currentLhs, rhs); // TODO: test effect
					newElement.maximizeNegativeRecursive(childPlis[extensionAttr], currentLhs, numAttributes, rhsPlis, invalidFds);
				}
				currentLhs.clear(extensionAttr);
			}
		}
	}
	
	public void addFunctionalDependenciesInto(List<FunctionalDependency> functionalDependencies, OpenBitSet lhs, ObjectArrayList<ColumnIdentifier> columnIdentifiers, List<PositionListIndex> plis) {
		for (int rhs = this.rhsFds.nextSetBit(0); rhs >= 0; rhs = this.rhsFds.nextSetBit(rhs + 1)) {
			ColumnIdentifier[] columns = new ColumnIdentifier[(int) lhs.cardinality()];
			int j = 0;
			for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
				int columnId = plis.get(i).getAttribute(); // Here we translate the column i back to the real column i before the sorting
				columns[j++] = columnIdentifiers.get(columnId); 
			}
			
			ColumnCombination colCombination = new ColumnCombination(columns);
			int rhsId = plis.get(rhs).getAttribute(); // Here we translate the column rhs back to the real column rhs before the sorting
			FunctionalDependency fdResult = new FunctionalDependency(colCombination, columnIdentifiers.get(rhsId));
			functionalDependencies.add(fdResult);
		}

		if (this.getChildren() == null)
			return;
			
		for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
			FDTreeElement element = this.getChildren()[childAttr];
			if (element != null) {
				lhs.set(childAttr);
				element.addFunctionalDependenciesInto(functionalDependencies, lhs, columnIdentifiers, plis);
				lhs.clear(childAttr);
			}
		}
	}

	public int addFunctionalDependenciesInto(FunctionalDependencyResultReceiver resultReceiver, OpenBitSet lhs, ObjectArrayList<ColumnIdentifier> columnIdentifiers, List<PositionListIndex> plis) throws CouldNotReceiveResultException, ColumnNameMismatchException {
		int numFDs = 0;
		for (int rhs = this.rhsFds.nextSetBit(0); rhs >= 0; rhs = this.rhsFds.nextSetBit(rhs + 1)) {
			ColumnIdentifier[] columns = new ColumnIdentifier[(int) lhs.cardinality()];
			int j = 0;
			for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
				int columnId = plis.get(i).getAttribute(); // Here we translate the column i back to the real column i before the sorting
				columns[j++] = columnIdentifiers.get(columnId); 
			}
			
			ColumnCombination colCombination = new ColumnCombination(columns);
			int rhsId = plis.get(rhs).getAttribute(); // Here we translate the column rhs back to the real column rhs before the sorting
			FunctionalDependency fdResult = new FunctionalDependency(colCombination, columnIdentifiers.get(rhsId));
			resultReceiver.receiveResult(fdResult);
			numFDs++;
		}

		if (this.getChildren() == null)
			return numFDs;
			
		for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
			FDTreeElement element = this.getChildren()[childAttr];
			if (element != null) {
				lhs.set(childAttr);
				numFDs += element.addFunctionalDependenciesInto(resultReceiver, lhs, columnIdentifiers, plis);
				lhs.clear(childAttr);
			}
		}
		return numFDs;
	}
	
	public int writeFunctionalDependencies(Writer writer, OpenBitSet lhs, ObjectArrayList<ColumnIdentifier> columnIdentifiers, List<PositionListIndex> plis, boolean writeTableNamePrefix) throws IOException {
		int numFDs = (int)this.rhsFds.cardinality();
		
		if (numFDs != 0) {
			List<String> lhsIdentifier = new ArrayList<>();
			for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
				int columnId = plis.get(i).getAttribute(); // Here we translate the column i back to the real column i before the sorting
				if (writeTableNamePrefix)
					lhsIdentifier.add(columnIdentifiers.get(columnId).toString());
				else
					lhsIdentifier.add(columnIdentifiers.get(columnId).getColumnIdentifier());
			}
			Collections.sort(lhsIdentifier);
			String lhsString = "[" + CollectionUtils.concat(lhsIdentifier, ", ") + "]";
			
			List<String> rhsIdentifier = new ArrayList<>();
			for (int i = this.rhsFds.nextSetBit(0); i >= 0; i = this.rhsFds.nextSetBit(i + 1)) {
				int columnId = plis.get(i).getAttribute(); // Here we translate the column i back to the real column i before the sorting
				if (writeTableNamePrefix)
					rhsIdentifier.add(columnIdentifiers.get(columnId).toString());
				else
					rhsIdentifier.add(columnIdentifiers.get(columnId).getColumnIdentifier());
			}
			Collections.sort(rhsIdentifier);
			String rhsString = CollectionUtils.concat(rhsIdentifier, ", ");
			
			writer.write(lhsString + " --> " + rhsString + "\r\n");
		}
			
		if (this.getChildren() == null)
			return numFDs;
			
		for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
			FDTreeElement element = this.getChildren()[childAttr];
			if (element != null) {
				lhs.set(childAttr);
				numFDs += element.writeFunctionalDependencies(writer, lhs, columnIdentifiers, plis, writeTableNamePrefix);
				lhs.clear(childAttr);
			}
		}
		return numFDs;
	}
	
	public boolean filterDeadElements() {
		boolean allChildrenFiltered = true;
		if (this.children != null) {
			for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
				FDTreeElement element = this.children[childAttr];
				if (element != null) {
					if (element.filterDeadElements())
						this.children[childAttr] = null;
					else
						allChildrenFiltered = false;
				}
			}
		}
		return allChildrenFiltered && (this.rhsFds.nextSetBit(0) < 0);
	}

	protected class ElementLhsPair {
		public FDTreeElement element = null;
		public OpenBitSet lhs = null;
		public ElementLhsPair(FDTreeElement element, OpenBitSet lhs) {
			this.element = element;
			this.lhs = lhs;
		}
	}
	
	protected void addToIndex(Int2ObjectOpenHashMap<ArrayList<ElementLhsPair>> level2elements, int level, OpenBitSet lhs) {
		level2elements.get(level).add(new ElementLhsPair(this, lhs.clone()));
		if (this.children != null) {
			for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
				FDTreeElement element = this.children[childAttr];
				if (element != null) {
					lhs.set(childAttr);
					element.addToIndex(level2elements, level + 1, lhs);
					lhs.clear(childAttr);
				}
			}
		}
	}

	public void grow(OpenBitSet lhs, FDTree fdTree) {
		// Add specializations of all nodes an mark them as isFD, but if specialization exists, then it is invalid and should not be marked; only add specializations of nodes not marked as isFD!
		OpenBitSet rhs = this.rhsAttributes;
		
		OpenBitSet invalidRhs = rhs.clone();
		invalidRhs.remove(this.rhsFds);
		
		// Add specializations that are not invalid
		if (invalidRhs.cardinality() > 0) {
			for (int extensionAttr = 0; extensionAttr < this.numAttributes; extensionAttr++) {
				if (lhs.get(extensionAttr) || rhs.get(extensionAttr))
					continue;
				
				lhs.set(extensionAttr);
				fdTree.addFunctionalDependencyIfNotInvalid(lhs, invalidRhs);
				lhs.clear(extensionAttr);
			}
		}
		
		// Traverse children and let them add their specializations
		if (this.children != null) {
			for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
				FDTreeElement element = this.children[childAttr];
				if (element != null) {
					lhs.set(childAttr);
					element.grow(lhs, fdTree);
					lhs.clear(childAttr);
				}
			}
		}
	}

	protected void minimize(OpenBitSet lhs, FDTree fdTree) {
		// Traverse children and minimize their FDs
		if (this.children != null) {
			for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
				FDTreeElement element = this.children[childAttr];
				if (element != null) {
					lhs.set(childAttr);
					element.minimize(lhs, fdTree);
					lhs.clear(childAttr);
				}
			}
		}
		
		// Minimize Fds by checking for generalizations
		for (int rhs = this.rhsFds.nextSetBit(0); rhs >= 0; rhs = this.rhsFds.nextSetBit(rhs + 1)) {
			this.rhsFds.clear(rhs);
			
			// If the FD was minimal, i.e. no generalization exists, set it again
			if (!fdTree.containsFdOrGeneralization(lhs, rhs))
				this.rhsFds.set(rhs);
		}
	}
	
/*	public void validateRecursive(OpenBitSet lhs, PositionListIndex currentPli, List<PositionListIndex> initialPlis, FDTree invalidFds) {
		// Validate the current FDs
		for (int rhs = this.rhsFds.nextSetBit(0); rhs >= 0; rhs = this.rhsFds.nextSetBit(rhs + 1)) {
			if (!currentPli.refines(initialPlis.get(rhs))) {
				this.removeFd(rhs);
				invalidFds.addFunctionalDependency(lhs, rhs, currentPli);
			}
		}
		
		// Recursively validate FDs in child nodes
		for (int childAttr = 0; childAttr < this.numAttributes; childAttr++) {
			if (this.children[childAttr] == null)
				continue;
			
			PositionListIndex childPli = currentPli.intersect(initialPlis.get(childAttr));
			lhs.set(childAttr);
			this.children[childAttr].validateRecursive(lhs, childPli, initialPlis, invalidFds);
			lhs.clear(childAttr);
		}
	}
*/
/*	public void discover(OpenBitSet lhs, List<PositionListIndex> initialPlis, FDTree invalidFds, FDTree validFds, List<FDTreeElementLhsPair> nextLevel) {
		for (int rhs = this.rhsFds.nextSetBit(0); rhs >= 0; rhs = this.rhsFds.nextSetBit(rhs + 1)) {
			for (int attr = 0; attr < this.numAttributes; attr++) {
				if ((rhs == attr) || lhs.get(attr))
					continue;
				
				lhs.set(attr);
				
				if (validFds.containsFdOrGeneralization(lhs, rhs))
					continue;
				
				// containsFdOrGeneralization() will do all the pruning, but it is an exponential search!				
				// TODO: Find better pruning structures (might require C+ or FreeSets)
				// if A->C, then we do not need to test AB->C
				// if A->B, then we do not need to test AB->C 
				
				// Validate
				PositionListIndex intersectPli = this.getPli().intersect(initialPlis.get(attr));
				if (intersectPli.refines(initialPlis.get(rhs))) {
					validFds.addFunctionalDependency(lhs, rhs);
				}
				else {
					// TODO: if invalidFds.containsFdOrSpecialization we can skip the add, because the added fd must be invalid too
					FDTreeElement newElement = invalidFds.addFunctionalDependency(lhs, rhs, intersectPli);
					if (newElement != null)
						nextLevel.add(new FDTreeElementLhsPair(newElement, lhs.clone()));
				}
				
				lhs.clear(attr);
			}
		}
	}*/
}
