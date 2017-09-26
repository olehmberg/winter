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

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.similarity.list.OverlapSimilarity;
/**
 * A comparator that compares multiple Token overlaps from different Attributes.
 * The blank is used as a token separator.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */
public class RecordComparatorOverlapMultipleAttributes implements Comparator<Record, Attribute> {

	private static final long serialVersionUID = 1L;

	private OverlapSimilarity similarity = new OverlapSimilarity();
	private List<Attribute> attributeRecords1;
	private List<Attribute> attributeRecords2;
	
	
	public RecordComparatorOverlapMultipleAttributes(List<Attribute> attributeRecords1, List<Attribute> attributeRecords2) {
		super();
		this.setAttributeRecords1(attributeRecords1);
		this.setAttributeRecords2(attributeRecords2);
	}
	
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.Comparator#compare(de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.SimpleCorrespondence)
	 */
	@Override
	public double compare(Record record1, Record record2, Correspondence<Attribute, Matchable> schemaCorrespondence) {
		ArrayList<String> first 	= new ArrayList<String>();
		ArrayList<String> second 	= new ArrayList<String>();
		
		for(Attribute firstAttribute : this.attributeRecords1){
			String valuesTemp 		= record1.getValue(firstAttribute);
			if(valuesTemp != null){
				String valuesArray[]	= valuesTemp.split(" ");
				for(String value: valuesArray){
					first.add(value.toLowerCase());
				}
			}
		}
		
		for(Attribute secondAttribute : this.attributeRecords2){
			String valuesTemp 		= record2.getValue(secondAttribute);
			if(valuesTemp != null){
				String valuesArray[]	= valuesTemp.split(" ");
				for(String value: valuesArray){
					second.add(value.toLowerCase());
				}
			}
		}
		if(!first.isEmpty()&& !second.isEmpty())
			return similarity.calculate(first, second);
		
		return 0;
	}


	public List<Attribute> getAttributeRecords1() {
		return attributeRecords1;
	}


	public void setAttributeRecords1(List<Attribute> attributeRecords1) {
		this.attributeRecords1 = attributeRecords1;
	}


	public List<Attribute> getAttributeRecords2() {
		return attributeRecords2;
	}


	public void setAttributeRecords2(List<Attribute> attributeRecords2) {
		this.attributeRecords2 = attributeRecords2;
	}

}
