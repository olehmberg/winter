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
package de.uni_mannheim.informatik.dws.winter.matching.rules;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;

/**
 * 
 * A matching rule that returns the input correspondences above the specified threshold and specifies that correspondences should be grouped by both elements.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class IdentityMatchingRule<TypeA extends Matchable, TypeB extends Matchable> extends AggregableMatchingRule<TypeA, TypeB> {

	public IdentityMatchingRule(double finalThreshold) {
		super(finalThreshold);
	}

	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.Comparator#compare(de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.SimpleCorrespondence)
	 */
	@Override
	public double compare(TypeA record1, TypeA record2, Correspondence<TypeB, Matchable> schemaCorrespondence) {
		// this method is not used, as the input is returned as output in mapRecord
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.RecordMapper#mapRecord(java.lang.Object, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecord(Correspondence<TypeA, TypeB> record,
			DataIterator<Correspondence<TypeA, TypeB>> resultCollector) {
		resultCollector.next(record);
	}

}
