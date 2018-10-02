package de.metanome.algorithms.hyfd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.hyfd.HyFD;
import de.metanome.algorithms.hyfd.fixtures.AbaloneFixture;
import de.metanome.algorithms.hyfd.fixtures.AbstractAlgorithmTestFixture;
import de.metanome.algorithms.hyfd.fixtures.AlgorithmTestFixture;
import de.metanome.algorithms.hyfd.fixtures.BridgesFixture;
import de.uni_potsdam.hpi.utils.FileUtils;

public class HyFDTest extends FDAlgorithmTest {
	
	private String tempFolderPath = "io" + File.separator + "temp_junit" + File.separator;
	private boolean nullEqualsNull = true;
	
	@Before
	public void setUp() throws Exception {
		this.algo = new HyFD();
	}

	@After
	public void tearDown() throws Exception {
		// Clean temp if there are files from previous runs that may pollute this run
		FileUtils.cleanDirectory(new File(this.tempFolderPath));
	}
	
	protected void executeAndVerifyWithFixture(AbstractAlgorithmTestFixture fixture) throws AlgorithmExecutionException {
		HyFD hyFD = (HyFD) this.algo;
		hyFD.setRelationalInputConfigurationValue(HyFD.Identifier.INPUT_GENERATOR.name(), fixture.getInputGenerator());
		hyFD.setBooleanConfigurationValue(HyFD.Identifier.NULL_EQUALS_NULL.name(), this.nullEqualsNull );
		hyFD.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
        
		// Execute algorithm
		hyFD.execute();
        
        // Check results
        fixture.verifyFunctionalDependencyResultReceiver();
	}

	protected void executeAndVerifyWithFixture(AbaloneFixture fixture) throws AlgorithmExecutionException, UnsupportedEncodingException, FileNotFoundException {
		HyFD hyFD = (HyFD) this.algo;
		hyFD.setRelationalInputConfigurationValue(HyFD.Identifier.INPUT_GENERATOR.name(), fixture.getInputGenerator());
		hyFD.setBooleanConfigurationValue(HyFD.Identifier.NULL_EQUALS_NULL.name(), this.nullEqualsNull );
		hyFD.setResultReceiver(fixture.getFdResultReceiver());
		
		// Execute functionality
		hyFD.execute();

        // Check Results
        fixture.verifyFdResultReceiver();
	}

	protected void executeAndVerifyWithFixture(BridgesFixture fixture) throws AlgorithmExecutionException, UnsupportedEncodingException, FileNotFoundException {
		HyFD hyFD = (HyFD) this.algo;
		hyFD.setRelationalInputConfigurationValue(HyFD.Identifier.INPUT_GENERATOR.name(), fixture.getInputGenerator());
		hyFD.setBooleanConfigurationValue(HyFD.Identifier.NULL_EQUALS_NULL.name(), this.nullEqualsNull );
		hyFD.setResultReceiver(fixture.getFdResultReceiver());
		
		// Execute functionality
		hyFD.execute();

        // Check Results
        fixture.verifyFunctionalDependencyResultReceiver();
	}

	protected void executeAndVerifyWithFixture(AlgorithmTestFixture fixture) throws AlgorithmExecutionException {
		HyFD hyFD = (HyFD) this.algo;
		hyFD.setRelationalInputConfigurationValue(HyFD.Identifier.INPUT_GENERATOR.name(), fixture.getInputGenerator());
		hyFD.setBooleanConfigurationValue(HyFD.Identifier.NULL_EQUALS_NULL.name(), this.nullEqualsNull );
		hyFD.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
		
		// Execute functionality
		hyFD.execute();

        // Check Results
        fixture.verifyFunctionalDependencyResultReceiver();
	}
}
