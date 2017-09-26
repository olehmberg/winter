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
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * 
 * Super class for CSV readers.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class CSVMatchableReader<RecordType extends Matchable, SchemaElementType extends Matchable> {

	public void loadFromCSV(File file, DataSet<RecordType, SchemaElementType> dataset) throws IOException {
		
		CSVReader reader = new CSVReader(new FileReader(file));
		
		String[] values = null;
		int rowNumber = 0;
		
		while((values = reader.readNext()) != null) {
			readLine(file, rowNumber++, values, dataset);
		}
		
		reader.close();
		
	}
	
	protected abstract void readLine(File file, int rowNumber, String[] values, DataSet<RecordType, SchemaElementType> dataset);
	
}
