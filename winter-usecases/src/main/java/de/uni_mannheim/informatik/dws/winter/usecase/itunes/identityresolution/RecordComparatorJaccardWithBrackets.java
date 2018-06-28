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
package de.uni_mannheim.informatik.dws.winter.usecase.itunes.identityresolution;

import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparator;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;

/**
 * {@link Comparator} for {@link Record}s based on the {@link Attribute} values,
 * and their {@link TokenizingJaccardSimilarity} similarity.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class RecordComparatorJaccardWithBrackets extends RecordComparator {

	private static final long serialVersionUID = 1L;
	TokenizingJaccardSimilarity sim = new TokenizingJaccardSimilarity();
	
	private HashMap<ComparatorDetails, String> comparisonResult = new HashMap<ComparatorDetails, String>();

	public RecordComparatorJaccardWithBrackets(Attribute attributeRecord1, Attribute attributeRecord2, double threshold,
			boolean squared) {
		super(attributeRecord1, attributeRecord2);
		this.threshold = threshold;
		this.squared = squared;
	}

	private double threshold;
	private boolean squared;

	@Override
	public double compare(Record record1, Record record2, Correspondence<Attribute, Matchable> schemaCorrespondence) {
		this.comparisonResult.put(ComparatorDetails.comparatorName, RecordComparatorJaccardWithBrackets.class.getName());
		
		
		String s1 = record1.getValue(this.getAttributeRecord1());
		String s2 = record2.getValue(this.getAttributeRecord2());
		
		this.comparisonResult.put(ComparatorDetails.record1Value, s1);
		this.comparisonResult.put(ComparatorDetails.record1Value, s2);
		
		// preprocessing
		if (s1.contains("(") || s2.contains("(")) {
			// Remove everything in brackets
			String s1_temp = s1.replaceAll("\\(.*\\)", "");
			String s2_temp = s2.replaceAll("\\(.*\\)", "");
			
			this.comparisonResult.put(ComparatorDetails.record1PreprocessedValue, s1_temp);
			this.comparisonResult.put(ComparatorDetails.record2PreprocessedValue, s2_temp);
			
			// calculate similarity
			if (!s1_temp.equals(s1) || !s2_temp.equals(s2)) {
				double similarity = sim.calculate(s1_temp, s2_temp);
				this.comparisonResult.put(ComparatorDetails.similarity, Double.toString(similarity));
				// postprocessing
				if (similarity <= this.threshold) {
					similarity = 0;
				}

				if (squared)
					similarity *= similarity;
				
				this.comparisonResult.put(ComparatorDetails.postproccesedSimilarity, Double.toString(similarity));
				return similarity;
			}
		}
		this.comparisonResult.put(ComparatorDetails.similarity, "0");
		return 0;
	}

	@Override
	public Map<ComparatorDetails, String> getComparisonResult() {
		return this.comparisonResult;
	}

}
