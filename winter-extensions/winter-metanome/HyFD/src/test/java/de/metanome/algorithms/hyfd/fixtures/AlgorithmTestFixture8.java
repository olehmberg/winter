package de.metanome.algorithms.hyfd.fixtures;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.FunctionalDependency;

public class AlgorithmTestFixture8 extends AbstractAlgorithmTestFixture {
	
	public AlgorithmTestFixture8() throws CouldNotReceiveResultException {
		this.columnNames = ImmutableList.of("A", "B", "C", "D");
		this.numberOfColumns = 4;
		this.table.add(ImmutableList.of("197", "145", "101", "171"));
		this.table.add(ImmutableList.of("57", "70", "145", "112"));
		this.table.add(ImmutableList.of("197", "70", "130", "39"));
		this.table.add(ImmutableList.of("122", "10", "137", "15"));
		this.table.add(ImmutableList.of("113", "72", "18", "124"));
	}
	
	public RelationalInput getRelationalInput() throws InputIterationException {
		RelationalInput input = mock(RelationalInput.class);
		
		when(input.columnNames())
			.thenReturn(this.columnNames);
		when(Integer.valueOf(input.numberOfColumns()))
			.thenReturn(Integer.valueOf(this.numberOfColumns));
		when(input.relationName())
			.thenReturn(this.relationName);
		
		when(Boolean.valueOf(input.hasNext()))
			.thenReturn(Boolean.valueOf(true))
			.thenReturn(Boolean.valueOf(true))
			.thenReturn(Boolean.valueOf(true))
			.thenReturn(Boolean.valueOf(true))
			.thenReturn(Boolean.valueOf(true))
			.thenReturn(Boolean.valueOf(false));
		
		when(input.next())
			.thenReturn(this.table.get(0))
			.thenReturn(this.table.get(1))
			.thenReturn(this.table.get(2))
			.thenReturn(this.table.get(3))
			.thenReturn(this.table.get(4));
			
		return input;
	}
	
	public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException, ColumnNameMismatchException {
		ColumnIdentifier expectedIdentifierA = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
		ColumnIdentifier expectedIdentifierB = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
		ColumnIdentifier expectedIdentifierC = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
		ColumnIdentifier expectedIdentifierD = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
		
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC), expectedIdentifierA));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC), expectedIdentifierB));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC), expectedIdentifierD));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierA));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierB));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD), expectedIdentifierC));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierB), expectedIdentifierC));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA, expectedIdentifierB), expectedIdentifierD));
		
		verifyNoMoreInteractions(this.fdResultReceiver);
	}
	
}
