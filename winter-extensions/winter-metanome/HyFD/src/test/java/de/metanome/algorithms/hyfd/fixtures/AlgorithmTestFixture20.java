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

public class AlgorithmTestFixture20 extends AbstractAlgorithmTestFixture {

	public AlgorithmTestFixture20()  throws CouldNotReceiveResultException {
		this.columnNames = ImmutableList.of("A", "B", "C", "D", "E", "F");
		this.numberOfColumns = this.columnNames.size();
		this.table.add(ImmutableList.of("a", "a", "b", "c", "a", "c"));
		this.table.add(ImmutableList.of("b", "c", "a", "d", "a", "c"));
		this.table.add(ImmutableList.of("c", "a", "c", "b", "c", "d"));
		this.table.add(ImmutableList.of("d", "d", "c", "d", "b", "d"));
		this.table.add(ImmutableList.of("e", "d", "c", "d", "e", "d"));
		this.table.add(ImmutableList.of("f", "d", "c", "d", "b", "f"));
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
			.thenReturn(Boolean.valueOf(true))
			.thenReturn(Boolean.valueOf(false));
		
		when(input.next())
			.thenReturn(this.table.get(0))
			.thenReturn(this.table.get(1))
			.thenReturn(this.table.get(2))
			.thenReturn(this.table.get(3))
			.thenReturn(this.table.get(4))
			.thenReturn(this.table.get(5));
			
		return input;
	}
	
	public void verifyFunctionalDependencyResultReceiver() throws CouldNotReceiveResultException, ColumnNameMismatchException {
		ColumnIdentifier expectedIdentifierA = new ColumnIdentifier(this.relationName, this.columnNames.get(0));
		ColumnIdentifier expectedIdentifierB = new ColumnIdentifier(this.relationName, this.columnNames.get(1));
		ColumnIdentifier expectedIdentifierC = new ColumnIdentifier(this.relationName, this.columnNames.get(2));
		ColumnIdentifier expectedIdentifierD = new ColumnIdentifier(this.relationName, this.columnNames.get(3));
		ColumnIdentifier expectedIdentifierE = new ColumnIdentifier(this.relationName, this.columnNames.get(4));
		ColumnIdentifier expectedIdentifierF = new ColumnIdentifier(this.relationName, this.columnNames.get(5));

		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA), expectedIdentifierB));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA), expectedIdentifierC));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA), expectedIdentifierD));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA), expectedIdentifierE));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierA), expectedIdentifierF));
		
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierC), expectedIdentifierD));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierD), expectedIdentifierC));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierE), expectedIdentifierC));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierE), expectedIdentifierD));
		
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierE, expectedIdentifierF), expectedIdentifierA));

		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierF), expectedIdentifierC));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierB, expectedIdentifierF), expectedIdentifierD));
		
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierD), expectedIdentifierB));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierE), expectedIdentifierB));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierE), expectedIdentifierD));

		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierC, expectedIdentifierE, expectedIdentifierF), expectedIdentifierA));

		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierE), expectedIdentifierB));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierE), expectedIdentifierC));
		
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierE, expectedIdentifierF), expectedIdentifierA));

		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierF), expectedIdentifierB));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(new ColumnCombination(expectedIdentifierD, expectedIdentifierF), expectedIdentifierC));
		
		verifyNoMoreInteractions(this.fdResultReceiver);
	}
}
