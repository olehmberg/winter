package de.metanome.algorithms.hyfd.fixtures;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

public abstract class AbstractAlgorithmTestFixture {
	
	protected ImmutableList<String> columnNames;
	protected int numberOfColumns;
	protected String relationName = "R";
	protected List<ImmutableList<String>> table = new LinkedList<ImmutableList<String>>();
	protected FunctionalDependencyResultReceiver fdResultReceiver = mock(FunctionalDependencyResultReceiver.class);
	
	public RelationalInputGenerator getInputGenerator() throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
		RelationalInputGenerator inputGenerator = mock(RelationalInputGenerator.class);
		RelationalInput input = this.getRelationalInput();
		when(inputGenerator.generateNewCopy()).thenReturn(input);
		return inputGenerator;
	}

	public FunctionalDependencyResultReceiver getFunctionalDependencyResultReceiver() {
		return this.fdResultReceiver;
	}
	
	public abstract RelationalInput getRelationalInput() throws InputIterationException;
	public abstract void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException, ColumnNameMismatchException;	
}
