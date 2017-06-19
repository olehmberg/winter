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
import de.uni_mannheim.informatik.dws.winter.model.SimpleCorrespondence;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;

/**
 * {@link Comparator} for {@link Record}s based on the
 * values, and their {@link LevenshteinSimilarity}
 * similarity.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class RecordComparatorLevenshtein extends RecordComparator {

	public RecordComparatorLevenshtein(Attribute attributeRecord1, Attribute attributeRecord2) {
		super(attributeRecord1, attributeRecord2);
	}

	private static final long serialVersionUID = 1L;
	private LevenshteinSimilarity sim = new LevenshteinSimilarity();

	@Override
	public double compare(Record record1, Record record2, SimpleCorrespondence<Attribute> schemaCorrespondence) {
		
		// calculate similarity
		double similarity = sim.calculate(record1.getValue(this.getAttributeRecord1()), record2.getValue(this.getAttributeRecord2()));

		return similarity;
	}


}
