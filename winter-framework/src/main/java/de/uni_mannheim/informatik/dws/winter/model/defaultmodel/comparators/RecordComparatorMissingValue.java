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

import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.MissingValueComparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;

import java.util.List;

/**
 * {@link Comparator} for {@link Record}s based on the {@link Attribute} values,
 * and their {@link TokenizingJaccardSimilarity} similarity.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class RecordComparatorMissingValue extends RecordComparator implements MissingValueComparator<Record, Attribute> {

	private static final long serialVersionUID = 1L;

	private ComparatorLogger comparisonLog;
	private final List<Comparator<Record, Attribute>> penalisedComparators;
	private final double penalty;

	public RecordComparatorMissingValue(Attribute attributeRecord1, Attribute attributeRecord2,
										List<Comparator<Record, Attribute>> penalisedComparators, double penalty) {
		super(attributeRecord1, attributeRecord2);
		this.penalisedComparators = penalisedComparators;
		this.penalty = penalty;
	}

	@Override
	public double compare(Record record1, Record record2, Correspondence<Attribute, Matchable> schemaCorrespondence) {

		// preprocessing
		String s1 = record1.getValue(this.getAttributeRecord1());
		String s2 = record2.getValue(this.getAttributeRecord2());

		if (this.comparisonLog != null) {
			this.comparisonLog.setComparatorName(getClass().getName());

			this.comparisonLog.setRecord1Value(s1);
			this.comparisonLog.setRecord2Value(s2);
		}
		double similarity = 0.0;

		if (s1 == null || s2 == null) {
			similarity = 1.0;
		}

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

	@Override
	public List<Comparator<Record, Attribute>> getPenalisedComparators() {
		return penalisedComparators;
	}

	@Override
	public void addPenalisedComparator(Comparator<Record, Attribute> comparator) {
		this.penalisedComparators.add(comparator);
	}

	@Override
	public Double getPenalty() {
		return penalty;
	}
}
