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
package de.uni_mannheim.informatik.dws.winter.model.defaultmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;

/**
 * A default model that represents an {@link AbstractRecord} as a set of key/value pairs.
 * Supports lists as values.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class Record extends AbstractRecord<Attribute> {

	private static final long serialVersionUID = 1L;
	private Map<Attribute, String> values;
	private Map<Attribute, List<String>> lists;

	public Record(String identifier) {
		super(identifier, "");
		values = new HashMap<>();
		lists = new HashMap<>();
	}
	
	public Record(String identifier, String provenance) {
		super(identifier, provenance);
		values = new HashMap<>();
		lists = new HashMap<>();
	}

	public String getValue(Attribute attribute) {
		return values.get(attribute);
	}

	public List<String> getList(Attribute attribute) {
		return lists.get(attribute);
	}

	public void setValue(Attribute attribute, String value) {
		values.put(attribute, value);
	}

	public void setList(Attribute attribute, List<String> list) {
		lists.put(attribute, list);
	}

	@Override
	public boolean hasValue(Attribute attribute) {
		return (values.containsKey(attribute) && values.get(attribute)!=null)
				|| (lists.containsKey(attribute) && lists.get(attribute)!=null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return values.toString();
	}
	
	
}
