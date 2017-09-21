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
import java.util.HashSet;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RDFRecordReaderTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.model.io.RDFMatchableReader#loadFromRDF(java.io.File, java.lang.String, de.uni_mannheim.informatik.dws.winter.model.DataSet)}.
	 * @throws IOException 
	 */
	public void testLoadFromRDF() throws IOException {
		DataSet<Record, Attribute> ds = new HashedDataSet<>();
		RDFRecordReader r = new RDFRecordReader(-1);
		
		String query = 				
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
				+ "}";
		
		r.loadFromRDF(new File("testdata/rdf/restaurant1.rdf"), query, ds);
		
		Set<String> attributeNames = new HashSet<>();
		attributeNames.add("name");
		attributeNames.add("street");
		attributeNames.add("city_name");
		attributeNames.add("category");
		attributeNames.add("phone");
		System.out.println("Attributes:");
		for(Attribute a : ds.getSchema().get()) {
			System.out.println(String.format("\t%s", a.getName()));
		}
		assertEquals(attributeNames, new HashSet<>(Q.project(ds.getSchema().get(), (a)->a.getName())));
		
		System.out.println("Records:");
		for(Record rec : ds.get()) {
			StringBuilder sb = new StringBuilder();
			for(Attribute a : ds.getSchema().get()) {
				if(sb.length()!=0) {
					sb.append(", ");
				}
				sb.append(rec.getValue(a));
			}
			System.out.println(String.format("\t%s", sb.toString()));
		}
	}

}
