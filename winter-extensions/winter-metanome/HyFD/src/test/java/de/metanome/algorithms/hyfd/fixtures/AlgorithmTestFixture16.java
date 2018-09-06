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

public class AlgorithmTestFixture16 extends AbstractAlgorithmTestFixture {
	
	public AlgorithmTestFixture16() throws CouldNotReceiveResultException {
		this.columnNames = ImmutableList.of("A", "B", "C");
		this.numberOfColumns = 3;
		
		this.table.add(ImmutableList.of("1","6","11"));
		this.table.add(ImmutableList.of("2","7","12"));
		this.table.add(ImmutableList.of("3","8","13"));
		this.table.add(ImmutableList.of("4","9","14"));
		this.table.add(ImmutableList.of("5","10","15"));
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
		
		ColumnCombination coComA = new ColumnCombination(expectedIdentifierA);
		ColumnCombination coComB = new ColumnCombination(expectedIdentifierB);
		ColumnCombination coComC = new ColumnCombination(expectedIdentifierC);
		
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(coComA, expectedIdentifierB));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(coComA, expectedIdentifierC));
		
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(coComB, expectedIdentifierA));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(coComB, expectedIdentifierC));

		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(coComC, expectedIdentifierA));
		verify(this.fdResultReceiver).receiveResult(new FunctionalDependency(coComC, expectedIdentifierB));

		
		verifyNoMoreInteractions(fdResultReceiver);
	}
	
}
