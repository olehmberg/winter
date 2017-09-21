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
package de.uni_mannheim.informatik.dws.winter.webtables.parsers;

import java.io.File;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RdfTableParserTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.webtables.parsers.RdfTableParser#parseTable(java.io.File)}.
	 */
	public void testParseTableFile() {
		
		RdfTableParser p = new RdfTableParser(
				"SELECT ?name ?street ?city_name ?category ?phone "
				+ "WHERE { "
				+ "?restaurant a <http://www.okkam.org/ontology_restaurant1.owl#Restaurant>;  "
				+ "<http://www.okkam.org/ontology_restaurant1.owl#name> ?name;"
				+ "<http://www.okkam.org/ontology_restaurant1.owl#has_address> ?address;"
				+ "<http://www.okkam.org/ontology_restaurant1.owl#category> ?category;"
				+ "<http://www.okkam.org/ontology_restaurant1.owl#phone_number> ?phone."
				+ "?address <http://www.okkam.org/ontology_restaurant1.owl#street> ?street; "
				+ "<http://www.okkam.org/ontology_restaurant1.owl#is_in_city> ?city."
				+ "?city <http://www.okkam.org/ontology_restaurant1.owl#name> ?city_name."
				+ "}");
		Table t = p.parseTable(new File("testdata/rdf/restaurant1.rdf"));
		
		System.out.println(t.getSchema().format(20));
		System.out.println(t.getSchema().formatDataTypes(20));
		
		for(int i = 0; i < t.getRows().size(); i++) {
			TableRow r = t.getRows().get(i);
			System.out.println(r.format(20));
		}
		
	}

}
