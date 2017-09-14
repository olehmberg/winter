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
package de.uni_mannheim.informatik.dws.winter.webtables.writers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.LodCsvTableParser;
import edu.stanford.nlp.io.IOUtils;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class LodCsvTableWriterTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.writers.LodCsvTableWriter#write(de.uni_mannheim.informatik.dws.winter.webtables.Table, java.io.File)}.
	 * @throws IOException 
	 */
	public void testWriteTableFile() throws IOException {
		
		// read a file
		LodCsvTableParser parser = new LodCsvTableParser();
		parser.setParseLists(true);
		
		File in = new File("testdata/dbpedia/Song.csv");
		Table t = parser.parseTable(in);
		
		// write it back to a file
		LodCsvTableWriter writer = new LodCsvTableWriter();
		File out = new File("testdata/out/Song_written.csv");
		writer.write(t, out);
		
		// check if both files are equal
		Iterable<String> inFile = IOUtils.readLines(in);
		Iterable<String> outFile = IOUtils.readLines(out);
		
		Iterator<String> inIt = inFile.iterator();
		Iterator<String> outIt = outFile.iterator();
		
		while(inIt.hasNext()) {
			
			String expected = inIt.next();
			String actual = outIt.next();
			
			assertEquals(expected, actual);
			
		}
		
	}

}
