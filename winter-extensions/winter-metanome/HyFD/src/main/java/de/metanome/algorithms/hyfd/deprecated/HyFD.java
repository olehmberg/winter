package de.metanome.algorithms.hyfd.deprecated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithms.hyfd.Inductor;
import de.metanome.algorithms.hyfd.MemoryGuardian;
import de.metanome.algorithms.hyfd.Sampler;
import de.metanome.algorithms.hyfd.Validator;
import de.metanome.algorithms.hyfd.fdep.FDEP;
import de.metanome.algorithms.hyfd.structures.FDList;
import de.metanome.algorithms.hyfd.structures.FDSet;
import de.metanome.algorithms.hyfd.structures.FDTree;
import de.metanome.algorithms.hyfd.structures.IntegerPair;
import de.metanome.algorithms.hyfd.structures.NonFDTree;
import de.metanome.algorithms.hyfd.structures.PLIBuilder;
import de.metanome.algorithms.hyfd.structures.PositionListIndex;
import de.metanome.algorithms.hyfd.utils.ValueComparator;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.FileUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@Deprecated
@SuppressWarnings("unused")
public class HyFD implements FunctionalDependencyAlgorithm, BooleanParameterAlgorithm, IntegerParameterAlgorithm, RelationalInputParameterAlgorithm {

	public enum Identifier {
		INPUT_GENERATOR, NULL_EQUALS_NULL, VALIDATE_PARALLEL, MAX_DETERMINANT_SIZE
	};

	private RelationalInputGenerator inputGenerator = null;
	private FunctionalDependencyResultReceiver resultReceiver = null;

	protected int numAttributes;
	protected ValueComparator valueComparator;
	private boolean validateParallel;
	
	private float efficiencyThreshold = 0.01f;
	
	private int progressiveThreshold = 0;	// Threshold on the number of new non-FDs to indicate when to stop enlarging the window size
	private int windowSize = 100;			// Size of the sliding window that defines which records should be compared in the sampling phase
	private int attributeThreshold = 5;		// Threshold on the number of an attribute's distinct values; defines when clusters become small enough to be useful for sampling 
	
//	private final int maxPivotsFromEachCluster = 0;		// Number of pivot records that are taken from each cluster for windowing
//	private final int maxPivotsFromEachAttribute = 0;	// Number of pivot records that are taken from each attribute for windowing
//	private final int topSize = 100;	 				// Number of records occurring in most clusters to take from each attribute pair
//	private final int bottomSize = 10;					// Number of records occurring in least clusters to take from each attribute pair (these should be records that occur almost only in this attribute pair)
	
	private int maxLhsSize = -1;		// The lhss can become numAttributes - 1 large, but usually we are only interested in FDs with lhs < some threshold (otherwise they would not be useful for normalization, key discovery etc.)
	private FDTree posCover;
	
	private final MemoryGuardian memoryGuardian = new MemoryGuardian(true);
	
	private String tableName;
	private List<String> attributeNames;

	@Override
	public String getAuthors() {
		return "Thorsten Papenbrock";
	}

	@Override
	public String getDescription() {
		return "Hybrid Sampling- and Lattice-Traversal-based FD discovery";
	}
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<ConfigurationRequirement<?>>(5);
		configs.add(new ConfigurationRequirementRelationalInput(HyFD.Identifier.INPUT_GENERATOR.name()));
		
		ConfigurationRequirementBoolean nullEqualsNull = new ConfigurationRequirementBoolean(HyFD.Identifier.NULL_EQUALS_NULL.name());
		Boolean[] defaultNullEqualsNull = new Boolean[1];
		defaultNullEqualsNull[0] = new Boolean(true);
		nullEqualsNull.setDefaultValues(defaultNullEqualsNull);
		nullEqualsNull.setRequired(true);
		configs.add(nullEqualsNull);

		ConfigurationRequirementBoolean validateParallel = new ConfigurationRequirementBoolean(HyFD.Identifier.VALIDATE_PARALLEL.name());
		Boolean[] defaultValidateParallel = new Boolean[1];
		defaultValidateParallel[0] = new Boolean(true);
		validateParallel.setDefaultValues(defaultValidateParallel);
		validateParallel.setRequired(true);
		configs.add(validateParallel);
		
/*		ConfigurationRequirementInteger maxLhsSize = new ConfigurationRequirementInteger(HyFD.Identifier.MAX_DETERMINANT_SIZE.name()); //TODO: This should become a paramater again!
		Integer[] defaultMaxLhsSize = new Integer[1];
		defaultMaxLhsSize[0] = new Integer(-1);
		maxLhsSize.setDefaultValues(defaultMaxLhsSize);
		maxLhsSize.setRequired(false);
		configs.add(maxLhsSize);
		
		// TODO: parallel sampling
*/
/*		ConfigurationRequirementInteger windowSize = new ConfigurationRequirementInteger(HyFD.Identifier.WINDOW_SIZE.name());
		Integer[] defaultWindowSize = new Integer[1];
		defaultWindowSize[0] = new Integer(100);
		windowSize.setDefaultValues(defaultWindowSize);
		windowSize.setRequired(false);
		configs.add(windowSize);

		ConfigurationRequirementInteger attributeThreshold = new ConfigurationRequirementInteger(HyFD.Identifier.ATTRIBUTE_THRESHOLD.name());
		Integer[] defaultAttributeThreshold = new Integer[1];
		defaultAttributeThreshold[0] = new Integer(5);
		attributeThreshold.setDefaultValues(defaultAttributeThreshold);
		attributeThreshold.setRequired(false);
		configs.add(attributeThreshold);
*/		
		return configs;
	}

	@Override
	public void setResultReceiver(FunctionalDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
		if (HyFD.Identifier.NULL_EQUALS_NULL.name().equals(identifier))
			this.valueComparator = new ValueComparator(values[0].booleanValue());
		else if (HyFD.Identifier.VALIDATE_PARALLEL.name().equals(identifier))
			this.validateParallel = values[0].booleanValue();
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values) throws AlgorithmConfigurationException {
		if (HyFD.Identifier.MAX_DETERMINANT_SIZE.name().equals(identifier))
			this.maxLhsSize = values[0].intValue();
/*		else if (HyFD.Identifier.WINDOW_SIZE.name().equals(identifier))
			this.windowSize = values[0].intValue();
		else if (HyFD.Identifier.ATTRIBUTE_THRESHOLD.name().equals(identifier))
			this.attributeThreshold = values[0].intValue();
*/		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
		if (HyFD.Identifier.INPUT_GENERATOR.name().equals(identifier))
			this.inputGenerator = values[0];
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	private void handleUnknownConfiguration(String identifier, String value) throws AlgorithmConfigurationException {
		throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + value);
	}

	@Override
	public String toString() {
		return "HyFD:\r\n\t" + 
				"inputGenerator: " + ((this.inputGenerator != null) ? this.inputGenerator.toString() : "-") + "\r\n\t" +
				"tableName: " + this.tableName + " (" + CollectionUtils.concat(this.attributeNames, ", ") + ")\r\n\t" +
				"numAttributes: " + this.numAttributes + "\r\n\t" +
				"isNullEqualNull: " + ((this.valueComparator != null) ? String.valueOf(this.valueComparator.isNullEqualNull()) : "-") + ")\r\n\t" +
				"maxLhsSize: " + this.maxLhsSize + "\r\n\t" +
				"positiveCover: " + ((this.posCover == null) ? "-" : this.posCover.toString());
	}

	private void initialize(RelationalInput relationalInput) throws AlgorithmExecutionException {
		this.tableName = relationalInput.relationName();
		this.attributeNames = relationalInput.columnNames();
		this.numAttributes = this.attributeNames.size();
		if (this.valueComparator == null)
			this.valueComparator = new ValueComparator(true);
	}
	
	@Override
	public void execute() throws AlgorithmExecutionException {
		long startTime = System.currentTimeMillis();
		if (this.inputGenerator == null)
			throw new AlgorithmConfigurationException("No input generator set!");
		if (this.resultReceiver == null)
			throw new AlgorithmConfigurationException("No result receiver set!");
		
		//this.executeFDEP();
		//this.executeFudebs();
		this.executeHyFD();
		
		System.out.println("Time: " + (System.currentTimeMillis() - startTime) + " ms");
	}

	private void executeHyFD() throws AlgorithmExecutionException {
		// Initialize
		System.out.println("Initializing ...");
		RelationalInput relationalInput = this.getInput();
		this.initialize(relationalInput);
		
		///////////////////////////////////////////////////////
		// Build data structures for sampling and validation //
		///////////////////////////////////////////////////////
		
		// Calculate plis
		System.out.println("Reading data and calculating plis ...");
		PLIBuilder pliBuilder = new PLIBuilder(-1);
		List<PositionListIndex> plis = pliBuilder.getPLIs(relationalInput, this.numAttributes, this.valueComparator.isNullEqualNull());
		this.closeInput(relationalInput);

		final int numRecords = pliBuilder.getNumLastRecords();
		pliBuilder = null;
		
		if (numRecords == 0) {
			ObjectArrayList<ColumnIdentifier> columnIdentifiers = this.buildColumnIdentifiers();
			for (int attr = 0; attr < this.numAttributes; attr++)
				this.resultReceiver.receiveResult(new FunctionalDependency(new ColumnCombination(), columnIdentifiers.get(attr)));
			return;
		}
		
		// Sort plis by number of clusters: For searching in the covers and for validation, it is good to have attributes with few non-unique values and many clusters left in the prefix tree
		System.out.println("Sorting plis by number of clusters ...");
		Collections.sort(plis, new Comparator<PositionListIndex>() {
			@Override
			public int compare(PositionListIndex o1, PositionListIndex o2) {		
				int numClustersInO1 = numRecords - o1.getNumNonUniqueValues() + o1.getClusters().size();
				int numClustersInO2 = numRecords - o2.getNumNonUniqueValues() + o2.getClusters().size();
				return numClustersInO2 - numClustersInO1;
			}
		});
		
		// Calculate inverted plis
		System.out.println("Inverting plis ...");
		int[][] invertedPlis = this.invertPlis(plis, numRecords);

		// Extract the integer representations of all records from the inverted plis
		System.out.println("Extracting integer representations for the records ...");
		int[][] compressedRecords = new int[numRecords][];
		for (int recordId = 0; recordId < numRecords; recordId++)
			compressedRecords[recordId] = this.fetchRecordFrom(recordId, invertedPlis);
		invertedPlis = null;
		
		// Initialize the negative cover
		FDSet negCover = new FDSet(this.numAttributes, this.maxLhsSize);
		
		// Initialize the positive cover
		FDTree posCover = new FDTree(this.numAttributes, this.maxLhsSize);
		posCover.addMostGeneralDependencies();
		
		//////////////////////////
		// Build the components //
		//////////////////////////
		
		Sampler sampler = new Sampler(negCover, posCover, compressedRecords, plis, this.efficiencyThreshold, this.valueComparator, this.memoryGuardian);
		Inductor inductor = new Inductor(negCover, posCover, this.memoryGuardian);
		Validator validator = new Validator(negCover, posCover, numRecords, compressedRecords, plis, this.efficiencyThreshold, this.validateParallel, this.memoryGuardian);
		
		List<IntegerPair> comparisonSuggestions = new ArrayList<>();
		do {
			FDList newNonFds = sampler.enrichNegativeCover(comparisonSuggestions);
			inductor.updatePositiveCover(newNonFds);
			comparisonSuggestions = validator.validatePositiveCover();
		}
		while (comparisonSuggestions != null);
		
		// Output all valid FDs
		System.out.println("Translating fd-tree into result format ...");
		
	//	int numFDs = posCover.writeFunctionalDependencies("HyFD_backup_" + this.tableName + "_results.txt", this.buildColumnIdentifiers(), plis, false);
		int numFDs = posCover.addFunctionalDependenciesInto(this.resultReceiver, this.buildColumnIdentifiers(), plis);
		
	/*	List<FunctionalDependency> result = posCover.getFunctionalDependencies(this.buildColumnIdentifiers(), plis);
		plis = null;
		int numFDs = 0;
		for (FunctionalDependency fd : result) {
			this.resultReceiver.receiveResult(fd);
			numFDs++;
		}
	*/	System.out.println("... done! (" + numFDs + " FDs)");
	}

	private void executeFDEP() throws AlgorithmExecutionException {
		// Initialize
		System.out.println("Initializing ...");
		RelationalInput relationalInput = this.getInput();
		this.initialize(relationalInput);
		
		// Load data
		System.out.println("Loading data ...");
		ObjectArrayList<List<String>> records = this.loadData(relationalInput);
		this.closeInput(relationalInput);
		
		// Create default output if input is empty
		if (records.isEmpty()) {
			ObjectArrayList<ColumnIdentifier> columnIdentifiers = this.buildColumnIdentifiers();
			for (int attr = 0; attr < this.numAttributes; attr++)
				this.resultReceiver.receiveResult(new FunctionalDependency(new ColumnCombination(), columnIdentifiers.get(attr)));
			return;
		}
		
		int numRecords = records.size();
		
		// Calculate plis
		System.out.println("Calculating plis ...");
		List<PositionListIndex> plis = PLIBuilder.getPLIs(records, this.numAttributes, this.valueComparator.isNullEqualNull());
		records = null; // we proceed with the values in the plis
		
		// Calculate inverted plis
		System.out.println("Inverting plis ...");
		int[][] invertedPlis = this.invertPlis(plis, numRecords);

		// Extract the integer representations of all records from the inverted plis
		System.out.println("Extracting integer representations for the records ...");
		int[][] compressedRecords = new int[numRecords][];
		for (int recordId = 0; recordId < numRecords; recordId++)
			compressedRecords[recordId] = this.fetchRecordFrom(recordId, invertedPlis);
		
		// Execute fdep
		System.out.println("Executing fdep ...");
		FDEP fdep = new FDEP(this.numAttributes, this.valueComparator);
		FDTree fds = fdep.execute(compressedRecords);
		
		// Output all valid FDs
		System.out.println("Translating fd-tree into result format ...");
		List<FunctionalDependency> result = fds.getFunctionalDependencies(this.buildColumnIdentifiers(), plis);
		plis = null;
		int numFDs = 0;
		for (FunctionalDependency fd : result) {
			//System.out.println(fd);
			this.resultReceiver.receiveResult(fd);
			numFDs++;
		}
		System.out.println("... done! (" + numFDs + " FDs)");
	}
	
	private void executeFudebs(ObjectArrayList<List<String>> records) throws InputIterationException, CouldNotReceiveResultException, ColumnNameMismatchException {
		final int numRecords = records.size();
		
		///////////////////////////////////////////////////////
		// Build data structures for sampling and validation //
		///////////////////////////////////////////////////////
		
		// Calculate plis
		System.out.println("Calculating plis ...");
		List<PositionListIndex> plis = PLIBuilder.getPLIs(records, this.numAttributes, this.valueComparator.isNullEqualNull());
		records = null; // we proceed with the values in the plis
		
		// Sort plis by number of clusters: For searching in the covers and for validation, it is good to have attributes with few non-unique values and many clusters left in the prefix tree
		System.out.println("Sort plis by number of clusters ...");
		Collections.sort(plis, new Comparator<PositionListIndex>() {
			@Override
			public int compare(PositionListIndex o1, PositionListIndex o2) {
//				if (o2.getNumNonUniqueValues() == o1.getNumNonUniqueValues())
//					return o2.getClusters().size() - o1.getClusters().size(); // Many clusters should be left
//				return o1.getNumNonUniqueValues() - o2.getNumNonUniqueValues(); // Few non-unique values should be left			
				int numClustersInO1 = numRecords - o1.getNumNonUniqueValues() + o1.getClusters().size();
				int numClustersInO2 = numRecords - o2.getNumNonUniqueValues() + o2.getClusters().size();
				return numClustersInO2 - numClustersInO1;
			}
		});
		
		// Calculate inverted plis
		System.out.println("Inverting plis ...");
		int[][] invertedPlis = this.invertPlis(plis, numRecords);

		// Extract the integer representations of all records from the inverted plis
		System.out.println("Extracting integer representations for the records ...");
		int[][] compressedRecords = new int[numRecords][];
		for (int recordId = 0; recordId < numRecords; recordId++)
			compressedRecords[recordId] = this.fetchRecordFrom(recordId, invertedPlis);
		
		// Compute plis of size 2 used to generate more sample comparisons and for candidate checks later on
		// Note: We should not do this, because most of these plis are not needed; the direct pli intersection must work well enough
		//       The number of intersect plis is low anyway and still this consumes much memory; and the benefit is low, because the plis are small anyway
/*		Int2ObjectMap<Int2ObjectMap<PositionListIndex>> intersectedPlis = new Int2ObjectOpenHashMap<>(this.numAttributes);
		for (int attr1 = 0; attr1 < this.numAttributes; attr1++) {
			Int2ObjectMap<PositionListIndex> currentIntersectPlis = new Int2ObjectOpenHashMap<PositionListIndex>(this.numAttributes - attr1);
			intersectedPlis.put(attr1, currentIntersectPlis);
			if (plis.get(attr1).getClusters().size() < this.clusterThreshold)
				continue;
			
			for (int attr2 = 0; attr2 < this.numAttributes; attr2++) {
				if (plis.get(attr2).getClusters().size() < this.clusterThreshold)
					continue;
				
				currentIntersectPlis.put(attr2, plis.get(attr1).intersect(invertedPlis[attr2]));
			}
		}
*/		
		//////////////////////////
		// Configure parameters //
		//////////////////////////
		
		// Set parameters dependent on the input dataset's size
		// ---> Setting the parameters dynamically turned out to be useless: if the data growth in one dimension or the other, we collect more samples anyway, i.e., we move the same window over more records
		//this.windowSize = (100000 / numRecords) * this.numAttributes / 20 + 10;
		
		//this.maxPivotsFromEachCluster = 100;
		//this.maxPivotsFromEachAttribute = 100000;
		
		/////////////////////////////////////////////
		// Collect record pairs for negative cover // // COLLECTING ALL RECORD PAIRS IS A BAD IDEA, BECAUSE THE NUMBER OF COMPARISONS THAT WE MIGHT NEED TO PERFORM IS MUCH LARGER THAN MAIN MEMORY CAPACITY !!!
		/////////////////////////////////////////////

		// TODO: Consider the size of the plis when collecting records for the sample --> larger plis might require more records / trade-off polynomial fdep and exponential growth-phase
		
	//	IntOpenHashSet[] recordPairs = new IntOpenHashSet[numRecords];
	//	for (int currentRecordId = 0; currentRecordId < numRecords; currentRecordId++)
	//		recordPairs[currentRecordId] = new IntOpenHashSet(); // TODO: Size estimation? recordIds2clusterIds.get(currentRecordId).size() * this.samplingWindowSize should usually greatly overestimate the size
		
	//	this.fetchSampleRecordsWindowingForEachRecord(recordPairs, plis);
	//	this.fetchSampleRecordsWindowingForEachRecord2(recordPairs, plis);
	//	this.fetchSampleRecordsWindowingForEachRecord3(recordPairs, plis);
		
		//this.fetchSampleRecordsTops(recordPairs, plis, compressedRecords, numRecords);
		//this.fetchSampleRecordsTopsFromSomeAttributePairClusters(recordPairs, intersectedPlis, numRecords);
		//this.fetchSampleRecordsTopsFromEachAttributePair(recordPairs, intersectedPlis, numRecords);
		//this.fetchSampleRecordsTopsFromEachAttributePair(recordPairs, recordIds2clusterIds, clusterIds2recordIds, intersectedPlis, numRecords);
		
		// TODO: Extract some more samples, compare them and add their non-FDs to the negative cover
		//       Can we find more record pairs with large overlap using sorting?
		
		////////////////////////////////////////////////////////////
		// Calculate negative cover using samples of record pairs //
		////////////////////////////////////////////////////////////
		
		System.out.println("Calculate negative cover ...");
		// Idee BitSets: @ Tobias Bleifuss
		Set<OpenBitSet> negCover = new HashSet<>(this.numAttributes);
//		NonFDTree negCover = new NonFDTree(this.numAttributes);
		
		// 0. Take the first record from each cluster and compare it against all other records of its clusters
	//	this.fetchNonFdsSPIDER(negCover, compressedRecords, plis);
		
		// 1. Move a window over cluster: move a window over all "small" clusters; we might have multiple comparisons here, but since we only consider small clusters, their number should not be so large
	//	this.fetchNonFdsWindowingOverClustersProgressive(negCover, compressedRecords, plis);
	//	this.fetchNonFdsWindowingOverClustersProgressive2(negCover, compressedRecords, plis);
		this.fetchNonFdsWindowingOverClustersProgressiveAveraging(negCover, compressedRecords, plis);
	//	this.fetchNonFdsWindowingOverClustersProgressiveAttributesParallel(negCover, compressedRecords, plis);
		
		// 2. Compare cluster first and cluster last records: for each cluster compare the window size first and last records, because interesting record pairs that are far apart in the original order might became close in some cluster
	//	this.fetchNonFdsFromClustersTopsAndBottomsProgressive(negCover, compressedRecords, plis);

		// 3. Move a window over records: compare each record in this window to its pivot record and if the comparison pli is not empty, then add it to the negative cover; this strategy moves the window over the large clusters 
	//	this.fetchNonFdsWindowingOverRecordsProgressive(negCover, compressedRecords);
		
		compressedRecords = null;
		
		OpenBitSet emptySet = new OpenBitSet(this.numAttributes);
		negCover.remove(emptySet);
		
//		System.out.println("Compress negative cover ...");
//		ArrayList<OpenBitSet> sortedNegCover = negCover.asBitSets();
//		negCover = null;
		
		System.out.println("Sort negative cover ...");
		ArrayList<OpenBitSet> sortedNegCover = new ArrayList<>(negCover);
		negCover = null;
/*		Collections.sort(sortedNegCover, new Comparator<OpenBitSet>() {
			@Override
			public int compare(OpenBitSet o1, OpenBitSet o2) {
				for (int i = 0; i < o1.length(); i++) {
					if (o1.get(i) && !o2.get(i))
						return 1;
					if (o2.get(i) && !o1.get(i))
						return -1;
				}
				return 0;
			}
		});
*/		
		Collections.sort(sortedNegCover, new Comparator<OpenBitSet>() {
			@Override
			public int compare(OpenBitSet o1, OpenBitSet o2) {
				return (int)(o1.cardinality() - o2.cardinality());
			}
		});
		
/*		for (OpenBitSet c : sortedNegCover) {
			for (int i = 0; i < this.numAttributes; i++)
				if (c.get(i))
					System.out.print(1 + " ");
				else
					System.out.print(0 + " ");
			System.out.println();
		}
*/		
/*		List<OpenBitSet> c = new ArrayList<>(sortedNegCover.size());
		for (int i = 0; i < sortedNegCover.size() - 1; i++) {
			OpenBitSet x = sortedNegCover.get(i).clone();
			x.andNot(sortedNegCover.get(i + 1));
			if (!x.isEmpty())
				c.add(sortedNegCover.get(i));
		}
		sortedNegCover = c;
*/		
		//////////////////////////////////////////////////
		// Calculate positive cover from negative cover //
		//////////////////////////////////////////////////
		
		System.out.println("Calculate positive cover ...");
		//TODO		List<List<OpenBitSet>> posCover = this.calculatePositiveCoverForest(sortedNegCover);
		this.posCover = this.calculatePositiveCover(sortedNegCover);
		sortedNegCover = null;
		
		/////////////////////////////////////////////////
		// Validate FDs from positive cover using PLIs //
		/////////////////////////////////////////////////
		
		System.out.println("Validating fds using plis ...");
		//posCover.validateFDsFdWise(plis, intersectedPlis, invertedPlis, numRecords);
	//	this.posCover.validateFDsElementWise(plis, invertedPlis, numRecords, this.memoryGuardian);
		invertedPlis = null;
		
		// Output all valid FDs
		System.out.println("Translating fd-tree into result format ...");
		List<FunctionalDependency> result = this.posCover.getFunctionalDependencies(this.buildColumnIdentifiers(), plis);
		plis = null;
		int numFDs = 0;
		for (FunctionalDependency fd : result) {
			this.resultReceiver.receiveResult(fd);
			numFDs++;
		}
		System.out.println("... done! (" + numFDs + " FDs)");
	}

	private void fetchNonFdsSPIDER(Set<OpenBitSet> negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.println("\tSPIDERing over clusters ...");
		for (PositionListIndex pli : plis) {
			for (IntArrayList cluster : pli.getClusters()) {
				int pivotRecord = cluster.getInt(0);
				
				IntArrayList[] clusters = new IntArrayList[this.numAttributes];
				for (int i = 0; i < this.numAttributes; i++)
					if (compressedRecords[pivotRecord][i] >= 0) // Maybe the record has no duplicate value in some attributes
						clusters[i] = plis.get(i).getClusters().get(compressedRecords[pivotRecord][i]);
				
				this.spider(clusters, pivotRecord, negCover);
			}
		}
	}
	
	private class Marker implements Comparable<Marker> {
		public int pointer;
		public int attribute;
		public IntArrayList cluster;
		public Marker(int pointer, int attribute, IntArrayList cluster) {
			this.pointer = pointer;
			this.attribute = attribute;
			this.cluster = cluster;
		}
		@Override
		public int compareTo(Marker o) {
			return this.getValue() - o.getValue();
		}
		public boolean next() {
			this.pointer++;
			return this.pointer < this.cluster.size();
		}
		public int getValue() {
			return this.cluster.getInt(this.pointer);
		}
	}
	
	private void spider(IntArrayList[] clusters, int pivotRecord, Set<OpenBitSet> negCover) {
		PriorityQueue<Marker> queue = new PriorityQueue<>(this.numAttributes);
		for (int i = 0; i < clusters.length; i++)
			if (clusters[i] != null)
				queue.add(new Marker(0, i, clusters[i]));
		
		while (!queue.isEmpty()) {
			Marker first = queue.poll();
			
			if ((queue.peek() != null ) && (queue.peek().getValue() == first.getValue())) {
				List<Marker> others = new ArrayList<>(this.numAttributes / 2);
				do {
					others.add(queue.remove());
				}
				while ((queue.peek() != null ) && (queue.peek().getValue() == first.getValue()));
				
				if (first.getValue() != pivotRecord) {
					OpenBitSet equalAttrs = new OpenBitSet(this.numAttributes);
					equalAttrs.set(first.attribute);
					for (Marker other : others)
						equalAttrs.set(other.attribute);
				}
				
				for (Marker other : others)
					if (other.next())
						queue.add(other);
			}
			
			if (first.next())
				queue.add(first);
		}
	}

	private void print(int[] record) {
		System.out.print("[");
		for (int i = 0; i < record.length; i++)
			System.out.print(record[i] + ",");
		System.out.println("]");
	}
	
	private class RingBufferInt {
		private int[] buffer;
		private int pointer;
		private boolean full;
		public RingBufferInt(int size) {
			this.buffer = new int[size];
			for (int i = 0; i < size; i++)
				this.buffer[i] = -1;
			this.pointer = 0;
			this.full = false;
		}
		public void add(int newEntry) {
			this.buffer[this.pointer] = newEntry;
			this.increment();
		}
		public int avg() {
			if (!this.full)
				return -1;
			int sum = 0;
			for (int i = 0; i < this.buffer.length; i++)
				sum += this.buffer[i];
			return sum / this.buffer.length;
		}
		private void increment() {
			if (this.pointer == this.buffer.length - 1) {
				this.pointer = 0;
				this.full = true;
			}
			else {
				this.pointer++;
			}
		}
	}

	private class RingBufferFloat {
		private float[] buffer;
		private int pointer;
		private boolean full;
		public RingBufferFloat(int size) {
			this.buffer = new float[size];
			for (int i = 0; i < size; i++)
				this.buffer[i] = -1;
			this.pointer = 0;
			this.full = false;
		}
		public void add(float newEntry) {
			this.buffer[this.pointer] = newEntry;
			this.increment();
		}
		public float avg() {
			if (!this.full)
				return -1;
			float sum = 0;
			for (int i = 0; i < this.buffer.length; i++)
				sum += this.buffer[i];
			return sum / this.buffer.length;
		}
		private void increment() {
			if (this.pointer == this.buffer.length - 1) {
				this.pointer = 0;
				this.full = true;
			}
			else {
				this.pointer++;
			}
		}
	}
	
	private class ClusterComparator implements Comparator<Integer> {
		private int[][] sortKeys;
		private int activeKey1;
		private int activeKey2;
		public ClusterComparator(int[][] sortKeys, int activeKey1, int activeKey2) {
			super();
			this.sortKeys = sortKeys;
			this.activeKey1 = activeKey1;
			this.activeKey2 = activeKey2;
		}
		public void incrementActiveKey() {
			this.activeKey1 = this.increment(this.activeKey1);
			this.activeKey2 = this.increment(this.activeKey2);
		}
		@Override
		public int compare(Integer o1, Integer o2) {
			// Next
		/*	int value1 = this.sortKeys[o1.intValue()][this.activeKey2];
			int value2 = this.sortKeys[o2.intValue()][this.activeKey2];
			return value2 - value1;
		*/	
			// Previous
		/*	int value1 = this.sortKeys[o1.intValue()][this.activeKey1];
			int value2 = this.sortKeys[o2.intValue()][this.activeKey1];
			return value2 - value1;
		*/	
			// Previous -> Next
			int value1 = this.sortKeys[o1.intValue()][this.activeKey1];
			int value2 = this.sortKeys[o2.intValue()][this.activeKey1];
			int result = value2 - value1;
			if (result == 0) {
				value1 = this.sortKeys[o1.intValue()][this.activeKey2];
				value2 = this.sortKeys[o2.intValue()][this.activeKey2];
			}
			return value2 - value1;
			
			// Next -> Previous
		/*	int value1 = this.sortKeys[o1.intValue()][this.activeKey2];
			int value2 = this.sortKeys[o2.intValue()][this.activeKey2];
			int result = value2 - value1;
			if (result == 0) {
				value1 = this.sortKeys[o1.intValue()][this.activeKey1];
				value2 = this.sortKeys[o2.intValue()][this.activeKey1];
			}
			return value2 - value1;
		*/	
		}
		private int increment(int number) {
			return (number == this.sortKeys[0].length - 1) ? 0 : number + 1;
		}
	}
	
	private void fetchNonFdsWindowingOverClustersProgressive(Set<OpenBitSet> negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.print("\tMoving window over clusters ... ");
		
		long time = System.currentTimeMillis();
		ClusterComparator comparator = new ClusterComparator(compressedRecords, compressedRecords[0].length - 1, 1);
		for (PositionListIndex pli : plis) {
			for (IntArrayList cluster : pli.getClusters()) {
				Collections.sort(cluster, comparator);
				
			//	Collections.shuffle(cluster, new Random(System.nanoTime()));
				
			//	for (int i : cluster)
			//		this.print(compressedRecords[i]);
			//	System.out.println();
			}
			comparator.incrementActiveKey();
		}
		System.out.println("Sorting (" + (System.currentTimeMillis() - time) + "ms)");
		
		for (int attribute = 0; attribute < this.numAttributes; attribute++) {
			System.out.print(attribute);
			
			RingBufferInt ringBuffer = new RingBufferInt(100);
			int numComparisons = 0;
			int currentWindowDistance = 1;
			int lastNegCoverSize = 0;
			List<IntArrayList> clusters = plis.get(attribute).getClusters();
			List<IntArrayList> clustersCopy = new ArrayList<IntArrayList>(clusters);

			do {
				lastNegCoverSize = negCover.size();
				numComparisons = 0;
				
				Iterator<IntArrayList> clusterIterator = clustersCopy.iterator();
				while (clusterIterator.hasNext()) {
					IntArrayList cluster = clusterIterator.next();
					
					if (cluster.size() <= currentWindowDistance) {
						clusterIterator.remove();
						continue;
					}
					
					for (int recordIndex = 0; recordIndex < (cluster.size() - currentWindowDistance); recordIndex++) {
						int recordId = cluster.getInt(recordIndex);
						int partnerRecordId = cluster.getInt(recordIndex + currentWindowDistance);
						negCover.add(this.getViolatedFds(compressedRecords[recordId], compressedRecords[partnerRecordId]));
						
						numComparisons++;
					}
				}

			//	System.out.println("\t" + attribute + ": w=" + currentWindowDistance + ", new=" + (negCover.size() - lastNegCoverSize) + ", comp=" + numComparisons + ", avg=" + ringBuffer.avg());
				
				currentWindowDistance++;
				
			//	int newNonFds = negCover.size() - lastNegCoverSize;
			//	if (newNonFds == ringBuffer.avg())
			//		break;
			//	ringBuffer.add(newNonFds);
			}
		//	while (negCover.size() - lastNegCoverSize > Math.floor(currentWindowDistance * 0.01f) * Math.ceil(numComparisons / 1000000.0f));
			while (negCover.size() - lastNegCoverSize > Math.floor(currentWindowDistance * 0.01f));
		//	while ((negCover.size() - lastNegCoverSize > 0) && (currentWindowDistance <= 1000));
		//	while (negCover.size() - lastNegCoverSize > (numComparisons / 100000));
		//	while (negCover.size() - lastNegCoverSize > 0);
			
			System.out.print("[" + (currentWindowDistance - 1) + "] ");
			
			if ((clusters.size() == 1) && (clusters.get(0).size() == compressedRecords[0].length)) // If we saw a pli with only a single cluster, all following plis also have (due to previous sorting) only one cluster and we can skip them
				break;
		}
		
		System.out.println();
	}

	private class AttributeComparator implements Comparator<Integer> {
		private int[] criterion;
		public AttributeComparator(int[] criterion) {
			this.criterion = criterion;
		}
		@Override
		public int compare(Integer o1, Integer o2) {
			return this.criterion[o2.intValue()] - this.criterion[o1.intValue()];
		}
	}

	private class AttributeRepresentant implements Comparable<AttributeRepresentant> {
		private int windowDistance;
		private IntArrayList numNewNonFds = new IntArrayList(100);
		private IntArrayList numComparisons = new IntArrayList(100);
		private float efficiencyFactor;
		private List<IntArrayList> clusters;
		private ValueComparator valueComparator;
		public int getEfficiency() {
			int sumNonFds = 0;
			int sumComparisons = 0;
			int index = this.numNewNonFds.size() - 1;
			while ((index >= 0) && (sumComparisons < this.efficiencyFactor)) {
				sumNonFds += this.numNewNonFds.getInt(index);
				sumComparisons += this.numComparisons.getInt(index);
				index--;
			}
			if (sumComparisons == 0)
				return 0;
			return (int)(sumNonFds * (this.efficiencyFactor / sumComparisons));
		}
		public AttributeRepresentant(List<IntArrayList> clusters, ValueComparator valueComparator, float efficiencyFactor) {
			this.clusters = new ArrayList<IntArrayList>(clusters);
			this.valueComparator = valueComparator;
			this.efficiencyFactor = efficiencyFactor;
		}
		@Override
		public int compareTo(AttributeRepresentant o) {
//			return o.getNumNewNonFds() - this.getNumNewNonFds();		
			return (int)Math.signum(o.getEfficiency() - this.getEfficiency());
		}
		public boolean runNext(Set<OpenBitSet> negCover, int[][] compressedRecords) {
			this.windowDistance++;
			int numNewNonFds = 0;
			int numComparisons = 0;
			
			int previousNegCoverSize = negCover.size();
			Iterator<IntArrayList> clusterIterator = this.clusters.iterator();
			while (clusterIterator.hasNext()) {
				IntArrayList cluster = clusterIterator.next();
				
				if (cluster.size() <= this.windowDistance) {
					clusterIterator.remove();
					continue;
				}
				
				for (int recordIndex = 0; recordIndex < (cluster.size() - this.windowDistance); recordIndex++) {
					int recordId = cluster.getInt(recordIndex);
					int partnerRecordId = cluster.getInt(recordIndex + this.windowDistance);
					negCover.add(this.getViolatedFds(compressedRecords[recordId], compressedRecords[partnerRecordId]));
					numComparisons++;
				}
			}
			numNewNonFds = negCover.size() - previousNegCoverSize;
			
			this.numNewNonFds.add(numNewNonFds);
			this.numComparisons.add(numComparisons);
			
			if (numComparisons == 0)
				return false;
			return true;
		}
		private OpenBitSet getViolatedFds(int[] t1, int[] t2) {
			// NOTE: This is a copy of the same function in HyFD
			OpenBitSet equalAttrs = new OpenBitSet(t1.length);
			for (int i = 0; i < t1.length; i++)
				if (this.valueComparator.isEqual(t1[i], t2[i]))
					equalAttrs.set(i);
			return equalAttrs;
		}
	}
	
	private void fetchNonFdsWindowingOverClustersProgressiveAttributesParallel(Set<OpenBitSet> negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.print("\tMoving window over clusters ... ");
		
		long time = System.currentTimeMillis();
		ClusterComparator comparator = new ClusterComparator(compressedRecords, compressedRecords[0].length - 1, 1);
		for (PositionListIndex pli : plis) {
			for (IntArrayList cluster : pli.getClusters()) {
				Collections.sort(cluster, comparator);
			}
			comparator.incrementActiveKey();
		}
		System.out.println("Sorting (" + (System.currentTimeMillis() - time) + "ms)");
		
		PriorityQueue<AttributeRepresentant> queue = new PriorityQueue<AttributeRepresentant>(this.numAttributes);
		for (int i = 0; i < this.numAttributes; i++) {
			AttributeRepresentant attributeRepresentant = new AttributeRepresentant(plis.get(i).getClusters(), this.valueComparator, 10000);
			attributeRepresentant.runNext(negCover, compressedRecords);
			if (attributeRepresentant.getEfficiency() != 0)
				queue.add(attributeRepresentant);
		}
	//	List<AttributeRepresentant> attributeRepresentantsCopy = new ArrayList<AttributeRepresentant>(attributeRepresentants);
		
	/*	for (int i = 0; i < 1000; i++) {
			Iterator<AttributeRepresentant> attributeIterator = attributeRepresentants.iterator();
			while (attributeIterator.hasNext()) {
				AttributeRepresentant attributeRepresentant = attributeIterator.next();
				attributeRepresentant.runNext(negCover, compressedRecords);
				
				if (attributeRepresentant.getNumNewNonFds() == 0)
					attributeIterator.remove();
			}
			
			int sum = 0;
			for (AttributeRepresentant attributeRepresentant : attributeRepresentantsCopy)
				sum += attributeRepresentant.getNumNewNonFds();
			System.out.println(sum);
			//for (AttributeRepresentant attributeRepresentant : attributeRepresentants) {
			//	attributeRepresentant.runNext(negCover, compressedRecords);
			//	System.out.print(attributeRepresentant.getNumNewNonFds() + ";");
			//}
			//System.out.println();
		}
	*/	
	//	long startTime = System.currentTimeMillis();
		int iterations = this.numAttributes;
	//	RingBufferFloat ringBuffer = new RingBufferFloat(100);
		while (!queue.isEmpty()) {
			iterations++;
			
			AttributeRepresentant attributeRepresentant = queue.remove();
			if (!attributeRepresentant.runNext(negCover, compressedRecords))
				continue;
			
			if (attributeRepresentant.getEfficiency() != 0) {
				queue.add(attributeRepresentant);
				
	//			if (attributeRepresentant.getEfficiency() == ringBuffer.avg()) {
	//				System.out.println(attributeRepresentant.getEfficiency() + " " + iterations);
	//				break;
	//			}
	//			
	//			ringBuffer.add(attributeRepresentant.getEfficiency());
				
//				System.out.println((int)(attributeRepresentant.getEfficiency() * 1000000));
			}
			
//			if (System.currentTimeMillis() - startTime > 10000)
//				break;
		}
	}
	
	private void fetchNonFdsWindowingOverClustersProgressiveAveraging(Set<OpenBitSet> negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.print("\tMoving window over clusters ... ");
		
		long time = System.currentTimeMillis();
		ClusterComparator comparator = new ClusterComparator(compressedRecords, compressedRecords[0].length - 1, 1);
		for (PositionListIndex pli : plis) {
			for (IntArrayList cluster : pli.getClusters()) {
				Collections.sort(cluster, comparator);
			}
			comparator.incrementActiveKey();
		}
		System.out.println("Sorting (" + (System.currentTimeMillis() - time) + "ms)");
		
		int[] lastWindowDistances = new int[this.numAttributes];
		for (int i = 0; i < this.numAttributes; i++)
			lastWindowDistances[i] = 0;
		
		int[] lastNumNewNonFds = new int[this.numAttributes];
		for (int i = 0; i < this.numAttributes; i++)
			lastNumNewNonFds[i] = -1;
		
		List<List<IntArrayList>> clusterCopies = new ArrayList<List<IntArrayList>>(this.numAttributes);
		for (int i = 0; i < this.numAttributes; i++)
			clusterCopies.add(new ArrayList<IntArrayList>(plis.get(i).getClusters()));
		
		int numCandidates = 0;
		int avg = 0;
		
		IntArrayList activeAttributes = new IntArrayList(this.numAttributes);
		for (int i = 0; i < this.numAttributes; i++)
			activeAttributes.add(i);
		
		while (activeAttributes.size() > 1) {
		/*	IntListIterator activeAttributeIterator = activeAttributes.iterator();
			while (activeAttributeIterator.hasNext()) {
				int attribute = activeAttributeIterator.nextInt();
				
				if (lastNumNewNonFds[attribute] == 0) {
					activeAttributeIterator.remove();
					continue;
				}
			}
		*/
			for (int attribute : activeAttributes) {
				if (lastNumNewNonFds[attribute] == 0)
					continue;
				
				System.out.print(attribute);
				
				RingBufferInt ringBuffer = new RingBufferInt(100);
				int currentWindowDistance = lastWindowDistances[attribute] + 1;
				int lastNegCoverSize = 0;
				List<IntArrayList> clusterCopy = clusterCopies.get(attribute);
	
				do {
					lastNegCoverSize = negCover.size();
					
					Iterator<IntArrayList> clusterIterator = clusterCopy.iterator();
					while (clusterIterator.hasNext()) {
						IntArrayList cluster = clusterIterator.next();
						
						if (cluster.size() <= currentWindowDistance) {
							clusterIterator.remove();
							continue;
						}
						
						for (int recordIndex = 0; recordIndex < (cluster.size() - currentWindowDistance); recordIndex++) {
							int recordId = cluster.getInt(recordIndex);
							int partnerRecordId = cluster.getInt(recordIndex + currentWindowDistance);
							negCover.add(this.getViolatedFds(compressedRecords[recordId], compressedRecords[partnerRecordId]));
						}
					}
	
					lastNumNewNonFds[attribute] = negCover.size() - lastNegCoverSize;
					
				//	System.out.println("\t" + attribute + ": w=" + currentWindowDistance + ", new=" + (negCover.size() - lastNegCoverSize) + ", comp=" + numComparisons + ", avg=" + ringBuffer.avg());
					
					currentWindowDistance++;
					
					if (lastNumNewNonFds[attribute] == ringBuffer.avg())
						break;
					ringBuffer.add(lastNumNewNonFds[attribute]);
				}
				while (lastNumNewNonFds[attribute] > 0);
			//	while (lastNumNewNonFds[attribute] > Math.floor(currentWindowDistance * 0.01f));
			//	while ((lastNumNewNonFds[attribute] > 0) && (currentWindowDistance <= 1000));
			//	while (lastNumNewNonFds[attribute] > (numComparisons / 100000));
				
				lastWindowDistances[attribute] = currentWindowDistance - 1;
				
				System.out.print("[" + (currentWindowDistance - 1) + "] ");
				
				List<IntArrayList> clusters = plis.get(attribute).getClusters();
				if ((clusters.size() == 1) && (clusters.get(0).size() == compressedRecords[0].length)) // If we saw a pli with only a single cluster, all following plis also have (due to previous sorting) only one cluster and we can skip them
					break;
			}
		
		/*	System.out.println();
			
			for (int i = 0; i < this.numAttributes; i++) {
				if (lastNumNewNonFds[i] == 0)
					continue;
				System.out.print(i + "[" + lastNumNewNonFds[i] + "']");
				numCandidates++;
				avg += lastNumNewNonFds[i];
			}
			
			if (numCandidates <= 1)
				break;
			avg = avg / numCandidates;
			if (avg == 0)
				break;
		*/
			Collections.sort(activeAttributes, new AttributeComparator(lastNumNewNonFds));
			int numAttributesToDiscard = activeAttributes.size() / 2;
			for (int i = 0; i < numAttributesToDiscard; i++)
				activeAttributes.remove(activeAttributes.size() - 1);
			
			System.out.println();
		}
		System.out.println();
	}
	
	private void fetchNonFdsWindowingOverClustersProgressive(NonFDTree negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.print("\tMoving window over clusters ... ");
		
		long time = System.currentTimeMillis();
		ClusterComparator comparator = new ClusterComparator(compressedRecords, compressedRecords[0].length - 1, 1);
		for (PositionListIndex pli : plis) {
			for (IntArrayList cluster : pli.getClusters()) {
				Collections.sort(cluster, comparator);
				
			//	Collections.shuffle(cluster, new Random(System.nanoTime()));
				
			//	for (int i : cluster)
			//		this.print(compressedRecords[i]);
			//	System.out.println();
			}
			comparator.incrementActiveKey();
		}
		System.out.println("Sorting (" + (System.currentTimeMillis() - time) + "ms)");
		
		for (int attribute = 0; attribute < plis.size(); attribute++) {
			System.out.print(attribute);
			
			int lastNegCoverSize = 0;
			int numComparisons = 0;
			int currentWindowDistance = 1;
			List<IntArrayList> clusters = plis.get(attribute).getClusters();
			List<IntArrayList> clustersCopy = new ArrayList<IntArrayList>(clusters);

			do {
				lastNegCoverSize = negCover.size();
				numComparisons = 0;
				
				Iterator<IntArrayList> clusterIterator = clustersCopy.iterator();
				while (clusterIterator.hasNext()) {
					IntArrayList cluster = clusterIterator.next();
					
					if (cluster.size() <= currentWindowDistance) {
						clusterIterator.remove();
						continue;
					}
					
					for (int recordIndex = 0; recordIndex < (cluster.size() - currentWindowDistance); recordIndex++) {
						int recordId = cluster.getInt(recordIndex);
						int partnerRecordId = cluster.getInt(recordIndex + currentWindowDistance);
						
						negCover.addMatches(compressedRecords[recordId], compressedRecords[partnerRecordId], this.valueComparator);
						numComparisons++;
					}
				}

		//		System.out.println("\t" + attribute + ": w=" + currentWindowDistance + ", new=" + (negCover.size() - lastNegCoverSize) + ", comp=" + numComparisons);				
				currentWindowDistance++;
			}
		//	while (negCover.size() - lastNegCoverSize > Math.floor(currentWindowDistance * 0.01f) * Math.ceil(numComparisons / 1000000.0f));
			while (negCover.size() - lastNegCoverSize > Math.floor(currentWindowDistance * 0.01f));
		//	while ((negCover.size() - lastNegCoverSize > 0) && (currentWindowDistance <= 1000));
		//	while (negCover.size() - lastNegCoverSize > (numComparisons / 100000));
		//	while (negCover.size() - lastNegCoverSize > 0);
			
			System.out.print("[" + (currentWindowDistance - 1) + "] ");
			
			if ((clusters.size() == 1) && (clusters.get(0).size() == compressedRecords[0].length)) // If we saw a pli with only a single cluster, all following plis also have (due to previous sorting) only one cluster and we can skip them
				break;
		}
		
		System.out.println();
	}
	
	private void fetchNonFdsWindowingOverClustersProgressive2(Set<OpenBitSet> negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.print("\tMoving window over clusters ... ");
		
		// If a cluster is small, compare all its records
		for (PositionListIndex pli : plis)
			for (IntArrayList cluster : pli.getClusters())
				if (cluster.size() <= this.numAttributes)
					for (int recordIndex = 0; recordIndex < cluster.size() - 1; recordIndex++)
						for (int partnerRecordIndex = recordIndex + 1; partnerRecordIndex < cluster.size(); partnerRecordIndex++)
							negCover.add(this.getViolatedFds(compressedRecords[cluster.getInt(recordIndex)], compressedRecords[cluster.getInt(partnerRecordIndex)]));
		System.out.print(" (small done) ");		
		
		// Progressive windowing on all pli-sort combinations
		for (int pliAttribute = 0; pliAttribute < this.numAttributes; pliAttribute++) {
			System.out.print(pliAttribute + "[");
			
			List<IntArrayList> clusters = plis.get(pliAttribute).getClusters();
			for (int sortAttribute = 0; sortAttribute < this.numAttributes; sortAttribute++) {
				if ((sortAttribute == pliAttribute) || (plis.get(sortAttribute).size() < 2))
					continue;

				ClusterComparator comparator = new ClusterComparator(compressedRecords, 0, sortAttribute);
				for (IntArrayList cluster : clusters)
					if (cluster.size() > this.numAttributes)
						Collections.sort(cluster, comparator);
				
			//	int numComparisons = 0;
				int currentWindowDistance = 1;
				int lastNegCoverSize = 0;
				List<IntArrayList> clustersCopy = new ArrayList<IntArrayList>(clusters);
				do {
					lastNegCoverSize = negCover.size();
			//		numComparisons = 0;
					
					Iterator<IntArrayList> clusterIterator = clustersCopy.iterator();
					while (clusterIterator.hasNext()) {
						IntArrayList cluster = clusterIterator.next();
						
						if ((cluster.size() <= currentWindowDistance) || (cluster.size() <= this.numAttributes)) {
							clusterIterator.remove();
							continue;
						}
						
						for (int recordIndex = 0; recordIndex < (cluster.size() - currentWindowDistance); recordIndex++) {
							int[] record = compressedRecords[cluster.getInt(recordIndex)];
							int[] partnerRecord = compressedRecords[cluster.getInt(recordIndex + currentWindowDistance)];
							if ((record[sortAttribute] == partnerRecord[sortAttribute]) && (record[sortAttribute] != -1))
								negCover.add(this.getViolatedFds(record, partnerRecord));
			//				numComparisons++;
						}
					}
					
					currentWindowDistance++;
				}
				while (negCover.size() - lastNegCoverSize > Math.floor(currentWindowDistance * 0.01f));
				
				System.out.print((currentWindowDistance - 1) + "|");
			}
			
			System.out.print("] ");
			
			if ((clusters.size() == 1) && (clusters.get(0).size() == compressedRecords[0].length)) // If we saw a pli with only a single cluster, all following plis also have (due to previous sorting) only one cluster and we can skip them
				break;
		}
		
		System.out.println();
	}

	private void fetchNonFdsFromClustersTopsAndBottomsProgressive(Set<OpenBitSet> negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.println("\tComparing window on clusters tops and bottoms ...");
		for (PositionListIndex pli : plis) {
			int currentWindowDistance = 1;
			int newNonFDs = 0;
			do {
				newNonFDs = 0;
				
				for (IntArrayList cluster : pli.getClusters()) {
					int recordIndex = currentWindowDistance;
					int partnerRecordIndex = cluster.size() - currentWindowDistance; 
					
					if ((recordIndex >= cluster.size()) || (partnerRecordIndex < 0) || (recordIndex == partnerRecordIndex))
						continue;
					
					int recordId = cluster.getInt(recordIndex);
					int partnerRecordId = cluster.getInt(partnerRecordIndex);
					
					if (negCover.add(this.getViolatedFds(compressedRecords[recordId], compressedRecords[partnerRecordId])))
						newNonFDs++;
				}
				
				currentWindowDistance++;
			}
			while (newNonFDs > this.progressiveThreshold);
		}
	}

	private void fetchNonFdsWindowingOverRecordsProgressive(Set<OpenBitSet> negCover, int[][] compressedRecords) {
		System.out.println("\tMoving window over records ...");
		int numRecords = compressedRecords.length;
		int currentWindowDistance = 1;
		int newNonFDs = 0;
		do {
			newNonFDs = 0;
			
			for (int recordId = 0; recordId < numRecords; recordId++) {
				int partnerRecordId = recordId + currentWindowDistance; 
				
				if (partnerRecordId >= numRecords)
					continue;
					
				if (negCover.add(this.getViolatedFds(compressedRecords[recordId], compressedRecords[partnerRecordId])))
					newNonFDs++;
			}
			
			currentWindowDistance++;
		}
		while (newNonFDs > this.progressiveThreshold);
	}

	private void fetchNonFdsWindowingOverRecords(Set<OpenBitSet> negCover, int[][] compressedRecords) {
		System.out.println("\tMoving window over records ...");
		int numRecords = compressedRecords.length;
		for (int recordId = 0; recordId < numRecords; recordId++) {
			for (int partnerRecordId = recordId + 1; partnerRecordId < Math.min(recordId + this.windowSize, numRecords); partnerRecordId++) {
				negCover.add(this.getViolatedFds(compressedRecords[recordId], compressedRecords[partnerRecordId]));
			}
		}
	}

	private void fetchNonFdsWindowingOverClusters(Set<OpenBitSet> negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.println("\tMoving window over small clusters ...");
		for (PositionListIndex pli : plis) {
			boolean selectSmallClustersOnly = pli.getClusters().size() < this.attributeThreshold; 	// If there are too few clusters, then the clusters are large and we have already executed sufficient comparisons between the records of these clusters
			
			for (IntArrayList cluster : pli.getClusters()) {
				if (selectSmallClustersOnly && (cluster.size() > this.windowSize))					// But if the current cluster is very small, we should still use it for comparisons (the other cluster(s) must be very large)
					continue;
				
				for (int recordIndex = 0; recordIndex < cluster.size(); recordIndex++) {
					int recordId = cluster.getInt(recordIndex);
					
					for (int partnerRecordIndex = recordIndex + 1; partnerRecordIndex < Math.min(recordIndex + this.windowSize, cluster.size()); partnerRecordIndex++) {
						int partnerRecordId = cluster.getInt(partnerRecordIndex);
						
						negCover.add(this.getViolatedFds(compressedRecords[recordId], compressedRecords[partnerRecordId]));
					}
				}
			}
		}
	}

	private void fetchNonFdsFromClustersTopsAndBottoms(Set<OpenBitSet> negCover, int[][] compressedRecords, List<PositionListIndex> plis) {
		System.out.println("\tComparing window on clusters tops and bottoms ...");
		for (PositionListIndex pli : plis) {
			for (IntArrayList cluster : pli.getClusters()) {
				if (cluster.size() < this.windowSize)
					continue;
				
				for (int recordIndex = 0; recordIndex < this.windowSize; recordIndex++) {
					int recordId = cluster.getInt(recordIndex);
					
					for (int partnerRecordIndex = cluster.size() - 1; partnerRecordIndex > cluster.size() - this.windowSize; partnerRecordIndex--) {
						int partnerRecordId = cluster.getInt(partnerRecordIndex);
						
						if (recordId == partnerRecordId)
							continue;
						
						negCover.add(this.getViolatedFds(compressedRecords[recordId], compressedRecords[partnerRecordId]));
					}
				}
			}
		}
	}

/*	private void fetchSampleRecordsWindowingForEachRecord(IntOpenHashSet[] recordPairs, List<PositionListIndex> plis) {//ArrayList<IntArrayList> recordIds2clusterIds, ArrayList<IntArrayList> clusterIds2recordIds, int numRecords) {
		// For each record, select partner records from the records neighborhood in all its clusters
		System.out.println("Fetch negative cover records from the records' cluster neighborhoods ...");
		for (PositionListIndex pli : plis) {
			int maxPivotsFromEachCluster = (int)Math.ceil((float) this.maxPivotsFromEachAttribute / pli.getClusters().size());
			for (IntArrayList cluster : pli.getClusters()) {
				for (int recordIndex = 0; recordIndex < Math.min(maxPivotsFromEachCluster, cluster.size()); recordIndex++) {
					for (int partnerRecordIndex = recordIndex + 1; partnerRecordIndex < Math.min(recordIndex + this.windowSize, cluster.size()); partnerRecordIndex++) {
						recordPairs[cluster.getInt(recordIndex)].add(cluster.getInt(partnerRecordIndex));
					}
				}
			}
		}
	}

	private void fetchSampleRecordsWindowingForEachRecord2(IntOpenHashSet[] recordPairs, List<PositionListIndex> plis) {//ArrayList<IntArrayList> recordIds2clusterIds, ArrayList<IntArrayList> clusterIds2recordIds, int numRecords) {
		// For each record, select partner records from the records neighborhood in all its clusters
		System.out.println("Fetch negative cover records from the records' cluster neighborhoods ...");
		for (PositionListIndex pli : plis) {
			for (IntArrayList cluster : pli.getClusters()) {
				for (int recordIndex = 0; recordIndex < Math.min(this.maxPivotsFromEachCluster, cluster.size()); recordIndex++) {
					for (int partnerRecordIndex = recordIndex + 1; partnerRecordIndex < Math.min(recordIndex + this.windowSize, cluster.size()); partnerRecordIndex++) {
						recordPairs[cluster.getInt(recordIndex)].add(cluster.getInt(partnerRecordIndex));
					}
				}
			}
		}
	}

	private void fetchSampleRecordsWindowingForEachRecord3(IntOpenHashSet[] recordPairs, List<PositionListIndex> plis) {//ArrayList<IntArrayList> recordIds2clusterIds, ArrayList<IntArrayList> clusterIds2recordIds, int numRecords) {
		// For each record, select partner records from the records neighborhood in all its clusters
		System.out.println("Fetch negative cover records from the records' cluster neighborhoods ...");
		for (PositionListIndex pli : plis) {
			for (IntArrayList cluster : pli.getClusters()) {
				for (int recordIndex = 0; recordIndex < cluster.size(); recordIndex++) {
					for (int partnerRecordIndex = recordIndex + 1; partnerRecordIndex < Math.min(recordIndex + this.windowSize, cluster.size()); partnerRecordIndex++) {
						recordPairs[cluster.getInt(recordIndex)].add(cluster.getInt(partnerRecordIndex));
					}
				}
			}
		}
	}
	
	private void fetchSampleRecordsTops(IntOpenHashSet[] recordPairs, List<PositionListIndex> plis, int[][] compressedRecords, int numRecords) {
		System.out.println("Fetch negative cover records from records with most clusters ...");

		System.out.println("\t- counting clusters per record ...");
		int[] clusterCounts = new int[numRecords];
		for (int i = 0; i < numRecords; i++)
			clusterCounts[i] = 0;
		for (PositionListIndex pli : plis) {
			// Skip plis in which all records belong to some cluster
			if (pli.getNumNonUniqueValues() == numRecords)
				continue;
			
			// Increment all cluster counts for records in this pli
			for (IntArrayList cluster : pli.getClusters())
				for (int recordId : cluster)
					clusterCounts[recordId] = clusterCounts[recordId] + 1;
		}
		
		System.out.println("\t- sorting records by cluster counts ...");
		List<RecordValuePair> recordCountPairs = new ArrayList<>(numRecords);
		for (int recordId = 0; recordId < numRecords; recordId++)
			if (clusterCounts[recordId] != 0)
				recordCountPairs.add(new RecordValuePair(recordId, clusterCounts[recordId]));
		clusterCounts = null;
		Collections.sort(recordCountPairs);
		
		System.out.println("\t- fetching some top records and adding some partner records with largest overlap ...");
		int maxTopRecords = 1000;
		int maxSamplesPerCluster = 10;
		int maxPartnerRecordsPerTopRecord = 10;
		IntOpenHashSet handledRecordIds = new IntOpenHashSet(maxTopRecords * maxPartnerRecordsPerTopRecord);
		for (int topRecordNumber = 0; topRecordNumber < maxTopRecords; topRecordNumber++) {
			int topRecordId = recordCountPairs.get(topRecordNumber).recordId;
			
			if (!handledRecordIds.add(topRecordId))
				continue;
			
			// Collect possible partner records from the top records clusters
			IntOpenHashSet possiblePartnerRecordIds = new IntOpenHashSet(this.numAttributes * maxSamplesPerCluster);
			for (int attributeId = 0; attributeId < this.numAttributes; attributeId++) {
				int clusterId = compressedRecords[topRecordId][attributeId];
				IntArrayList cluster = plis.get(attributeId).getClusters().get(clusterId);
				
				int recordsTakenFromCluster = 0;
				int currentRecordIndex = cluster.size() - 1;
				while ((recordsTakenFromCluster < maxSamplesPerCluster) && (currentRecordIndex >= 0)) {
					int possiblePartnerRecordId = cluster.getInt(currentRecordIndex);
					currentRecordIndex--;
					
					if (!handledRecordIds.add(possiblePartnerRecordId))
						continue;
					
					possiblePartnerRecordIds.add(possiblePartnerRecordId);
				}
			}
			
			// Count common clusters with top record and sort by this number
			List<RecordValuePair> possiblePartnerRecordMatchPairs = new ArrayList<RecordValuePair>(possiblePartnerRecordIds.size());
			for (int recordId : possiblePartnerRecordIds) {
				int matches = 0;
				for (int attributeId = 0; attributeId < this.numAttributes; attributeId++)
					if (compressedRecords[topRecordId][attributeId] == compressedRecords[recordId][attributeId])
						matches++;
				if (matches > 1)
					possiblePartnerRecordMatchPairs.add(new RecordValuePair(recordId, matches));
			}
			possiblePartnerRecordIds = null;
			Collections.sort(possiblePartnerRecordMatchPairs);
			
			// Add the records with most overlap with the top record as new record pairs
			for (int partnerRecordNumber = 0; partnerRecordNumber < maxPartnerRecordsPerTopRecord; partnerRecordNumber++)
				recordPairs[topRecordId].add(possiblePartnerRecordMatchPairs.get(partnerRecordNumber).recordId);
		}
	}
*/	
	protected class RecordValuePair implements Comparable<RecordValuePair> {
		public int recordId;
		public int value;
		public RecordValuePair(int recordId, int value) {
			this.recordId = recordId;
			this.value = value;
		}
		@Override
		public int hashCode() {
			return this.recordId;
		}
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RecordValuePair))
				return false;
			RecordValuePair other = (RecordValuePair) obj;
			return (this.recordId == other.recordId) && (this.value == other.value);
		}
		@Override
		public String toString() {
			return "RecordValuePair(" + this.recordId + "," + this.value + ")";
		}
		@Override
		public int compareTo(RecordValuePair o) {
			if (this.value == o.value)
				return o.recordId - this.recordId;
			return o.value - this.value;
		}
	}
	
/*	private void fetchSampleRecordsTopsFromSomeAttributePairClusters(IntOpenHashSet[] recordPairs, Int2ObjectMap<Int2ObjectMap<PositionListIndex>> intersectedPlis, int numRecords) {
		System.out.println("Fetch negative cover records from (attr1,attr2) samples ...");
		
		System.out.println("\t- counting clusters per record ...");
		int[] clusterCounts = new int[numRecords];
		for (int i = 0; i < numRecords; i++)
			clusterCounts[i] = 0;
		for (Int2ObjectMap<PositionListIndex> plis : intersectedPlis.values()) // TODO: this must go over all unary plis, not over the intersected plis!
			for (PositionListIndex pli : plis.values())
				for (IntArrayList cluster : pli.getClusters())
					for (int recordId : cluster)
						clusterCounts[recordId] = clusterCounts[recordId] + 1;
		
		System.out.println("\t- choosing candidates ...");
		for (int attr1 = 0; attr1 < this.numAttributes; attr1++) {
			for (int attr2 = 0; attr2 < this.numAttributes; attr2++) { 
				PositionListIndex intersectedPli = intersectedPlis.get(attr1).get(attr2);
				if (intersectedPli == null)
					continue;
				
				int maxClusters = 10;
				for (int clusterIndex = 0; clusterIndex < Math.min(maxClusters, intersectedPli.getClusters().size()); clusterIndex++) {
					IntArrayList cluster = intersectedPli.getClusters().get(clusterIndex);
					List<RecordValuePair> recordCountPairs = new ArrayList<>(cluster.size());
					for (int recordId : cluster)
						recordCountPairs.add(new RecordValuePair(recordId, clusterCounts[recordId]));
					Collections.sort(recordCountPairs);
					
					for (int recordIndex = 0; recordIndex < Math.min(this.topSize, recordCountPairs.size()); recordIndex++) {
						for (int partnerRecordIndex = recordIndex + 1; partnerRecordIndex < Math.min(recordIndex + this.windowSize, recordCountPairs.size()); partnerRecordIndex++) {
							recordPairs[recordCountPairs.get(recordIndex).recordId].add(recordCountPairs.get(partnerRecordIndex).recordId);
						}
					}
					
					int bottomRecords = Math.max(recordCountPairs.size() - this.topSize, 0);
					for (int recordIndex = recordCountPairs.size() - Math.min(this.bottomSize, bottomRecords); recordIndex < recordCountPairs.size(); recordIndex++) {
						for (int partnerRecordIndex = recordIndex + 1; partnerRecordIndex < Math.min(recordIndex + this.windowSize, recordCountPairs.size()); partnerRecordIndex++) {
							recordPairs[recordCountPairs.get(recordIndex).recordId].add(recordCountPairs.get(partnerRecordIndex).recordId);
						}
					}
				}
			}
		}
	}
	
	private void fetchSampleRecordsTopsFromEachAttributePair(IntOpenHashSet[] recordPairs, Int2ObjectMap<Int2ObjectMap<PositionListIndex>> intersectedPlis, int numRecords) {
		// Sort all records for each (attr1,attr2) attribute pair by the number of clusters they occur in, select (globally) the top records per pair and move window through their respective clusters
	}
	
	private void fetchSampleRecordsTopsFromEachAttributePair_OLD_VERSION(IntOpenHashSet[] recordPairs, List<PositionListIndex> plis, Int2ObjectMap<Int2ObjectMap<PositionListIndex>> intersectedPlis, int numRecords) {
		// Build two indexes recordIds2clusterIds and clusterIds2recordIds to choose the sample records from
		System.out.println("Building recordIds <-> clusterIds maps ...");
		ArrayList<IntArrayList> recordIds2clusterIds = new ArrayList<>(numRecords); // TODO: An array might be more performant
		ArrayList<IntArrayList> clusterIds2recordIds = new ArrayList<>(this.numAttributes * 64); // TODO: Can we predict the size better?
		
		for (int recordId = 0; recordId < numRecords; recordId++)
			recordIds2clusterIds.add(new IntArrayList(4)); // TODO: predict size better?
		
		int clusterId = 0;
		for (int attribute = 0; attribute < this.numAttributes; attribute++) {
			for (IntArrayList cluster : plis.get(attribute).getClusters()) {
				clusterIds2recordIds.add(cluster);
				for (int recordId : cluster)
					recordIds2clusterIds.get(recordId).add(clusterId);
				clusterId++;
			}
		}
		
		// For each pair of attributes, choose the records in their clusters that occur in most and least clusters, then select partner records from these records neighborhoods in all its clusters
		// For each combination of two attributes, find the ones with the largest/least overlap (use pairs of attributes to increase the variety of lhs attributes) --> This operation is quadratic in the number of attributes, but the solution space is exponential so that this is no issue
		// Why do we need to focus on large lhs? - Because they prune many smaller candidates especially if they are located higher than numAttr/2; we found most small lhs in the previous sampling anyway
		System.out.println("Fetch negative cover records from (attr1,attr2) samples ...");
		for (int attr1 = 0; attr1 < this.numAttributes; attr1++) {
			for (int attr2 = 0; attr2 < this.numAttributes; attr2++) { 
				PositionListIndex intersectedPli = intersectedPlis.get(attr1).get(attr2);
				if (intersectedPli == null)
					continue;
				
				// Sort the records in the pli by the number of clusters they occur in
				ObjectArrayList<RecordDescriptor> recordDescriptors = new ObjectArrayList<>(intersectedPli.getNumNonUniqueValues());
				for (clusterId = 0; clusterId < intersectedPli.getClusters().size(); clusterId++)
					for (int recordId : intersectedPli.getClusters().get(clusterId))
						recordDescriptors.add(new RecordDescriptor(recordId, clusterId, recordIds2clusterIds.get(recordId).size()));
				Collections.sort(recordDescriptors);
				
				// Take the top X records with most and the top Y records with least clusters for attribute combination (attr1,attr2) and compare these records with some of their partner records
				int numTopRecords = Math.min(recordDescriptors.size(), this.topSize);
				int numBottomRecords = Math.min(recordDescriptors.size(), this.bottomSize);
				HashSet<RecordDescriptor> topRecordDescriptors = new HashSet<>(numTopRecords + numBottomRecords);
				for (int recordIndex = 0; recordIndex < numTopRecords; recordIndex++)
					topRecordDescriptors.add(recordDescriptors.get(recordIndex));
				for (int recordIndex = recordDescriptors.size() - numBottomRecords; recordIndex < recordDescriptors.size(); recordIndex++)
					topRecordDescriptors.add(recordDescriptors.get(recordIndex));
				
				// Choose partner records for the top records from their cluster in the current attribute pair
				for (RecordDescriptor topRecordDescriptor : topRecordDescriptors) {
					int topRecordId = topRecordDescriptor.record;
					int topRecordClusterId = topRecordDescriptor.cluster;
					
					IntArrayList topRecordCluster = intersectedPli.getClusters().get(topRecordClusterId);
					// TODO: Select the partner records more focused, i.e., take some with large lhs and some with small lhs, also consider that the lhs of partner and top record overlap --> all this might be too complicated and since we need any sizes, random records might just be fine
					
					int topRecordIndex = topRecordCluster.indexOf(topRecordId);
					int lastPartnerRecordIndex = Math.min(topRecordIndex + this.windowSize, topRecordCluster.size() - 1);
					for (int partnerRecordIndex = topRecordIndex + 1; partnerRecordIndex <= lastPartnerRecordIndex; partnerRecordIndex++)
						recordPairs[topRecordId].add(topRecordCluster.getInt(partnerRecordIndex));
				}
			}
		}
	}
*/	
	private OpenBitSet getViolatedFds(int[] t1, int[] t2) {
		OpenBitSet equalAttrs = new OpenBitSet(t1.length);
		for (int i = 0; i < t1.length; i++)
			if (this.valueComparator.isEqual(t1[i], t2[i]))
				equalAttrs.set(i);
		return equalAttrs;
	}
	
	private FDTree calculatePositiveCover(ArrayList<OpenBitSet> negCover) {
		FDTree posCover = new FDTree(this.numAttributes, this.maxLhsSize);
		posCover.addMostGeneralDependencies();
		
		//
		//int bitsetCounter = 0;
		//long t = System.currentTimeMillis();
		//
		
//		OpenBitSet previous1Lhs = null;
//		OpenBitSet previous2Lhs = null;
		for (int i = negCover.size() - 1; i >= 0; i--) {
			OpenBitSet lhs = negCover.remove(i);

			//
			//bitsetCounter++;
			//if (bitsetCounter % 1 == 0) {
			//	System.out.println("\t\t" + bitsetCounter + "\\" + negCover.size() + " bitsets; " + "- fds; " + (System.currentTimeMillis() - t) + " time");
			//	t = System.currentTimeMillis();
			//}
			//
			
			OpenBitSet fullRhs = lhs.clone();
			fullRhs.flip(0, fullRhs.size());
			
			for (int rhs = fullRhs.nextSetBit(0); rhs >= 0; rhs = fullRhs.nextSetBit(rhs + 1)) {
				// If one of the previous lhs subsumes this lhs with the same rhs, then we can skip it here
//				if ((previous1Lhs != null) && this.subsumes(lhs, previous1Lhs, rhs))
//					continue;
//				if ((previous2Lhs != null) && this.subsumes(lhs, previous2Lhs, rhs))
//					continue;
				
				this.memoryGuardian.memoryChanged(this.specializePositiveCover(posCover, lhs, rhs));
			}
			
			// If dynamic memory management is enabled, frequently check the memory consumption and trim the positive cover if it does not fit anymore
		//	this.memoryGuardian.match(posCover);
			
//			previous2Lhs = previous1Lhs;
//			previous1Lhs = lhs;
		}
		return posCover;
	}
	
/*	private boolean subsumes(OpenBitSet subLhs, OpenBitSet superLhs, int rhs) {
		if (superLhs.get(rhs))
			return false;
		
		for (int i = subLhs.nextSetBit(0); i >= 0; i = subLhs.nextSetBit(i + 1))
			if (!superLhs.get(i))
				return false;
		return true;
	}
*/
	protected int specializePositiveCover(FDTree posCoverTree, OpenBitSet lhs, int rhs) {
		int newFDs = 0;
		List<OpenBitSet> specLhss = posCoverTree.getFdAndGeneralizations(lhs, rhs);
		for (OpenBitSet specLhs : specLhss) {
			posCoverTree.removeFunctionalDependency(specLhs, rhs);
			
			if (specLhs.cardinality() == posCoverTree.getMaxDepth())
				continue;
			
			for (int attr = this.numAttributes - 1; attr >= 0; attr--) { // TODO: Is iterating backwards a good or bad idea?
				if (!lhs.get(attr) && (attr != rhs)) {
					specLhs.set(attr);
					if (!posCoverTree.containsFdOrGeneralization(specLhs, rhs)) {
						posCoverTree.addFunctionalDependency(specLhs, rhs);
						newFDs++;					
					}
					specLhs.clear(attr);
				}
			}
		}
		return newFDs;
	}
/*
	private List<List<OpenBitSet>> calculatePositiveCoverForest(List<OpenBitSet> negCover) {
		List<List<OpenBitSet>> posCover = new ArrayList<List<OpenBitSet>>(this.numAttributes);
		for (int rhs = 0; rhs < this.numAttributes; rhs++) {
			LhsTrie posCoverTrie = new LhsTrie(this.numAttributes);
			
			for (OpenBitSet lhs : negCover) {
				if (lhs.get(rhs))
					continue;
				
				this.specializePositiveCover(posCoverTrie, lhs, rhs);
			}
			
			List<OpenBitSet> posCoverBitSets = posCoverTrie.asBitSetList();
			Collections.sort(posCoverBitSets, new Comparator<OpenBitSet>() {
				@Override
				public int compare(OpenBitSet o1, OpenBitSet o2) {		
					if (o1.cardinality() == o2.cardinality()) {
						for (int i = 0; i < o1.length(); i++) {
							if (o1.get(i) && !o2.get(i))
								return 1;
							if (o2.get(i) && !o1.get(i))
								return -1;
						}
						return 0;
					}
					
					return (int)(o1.cardinality() - o2.cardinality());
				}
			});
			posCover.add(posCoverBitSets);
		}
		
		// TODO
		
		return posCover;
	}

	//public static int fdCounter = 0;
	private void specializePositiveCover(LhsTrie posCoverTrie, OpenBitSet lhs, int rhs) {
		List<OpenBitSet> specLhss = null;
		specLhss = posCoverTrie.getLhsAndGeneralizations(lhs);
		for (OpenBitSet specLhs : specLhss) {
			posCoverTrie.removeLhs(specLhs);
			
		//	if (specLhs.cardinality() == this.maxLhsSize)
		//		continue;
			
			//fdCounter--;			
			for (int attr = this.numAttributes - 1; attr >= 0; attr--) { // TODO: Is iterating backwards a good or bad idea?
				if (!lhs.get(attr) && (attr != rhs)) {
					specLhs.set(attr);
					if (!posCoverTrie.containsLhsOrGeneralization(specLhs)) {
						posCoverTrie.addLhs(specLhs);
						//fdCounter++;						
					}
					specLhs.clear(attr);
				}
			}
		}
	}
*/
	private RelationalInput getInput() throws InputGenerationException, AlgorithmConfigurationException {
		RelationalInput relationalInput = this.inputGenerator.generateNewCopy();
		if (relationalInput == null)
			throw new InputGenerationException("Input generation failed!");
		return relationalInput;
	}
	
	private void closeInput(RelationalInput relationalInput) {
		FileUtils.close(relationalInput);
	}

	private ObjectArrayList<ColumnIdentifier> buildColumnIdentifiers() {
		ObjectArrayList<ColumnIdentifier> columnIdentifiers = new ObjectArrayList<ColumnIdentifier>(this.attributeNames.size());
		for (String attributeName : this.attributeNames)
			columnIdentifiers.add(new ColumnIdentifier(this.tableName, attributeName));
		return columnIdentifiers;
	}

	private ObjectArrayList<List<String>> loadData(RelationalInput relationalInput) throws InputIterationException {
		ObjectArrayList<List<String>> records = new ObjectArrayList<List<String>>();
		while (relationalInput.hasNext())
			records.add(relationalInput.next());
		return records;
	}

	private int[][] invertPlis(List<PositionListIndex> plis, int numRecords) {
		int[][] invertedPlis = new int[plis.size()][];
		for (int attr = 0; attr < plis.size(); attr++) {
			int[] invertedPli = new int[numRecords];
			Arrays.fill(invertedPli, -1);
			
			for (int clusterId = 0; clusterId < plis.get(attr).size(); clusterId++) {
				for (int recordId : plis.get(attr).getClusters().get(clusterId))
					invertedPli[recordId] = clusterId;
			}
			invertedPlis[attr] = invertedPli;
		}
		return invertedPlis;
	}
	
	protected class RecordDescriptor implements Comparable<RecordDescriptor> {
		public int record, cluster, count;
		public RecordDescriptor(int record, int cluster, int count) {
			this.record = record;
			this.cluster = cluster;
			this.count = count;
		}
		@Override
		public int hashCode() {
			return this.record;
		}
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RecordDescriptor))
				return false;
			RecordDescriptor other = (RecordDescriptor) obj;
			return this.count == other.count;
		}
		@Override
		public String toString() {
			return "[" + this.record + "," + this.cluster + "," + this.count + "]";
		}
		@Override
		public int compareTo(RecordDescriptor o) {
			return o.count - this.count;
		}
	}
	
	private int[] fetchRecordFrom(int recordId, int[][] invertedPlis) {
		int[] record = new int[this.numAttributes];
		for (int i = 0; i < this.numAttributes; i++)
			record[i] = invertedPlis[i][recordId];
		return record;
	}
}

