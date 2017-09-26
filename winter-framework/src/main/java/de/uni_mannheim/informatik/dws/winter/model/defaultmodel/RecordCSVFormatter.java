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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVDataSetFormatter;

/**
 * Formats a DefaultModel {@link AbstractRecord} as CSV
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class RecordCSVFormatter extends CSVDataSetFormatter<Record, Attribute> {

	@Override
	public String[] getHeader(DataSet<Record, Attribute> dataset) {
		List<String> names = new ArrayList<>();
		
		for(Attribute elem : dataset.getSchema().get()) {
			names.add(elem.toString());
		}
		
		Collections.sort(names);

		return names.toArray(new String[names.size()]);
	}

	@Override
	public String[] format(Record record, DataSet<Record, Attribute> dataset) {
		List<String> values = new ArrayList<>(dataset.getSchema().size());

		List<Attribute> names = new ArrayList<>();
		
		for(Attribute elem : dataset.getSchema().get()) {
			names.add(elem);
		}
		Collections.sort(names, new Comparator<Attribute>() {

			@Override
			public int compare(Attribute o1, Attribute o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		for (Attribute name : names) {
			values.add(record.getValue(name));
		}

		return values.toArray(new String[values.size()]);
	}

}
