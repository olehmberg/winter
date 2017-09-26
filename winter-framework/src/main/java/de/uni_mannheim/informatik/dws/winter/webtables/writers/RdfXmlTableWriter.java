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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;

/**
 * 
 * Writes the table data as N3 RDF data.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RdfXmlTableWriter implements TableWriter {

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.t2k.webtables.writers.TableWriter#write(de.uni_mannheim.informatik.dws.t2k.webtables.Table, java.io.File)
	 */
	@Override
	public File write(Table t, File f) throws IOException, TransformerException, ParserConfigurationException {
		if(!f.getName().endsWith(".xml")) {
			f = new File(f.getAbsolutePath() + ".xml");
		}
		
		return write(t,f,new FileOutputStream(f));
	}

	protected File write(Table t, File f, OutputStream w) throws IOException, TransformerException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		
		// write root node with namespace declarations
		Element root = doc.createElement("rdf:RDF"); 
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:source", "file://" + t.getPath() + "#");
		doc.appendChild(root);

		// write schema
		Element sourceType = doc.createElement("rdf:Description");
		sourceType.setAttribute("rdf:about", "source:type");
		sourceType.setAttribute("rdf:type", "rdfs:Class");
		root.appendChild(sourceType);
		for(TableColumn c : t.getColumns()) {
			Element column = doc.createElement("rdf:Description");
			column.setAttribute("rdf:about", "source:column" + + c.getColumnIndex());
			column.setAttribute("rdfs:label", c.getHeader());
			root.appendChild(column);
		}
		
		// write records
       for(TableRow r : t.getRows()) {
    	   Element record = doc.createElement("rdf:Description");
    	   record.setAttribute("rdf:type", "source:type");
    	   
           for(TableColumn c: t.getColumns()) {
                
        	   Object value = r.get(c.getColumnIndex());
            	
        	   if(value!=null) {
        		   record.setAttribute("source:column" + c.getColumnIndex(), value.toString());
        	   }
                
           }
            
            root.appendChild(record);
        }

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;
		transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(root);
		StreamResult result = new StreamResult(w);

		transformer.transform(source, result);
		
        return f;
	}

}
