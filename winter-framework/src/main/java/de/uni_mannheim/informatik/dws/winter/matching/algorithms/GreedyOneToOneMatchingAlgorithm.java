package de.uni_mannheim.informatik.dws.winter.matching.algorithms;

import java.util.HashSet;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Group;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.RecordKeyValueMapper;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;

/**
 * 
 * Takes a set of correspondences as input and returns a set of correspondences in which every element can only part of one correspondence.
 * Uses a greedy approach (sorting all correspondences in descending order) and is not guaranteed to return a maximum weight matching. 
 * 
 * Example:
 * 
 *  Input: a-b (0.9), a-c (0.8), d-b (0.8), d-c (0.1)
 *  Output: a-b (0.9), d-c (0.1)
 * 
 * @author Oliver
 *
 * @param <TypeA>
 * @param <TypeB>
 */
public class GreedyOneToOneMatchingAlgorithm<TypeA extends Matchable, TypeB extends Matchable> implements MatchingAlgorithm<TypeA, TypeB> {

	private Processable<Correspondence<TypeA, TypeB>> correspondences;
	private Processable<Correspondence<TypeA, TypeB>> result;
	
	private boolean groupByLeftDataSource = false;
	private boolean groupByRightDataSource = false;
	
	/**
	 * 
	 * Specifies if correspondences should first be grouped by the data source ID of the left-hand side of the correspondences.
	 * If true, all data sources on the left-hand side will be processed individually
	 * 
	 * @param groupByLeftDataSource the groupByLeftDataSource to set
	 */
	public void setGroupByLeftDataSource(boolean groupByLeftDataSource) {
		this.groupByLeftDataSource = groupByLeftDataSource;
	}
	/**
	 * Specifies if correspondences should first be grouped by the data source ID of the right-hand side of the correspondences.
	 * If true, all data source on the right-hand side will be processed individually
	 * 
	 * @param groupByRightDataSource the groupByRightDataSource to set
	 */
	public void setGroupByRightDataSource(boolean groupByRightDataSource) {
		this.groupByRightDataSource = groupByRightDataSource;
	} 
	
	public GreedyOneToOneMatchingAlgorithm(Processable<Correspondence<TypeA, TypeB>> correspondences) {
		this.correspondences = correspondences;
	}
	
	@Override
	public void run() {

		Processable<Group<Pair<Integer, Integer>, Correspondence<TypeA, TypeB>>> grouped = correspondences.group(new RecordKeyValueMapper<Pair<Integer, Integer>, Correspondence<TypeA,TypeB>, Correspondence<TypeA,TypeB>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecordToKey(Correspondence<TypeA, TypeB> record,
					DataIterator<Pair<Pair<Integer, Integer>, Correspondence<TypeA, TypeB>>> resultCollector) {
				
				int leftGroup = groupByLeftDataSource ? record.getFirstRecord().getDataSourceIdentifier() : 0;
				int rightGroup = groupByRightDataSource ? record.getSecondRecord().getDataSourceIdentifier() : 0;
				
				resultCollector.next(new Pair<Pair<Integer,Integer>, Correspondence<TypeA,TypeB>>(new Pair<>(leftGroup, rightGroup), record));
				
			}

			
		});
		
		result = grouped.map(new RecordMapper<Group<Pair<Integer,Integer>,Correspondence<TypeA,TypeB>>, Correspondence<TypeA,TypeB>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void mapRecord(Group<Pair<Integer, Integer>, Correspondence<TypeA, TypeB>> record,
					DataIterator<Correspondence<TypeA, TypeB>> resultCollector) {
				
				Set<TypeA> matchedElements = new HashSet<>();
				for(Correspondence<TypeA, TypeB> cor : record.getRecords().sort((c)->c.getSimilarityScore(),false).get()) {
					if(!matchedElements.contains(cor.getFirstRecord()) && !matchedElements.contains(cor.getSecondRecord())) {
						resultCollector.next(cor);
						matchedElements.add(cor.getFirstRecord());
						matchedElements.add(cor.getSecondRecord());
					}
				}
			}
		});
		
	}

	@Override
	public Processable<Correspondence<TypeA, TypeB>> getResult() {
		return result;
	}

}
