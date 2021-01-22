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
package de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.similarity.EqualsSimilarity;

/**
 * {@link Comparator} for {@link Record}s, which always returns a similarity of 1.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class DummyComparator implements Comparator {

	public DummyComparator() {
	}

	private static final long serialVersionUID = 1L;
	private EqualsSimilarity<String> sim = new EqualsSimilarity<String>();
	private String dummyValue = "";

	private ComparatorLogger comparisonLog;

	/**
	 * Return always a value of 1
	 *
	 * @param record1
	 *            the first record (must not be null)
	 * @param record2
	 *            the second record (must not be null)
	 * @param schemaCorrespondence
	 *            A schema correspondence between two record1 and record2 (can
	 *            be null)
	 * @return Returns a similarity of 1
	 */
	@Override
	public double compare(Matchable record1, Matchable record2, Correspondence schemaCorrespondence) {
		if (this.comparisonLog != null) {
			this.comparisonLog.setComparatorName(getClass().getName());

			this.comparisonLog.setRecord1Value(dummyValue);
			this.comparisonLog.setRecord2Value(dummyValue);
		}

		if (this.comparisonLog != null) {
			this.comparisonLog.setRecord1PreprocessedValue(dummyValue);
			this.comparisonLog.setRecord2PreprocessedValue(dummyValue);
		}

		double similarity = 1;

		if (this.comparisonLog != null) {
			this.comparisonLog.setSimilarity(Double.toString(similarity));
		}
		return similarity;
	}

	@Override
	public ComparatorLogger getComparisonLog() {
		return this.comparisonLog;
	}

	@Override
	public void setComparisonLog(ComparatorLogger comparatorLog) {
		this.comparisonLog = comparatorLog;
	}

}
