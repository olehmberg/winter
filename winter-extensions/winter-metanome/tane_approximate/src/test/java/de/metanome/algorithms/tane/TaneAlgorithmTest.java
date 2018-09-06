// package de.metanome.algorithms.tane;

// import de.metanome.algorithm_integration.AlgorithmExecutionException;
// import de.metanome.algorithms.tane.algorithm_helper.test_helper.AlgorithmTester;
// import de.metanome.algorithms.tane.algorithm_helper.test_helper.fixtures.AbstractAlgorithmTestFixture;
// import org.junit.After;
// import org.junit.Before;

// public class TaneAlgorithmTest extends AlgorithmTester {
//     private TaneAlgorithm algo;

//     @Before
//     public void setUp() throws Exception {
//         this.algo = new TaneAlgorithm();
//     }

//     @After
//     public void tearDown() throws Exception {
//     }

//     protected void executeAndVerifyWithFixture(AbstractAlgorithmTestFixture fixture)
//             throws AlgorithmExecutionException {
//         this.algo.setRelationalInputConfigurationValue(TaneAlgorithm.INPUT_TAG, fixture.getInputGenerator());
//         this.algo.setResultReceiver(fixture.getFunctionalDependencyResultReceiver());
//         // Execute functionality
//         this.algo.execute();

//         // Check Results
//         fixture.verifyFunctionalDependencyResultReceiver();
//     }

// //	@Test
// //	public void testGetConfigurationRequirements() {
// //		fail("Not yet implemented");
// //	}
// //
// //	@Test
// //	public void testSetConfigurationValueStringString() {
// //		fail("Not yet implemented");
// //	}
// //
// //	@Test
// //	public void testSetConfigurationValueStringBoolean() {
// //		fail("Not yet implemented");
// //	}
// //
// //	@Test
// //	public void testSetConfigurationValueStringSimpleRelationalInputGenerator() {
// //		fail("Not yet implemented");
// //	}
// //
// //	@Test
// //	public void testStart() {
// //		fail("Not yet implemented");
// //	}
// //
// //	@Test
// //	public void testSetResultReceiverFunctionalDependencyResultReceiver() {
// //		fail("Not yet implemented");
// //	}
// //
// //	@Test
// //	public void testSetResultReceiverUniqueColumnCombinationResultReceiver() {
// //		fail("Not yet implemented");
// //	}
// //	
// //	@Test
// //	public void testStrippedPartitionProduct() {
// //		TaneAlgorithm ta = new TaneAlgorithm();
// //		
// //	}

// }
