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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class CSVRecordReaderTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.model.io.CSVMatchableReader#loadFromCSV(java.io.File, de.uni_mannheim.informatik.dws.winter.model.DataSet)}.
	 * @throws IOException 
	 */
	public void testLoadFromCSV() throws IOException {
		
		Map<String, Attribute> mapping = new HashMap<>();
		mapping.put("id", new Attribute("id"));
		mapping.put("page", new Attribute("page"));
		mapping.put("uri0", new Attribute("uri0"));
		mapping.put("uri1", new Attribute("uri1"));
		mapping.put("uri2", new Attribute("uri2"));
		mapping.put("uri3", new Attribute("uri3"));
		mapping.put("position", new Attribute("position"));
		mapping.put("name", new Attribute("name"));
		mapping.put("artist", new Attribute("artist"));
		mapping.put("time", new Attribute("time"));
		CSVRecordReader reader = new CSVRecordReader(0, mapping);
		
		DataSet<Record, Attribute> dataset = new HashedDataSet<>();
		
		reader.loadFromCSV(new File("testdata/csv/itunes.csv"), dataset);
		
		assertEquals(mapping.size(), dataset.getSchema().size());
		
		for(Attribute a : dataset.getSchema().get()) {
			assertEquals(mapping.get(a.getIdentifier()), a);
		}
		
	}

}
