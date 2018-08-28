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
package de.uni_mannheim.informatik.dws.winter.model.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * Super class for formatting a {@link AbstractRecord} as CSV
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public abstract class CSVDataSetFormatter<RecordType extends Matchable, SchemaElementType extends Matchable> {

	public abstract String[] getHeader(List<SchemaElementType> orderedHeader);

	public abstract String[] format(RecordType record, DataSet<RecordType, SchemaElementType> dataset,
			List<SchemaElementType> orderedHeader);

	/**
	 * Writes the data set to a CSV file
	 * 
	 * @param file
	 * @param dataset
	 * @param orderedHeader
	 * @throws IOException
	 */
	public void writeCSV(File file, DataSet<RecordType, SchemaElementType> dataset, List<SchemaElementType> orderedHeader)
			throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(file));
		
		String[] headers = null;
		if(orderedHeader != null){
			headers = getHeader(orderedHeader);
		}
		else{
			headers = getHeader(sortAttributesAlphabetically(dataset));
		}
		

		if (headers != null) {
			writer.writeNext(headers);
		}

		for (RecordType record : dataset.get()) {
			String[] values = format(record, dataset, orderedHeader);

			writer.writeNext(values);
		}

		writer.close();
	}
	
	/**
	 * Sorts the the header attributes of a schema alphabetically.
	 * 
	 * @param dataset
	 * @return
	 */
	private List<SchemaElementType> sortAttributesAlphabetically(DataSet<RecordType, SchemaElementType> dataset) {
		List<SchemaElementType> attributes = new ArrayList<>();

			for (SchemaElementType elem : dataset.getSchema().get()) {
				attributes.add(elem);
			}

			Collections.sort(attributes, new Comparator<SchemaElementType>() {

				@Override
				public int compare(SchemaElementType o1, SchemaElementType o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
			
			return attributes;
		} 

}
