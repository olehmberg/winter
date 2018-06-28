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



import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.similarity.EqualsSimilarity;

/**
 * {@link Comparator} for {@link Record}s
 * exactly matching.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class RecordComparatorEqual extends StringComparator {

	public RecordComparatorEqual(Attribute attributeRecord1, Attribute attributeRecord2) {
		super(attributeRecord1, attributeRecord2);
	}


	private static final long serialVersionUID = 1L;
	private EqualsSimilarity<String> sim = new EqualsSimilarity<String>();
	
	private HashMap<Integer, String> comparisonResult = new HashMap<Integer, String>();


	@Override
	public double compare(Record record1, Record record2, Correspondence<Attribute, Matchable> schemaCorrespondence) {
		this.comparisonResult.put(Comparator.comparatorName, RecordComparatorEqual.class.getName());
		
		String s1 = record1.getValue(this.getAttributeRecord1());
		String s2 = record2.getValue(this.getAttributeRecord2());
		
		this.comparisonResult.put(Comparator.record1Value, s1);
		this.comparisonResult.put(Comparator.record2Value, s2);
		
		// preprocessing		
		s1 = preprocess(s1);
		s2 = preprocess(s2);
		
		this.comparisonResult.put(Comparator.record1PreprocessedValue, s1);
		this.comparisonResult.put(Comparator.record2PreprocessedValue, s2);
		
		double similarity = sim.calculate(s1, s2);
		this.comparisonResult.put(Comparator.similarity, s2);

		return similarity;
	}


	@Override
	public Map<Integer, String> getComparisonResult() {
		// TODO Auto-generated method stub
		return this.comparisonResult;
	}

}
