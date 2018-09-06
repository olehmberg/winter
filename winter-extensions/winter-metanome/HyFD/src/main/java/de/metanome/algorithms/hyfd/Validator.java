package de.metanome.algorithms.hyfd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.hyfd.structures.FDSet;
import de.metanome.algorithms.hyfd.structures.FDTree;
import de.metanome.algorithms.hyfd.structures.FDTreeElement;
import de.metanome.algorithms.hyfd.structures.FDTreeElementLhsPair;
import de.metanome.algorithms.hyfd.structures.IntegerPair;
import de.metanome.algorithms.hyfd.structures.PositionListIndex;
import de.metanome.algorithms.hyfd.utils.Logger;

public class Validator {

	private FDSet negCover;
	private FDTree posCover;
	private int numRecords;
	private List<PositionListIndex> plis;
	private int[][] compressedRecords;
	private float efficiencyThreshold;
	private MemoryGuardian memoryGuardian;
	private ExecutorService executor;
	
	private int level = 0;

	public Validator(FDSet negCover, FDTree posCover, int numRecords, int[][] compressedRecords, List<PositionListIndex> plis, float efficiencyThreshold, boolean parallel, MemoryGuardian memoryGuardian) {
		this.negCover = negCover;
		this.posCover = posCover;
		this.numRecords = numRecords;
		this.plis = plis;
		this.compressedRecords = compressedRecords;
		this.efficiencyThreshold = efficiencyThreshold;
		this.memoryGuardian = memoryGuardian;
		
		if (parallel) {
			int numThreads = Runtime.getRuntime().availableProcessors();
			this.executor = Executors.newFixedThreadPool(numThreads);
		}
	}
	
	private class FD {
		public OpenBitSet lhs;
		public int rhs;
		public FD(OpenBitSet lhs, int rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}
	
	private class ValidationResult {
		public int validations = 0;
		public int intersections = 0;
		public List<FD> invalidFDs = new ArrayList<>();
		public List<IntegerPair> comparisonSuggestions = new ArrayList<>();
		public void add(ValidationResult other) {
			this.validations += other.validations;
			this.intersections += other.intersections;
			this.invalidFDs.addAll(other.invalidFDs);
			this.comparisonSuggestions.addAll(other.comparisonSuggestions);
		}
	}
	
	private class ValidationTask implements Callable<ValidationResult> {
		private FDTreeElementLhsPair elementLhsPair;
		public void setElementLhsPair(FDTreeElementLhsPair elementLhsPair) {
			this.elementLhsPair = elementLhsPair;
		}
		public ValidationTask(FDTreeElementLhsPair elementLhsPair) {
			this.elementLhsPair = elementLhsPair;
		}
		public ValidationResult call() throws Exception {
			ValidationResult result = new ValidationResult();
			
			FDTreeElement element = this.elementLhsPair.getElement();
			OpenBitSet lhs = this.elementLhsPair.getLhs();
			OpenBitSet rhs = element.getFds();
			
			int rhsSize = (int) rhs.cardinality();
			if (rhsSize == 0)
				return result;
			result.validations = result.validations + rhsSize;
			
			if (Validator.this.level == 0) {
				// Check if rhs is unique
				for (int rhsAttr = rhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = rhs.nextSetBit(rhsAttr + 1)) {
					if (!Validator.this.plis.get(rhsAttr).isConstant(Validator.this.numRecords)) {
						element.removeFd(rhsAttr);
						result.invalidFDs.add(new FD(lhs, rhsAttr));
					}
					result.intersections++;
				}
			}
			else if (Validator.this.level == 1) {
				// Check if lhs from plis refines rhs
				int lhsAttribute = lhs.nextSetBit(0);
				for (int rhsAttr = rhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = rhs.nextSetBit(rhsAttr + 1)) {
					if (!Validator.this.plis.get(lhsAttribute).refines(Validator.this.compressedRecords, rhsAttr)) {
						element.removeFd(rhsAttr);
						result.invalidFDs.add(new FD(lhs, rhsAttr));
					}
					result.intersections++;
				}
			}
			else {
				// Check if lhs from plis plus remaining inverted plis refines rhs
				int firstLhsAttr = lhs.nextSetBit(0);
				
				lhs.clear(firstLhsAttr);
				OpenBitSet validRhs = Validator.this.plis.get(firstLhsAttr).refines(Validator.this.compressedRecords, lhs, rhs, result.comparisonSuggestions);
				lhs.set(firstLhsAttr);
				
				result.intersections++;
				
				rhs.andNot(validRhs); // Now contains all invalid FDs
				element.setFds(validRhs); // Sets the valid FDs in the FD tree
				
				for (int rhsAttr = rhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = rhs.nextSetBit(rhsAttr + 1))
					result.invalidFDs.add(new FD(lhs, rhsAttr));
			}
			return result;
		}
	}

	private ValidationResult validateSequential(List<FDTreeElementLhsPair> currentLevel) throws AlgorithmExecutionException {
		ValidationResult validationResult = new ValidationResult();
		
		ValidationTask task = new ValidationTask(null);
		for (FDTreeElementLhsPair elementLhsPair : currentLevel) {
			task.setElementLhsPair(elementLhsPair);
			try {
				validationResult.add(task.call());
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new AlgorithmExecutionException(e.getMessage());
			}
		}
		
		return validationResult;
	}
	
	private ValidationResult validateParallel(List<FDTreeElementLhsPair> currentLevel) throws AlgorithmExecutionException {
		ValidationResult validationResult = new ValidationResult();
		
		List<Future<ValidationResult>> futures = new ArrayList<>();
		for (FDTreeElementLhsPair elementLhsPair : currentLevel) {
			ValidationTask task = new ValidationTask(elementLhsPair);
			futures.add(this.executor.submit(task));
		}
		
		for (Future<ValidationResult> future : futures) {
			try {
				validationResult.add(future.get());
			}
			catch (ExecutionException e) {
				this.executor.shutdownNow();
				e.printStackTrace();
				throw new AlgorithmExecutionException(e.getMessage());
			}
			catch (InterruptedException e) {
				this.executor.shutdownNow();
				e.printStackTrace();
				throw new AlgorithmExecutionException(e.getMessage());
			}
		}
		
		return validationResult;
	}
	
	public List<IntegerPair> validatePositiveCover() throws AlgorithmExecutionException {
		int numAttributes = this.plis.size();
		
		Logger.getInstance().writeln("Validating FDs using plis ...");
		
		List<FDTreeElementLhsPair> currentLevel = null;
		if (this.level == 0) {
			currentLevel = new ArrayList<>();
			currentLevel.add(new FDTreeElementLhsPair(this.posCover, new OpenBitSet(numAttributes)));
		}
		else {
			currentLevel = this.posCover.getLevel(this.level);
		}
		
		// Start the level-wise validation/discovery
		int previousNumInvalidFds = 0;
		List<IntegerPair> comparisonSuggestions = new ArrayList<>();
		while (!currentLevel.isEmpty()) {
			Logger.getInstance().write("\tLevel " + this.level + ": " + currentLevel.size() + " elements; ");
			
			// Validate current level
			Logger.getInstance().write("(V)");
			
			ValidationResult validationResult = (this.executor == null) ? this.validateSequential(currentLevel) : this.validateParallel(currentLevel);
			comparisonSuggestions.addAll(validationResult.comparisonSuggestions);
			
			// If the next level exceeds the predefined maximum lhs size, then we can stop here
			if ((this.posCover.getMaxDepth() > -1) && (this.level >= this.posCover.getMaxDepth())) {
				int numInvalidFds = validationResult.invalidFDs.size();
				int numValidFds = validationResult.validations - numInvalidFds;
				Logger.getInstance().writeln("(-)(-); " + validationResult.intersections + " intersections; " + validationResult.validations + " validations; " + numInvalidFds + " invalid; " + "-" + " new candidates; --> " + numValidFds + " FDs");
				break;
			}
			
			// Add all children to the next level
			Logger.getInstance().write("(C)");
			
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
						
			// Generate new FDs from the invalid FDs and add them to the next level as well
			Logger.getInstance().write("(G); ");
			
			int candidates = 0;
			for (FD invalidFD : validationResult.invalidFDs) {
				for (int extensionAttr = 0; extensionAttr < numAttributes; extensionAttr++) {
					OpenBitSet childLhs = this.extendWith(invalidFD.lhs, invalidFD.rhs, extensionAttr);
					if (childLhs != null) {
						FDTreeElement child = this.posCover.addFunctionalDependencyGetIfNew(childLhs, invalidFD.rhs);
						if (child != null) {
							nextLevel.add(new FDTreeElementLhsPair(child, childLhs));
							candidates++;
							
							this.memoryGuardian.memoryChanged(1);
							this.memoryGuardian.match(this.negCover, this.posCover, null);
						}
					}
				}
				
				if ((this.posCover.getMaxDepth() > -1) && (this.level >= this.posCover.getMaxDepth()))
					break;
			}
			
			currentLevel = nextLevel;
			this.level++;
			int numInvalidFds = validationResult.invalidFDs.size();
			int numValidFds = validationResult.validations - numInvalidFds;
			Logger.getInstance().writeln(validationResult.intersections + " intersections; " + validationResult.validations + " validations; " + numInvalidFds + " invalid; " + candidates + " new candidates; --> " + numValidFds + " FDs");
		
			// Decide if we continue validating the next level or if we go back into the sampling phase
			if ((numInvalidFds > numValidFds * this.efficiencyThreshold) && (previousNumInvalidFds < numInvalidFds))
				return comparisonSuggestions;
			//	return new ArrayList<>();
			previousNumInvalidFds = numInvalidFds;
		}
		
		if (this.executor != null) {
			this.executor.shutdown();
			try {
				this.executor.awaitTermination(365, TimeUnit.DAYS);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private OpenBitSet extendWith(OpenBitSet lhs, int rhs, int extensionAttr) {
		if (lhs.get(extensionAttr) || 											// Triviality: AA->C cannot be valid, because A->C is invalid
			(rhs == extensionAttr) || 											// Triviality: AC->C cannot be valid, because A->C is invalid
			this.posCover.containsFdOrGeneralization(lhs, extensionAttr) ||		// Pruning: If A->B, then AB->C cannot be minimal // TODO: this pruning is not used in the Inductor when inverting the negCover; so either it is useless here or it is useful in the Inductor?
			((this.posCover.getChildren() != null) && (this.posCover.getChildren()[extensionAttr] != null) && this.posCover.getChildren()[extensionAttr].isFd(rhs)))	
																				// Pruning: If B->C, then AB->C cannot be minimal
			return null;
		
		OpenBitSet childLhs = lhs.clone(); // TODO: This clone() could be avoided when done externally
		childLhs.set(extensionAttr);
		
		// TODO: Add more pruning here
		
		// if contains FD: element was a child before and has already been added to the next level
		// if contains Generalization: element cannot be minimal, because generalizations have already been validated
		if (this.posCover.containsFdOrGeneralization(childLhs, rhs))										// Pruning: If A->C, then AB->C cannot be minimal
			return null;
		
		return childLhs;
	}

}
