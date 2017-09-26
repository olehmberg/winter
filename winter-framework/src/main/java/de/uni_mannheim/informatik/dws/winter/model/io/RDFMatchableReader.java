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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class RDFMatchableReader<RecordType extends Matchable, SchemaElementType extends Matchable> {

	public void loadFromRDF(File file, String sparqlQuery, DataSet<RecordType, SchemaElementType> dataset) throws IOException {
		

		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// read the RDF/XML file
		model.read(new FileReader(file), null);

		// read data
		Map<String, Integer> attributes = new HashMap<>();
		
		int rowNumber = 0;
		// execute the query
		Query query = QueryFactory.create(sparqlQuery);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();

				Iterator<String> attributeNames = soln.varNames();
				while(attributeNames.hasNext()) {
					String att = attributeNames.next();
					if(!attributes.containsKey(att)) {
						attributes.put(att, attributes.size());
					}
				}
				
				String[] values = new String[attributes.size()];
				
				if(rowNumber==0) {
					// add attribute names
					for(Map.Entry<String, Integer> attribute : attributes.entrySet()) {
						values[attribute.getValue()] = attribute.getKey();
					}
					readLine(file, rowNumber++, values, dataset);
				}
				
				// add values
				for(String att : attributes.keySet()) {
					values[attributes.get(att)] = soln.get(att).toString();
				}
				readLine(file, rowNumber++, values, dataset);
			}
			
			qexec.close();
		}
	}
	
	protected abstract void readLine(File file, int rowNumber, String[] values, DataSet<RecordType, SchemaElementType> dataset);
	
}
