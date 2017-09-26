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
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;

/**
 * {@link Comparator} for {@link Record}s based on the
 * {@link Attribute} values.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public abstract class RecordComparator implements Comparator<Record, Attribute> {
	
	/**
	 * When a ReordComparator is created. The two to be compared attributes need to be handed over.
	 */
	private static final long serialVersionUID = 1L;
	private Attribute attributeRecord1;
	private Attribute attributeRecord2;

	public RecordComparator(Attribute attributeRecord1, Attribute attributeRecord2) {
		super();
		this.setAttributeRecord1(attributeRecord1);
		this.setAttributeRecord2(attributeRecord2);
	}
	
	public Attribute getAttributeRecord1() {
		return attributeRecord1;
	}

	public void setAttributeRecord1(Attribute attributeRecord1) {
		this.attributeRecord1 = attributeRecord1;
	}

	public Attribute getAttributeRecord2() {
		return attributeRecord2;
	}

	public void setAttributeRecord2(Attribute attributeRecord2) {
		this.attributeRecord2 = attributeRecord2;
	}
}
