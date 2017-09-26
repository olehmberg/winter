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
import au.com.bytecode.opencsv.CSVWriter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * 
 * Super class for CSV writers.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class CSVFormatter<RecordType> {

	public abstract String[] getHeader();

	public abstract String[] format(RecordType record);

	/**
	 * Writes the data set to a CSV file
	 * 
	 * @param file
	 * @param dataset
	 * @throws IOException
	 */
	public void writeCSV(File file, Processable<RecordType> dataset)
			throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(file));

		String[] headers = getHeader();
		if(headers!=null) {
			writer.writeNext(headers);
		}

		for (RecordType record : dataset.get()) {
			String[] values = format(record);

			writer.writeNext(values);
		}

		writer.close();
	}
	
}
