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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;

/**
 * A parser that loads the result of a SPARQL query executed against an RDF document as table
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RdfTableParser extends TableParser {

	private String queryString;

	public RdfTableParser(String query) {
		this.queryString = query;
		setStringNormalizer(new DynamicStringNormalizer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_mannheim.informatik.dws.winter.webtables.parsers.TableParser#
	 * parseTable(java.io.File)
	 */
	@Override
	public Table parseTable(File file) {
		Reader r = null;
		Table t = null;
		try {
			if (file.getName().endsWith(".gz")) {
				GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
				r = new InputStreamReader(gzip, "UTF-8");
			} else {
				r = new InputStreamReader(new FileInputStream(file), "UTF-8");
			}

			t = parseTable(r, file.getName());

			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_mannheim.informatik.dws.winter.webtables.parsers.TableParser#
	 * parseTable(java.io.Reader, java.lang.String)
	 */
	@Override
	public Table parseTable(Reader reader, String fileName) throws IOException {

		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// read the RDF/XML file
		model.read(reader, null);

		// create new table
		Table table = new Table();
		table.setPath(fileName);

		boolean typesAlreadyDetected = true;

		// read data
		Map<String, Integer> attributes = new HashMap<>();
		Map<String, DataType> types = new HashMap<>();
		List<String[]> tableListContent = new LinkedList<>();
		
		// execute the query
		Query query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();

				Iterator<String> attributeNames = soln.varNames();
				while(attributeNames.hasNext()) {
					String att = attributeNames.next();
					if(!attributes.containsKey(att)) {
						attributes.put(att, attributes.size());
						
						if(soln.get(att).isLiteral()) {
							switch (soln.getLiteral(att).getDatatype().getURI()) {
							case "http://www.w3.org/2001/XMLSchema#decimal":
								types.put(att, DataType.numeric);
								break;
							case "http://www.w3.org/2001/XMLSchema#date":
								types.put(att, DataType.date);
								break;
							case "http://www.w3.org/2001/XMLSchema#string":
								types.put(att, DataType.string);
								break;
							default:
								types.put(att, DataType.unknown);
								typesAlreadyDetected = false;
								break;
							}
						} else {
							types.put(att, DataType.string);
						}
					}
				}
				
				String[] values = new String[attributes.size()];
				for(String att : attributes.keySet()) {
					values[attributes.get(att)] = soln.get(att).toString();
				}
				tableListContent.add(values);
			}
			
			qexec.close();
		}

		// check whether table content is not empty!
		if (tableListContent.size()==0 )
			return null;


		int maxWidth = 0;
		for (String[] line : tableListContent) {
			maxWidth = Math.max(maxWidth, line.length);
		}

		// convert content into String[][] format for easier processing.
		String[][] tableContent = new String[tableListContent.size()][];
		tableListContent.toArray(tableContent);

		// make sure all rows have the same length!
		for (int i = 0; i < tableContent.length; i++) {
			if (tableContent[i].length < maxWidth) {
				tableContent[i] = Arrays.copyOf(tableContent[i], maxWidth);
			}
		}

		int colIdx = 0;
		// set the header,
		for (String columnName : Q.sort(attributes.keySet(), (a1,a2)->Integer.compare(attributes.get(a1), attributes.get(a2)))) {
			TableColumn c = new TableColumn(colIdx, table);

			String header = columnName;
			if (isCleanHeader()) {
				header = this.getStringNormalizer().normaliseHeader(header);
			}
			c.setHeader(header);

			c.setDataType(types.get(columnName));

			table.addColumn(c);

			colIdx++;
		}

		
		// populate table content
		populateTable(tableContent, table, new int[0]);

		if (typesAlreadyDetected && isConvertValues()) {
			table.convertValues();
		} else if (isConvertValues()) {
			table.inferSchemaAndConvertValues(this.getTypeDetector());
		} else {
			table.inferSchema(this.getTypeDetector());
		}

		if (!table.hasSubjectColumn()) {
			table.identifySubjectColumn();
		}

		return table;
	}

}
