/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package de.uni_mannheim.informatik.dws.winter.matching.algorithms;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.SymmetricBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.MatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * 
 * Implementation of rule-based identity resolution for the special case of a single data set (=duplicate detection)
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RuleBasedDuplicateDetectionAlgorithm<TypeA extends Matchable, TypeB extends Matchable> extends RuleBasedMatchingAlgorithm<TypeA, TypeB, TypeB> {

	private SymmetricBlocker<TypeA, TypeB, TypeA, TypeB> blocker;
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.algorithms.RuleBasedMatchingAlgorithm#getDataset2()
	 */
	@Override
	public DataSet<TypeA, TypeB> getDataset2() {
		return getDataset1();
	}
	

	public RuleBasedDuplicateDetectionAlgorithm(DataSet<TypeA, TypeB> dataset, MatchingRule<TypeA, TypeB> rule,
			SymmetricBlocker<TypeA, TypeB, TypeA, TypeB> blocker) {
		super(dataset, null, null, rule, null);
		this.blocker = blocker;
	}
	
	public RuleBasedDuplicateDetectionAlgorithm(DataSet<TypeA, TypeB> dataset, Processable<Correspondence<TypeB, Matchable>> correspondences, MatchingRule<TypeA, TypeB> rule,
			SymmetricBlocker<TypeA, TypeB, TypeA, TypeB> blocker) {
		super(dataset, null, correspondences, rule, null);
		this.blocker = blocker;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.algorithms.RuleBasedMatchingAlgorithm#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.Result)
	 */
	@Override
	public Processable<Correspondence<TypeA, TypeB>> runBlocking(DataSet<TypeA, TypeB> dataset1,
			DataSet<TypeA, TypeB> dataset2, Processable<Correspondence<TypeB, Matchable>> correspondences) {
		return blocker.runBlocking(dataset1, correspondences);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.algorithms.RuleBasedMatchingAlgorithm#getReductionRatio()
	 */
	@Override
	public double getReductionRatio() {
		return blocker.getReductionRatio();
	}
	
	@Override
	public void run() {
		super.run();
		
		Correspondence.setDirectionByDataSourceIdentifier(getResult());
	}
}
