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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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

import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Abstract super class for specifying how a {@link HashedDataSet} should be
 * transformed into XML
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 */
public abstract class XMLFormatter<RecordType> {

	/**
	 * Creates the root element for a collection of records
	 * 
	 * @param doc
	 * @return the root element for a collection of records
	 */
	public abstract Element createRootElement(Document doc);

	/**
	 * Creates an element representing a record
	 * 
	 * @param record
	 * @param doc
	 * @return an element representing a record
	 */
	public abstract Element createElementFromRecord(RecordType record,
			Document doc);

	/**
	 * Creates a text element with the specified element name and the value as content
	 * 
	 * @param name
	 * @param value
	 * @param doc
	 * @return a text element with the specified element name and the value as content
	 */
	protected Element createTextElement(String name, String value, Document doc) {
		Element elem = doc.createElement(name);
		if (value != null) {
			elem.appendChild(doc.createTextNode(value));
		}
		return elem;
	}
	
	/**
	 * Writes dataset to an XML file using this specified formatter
	 * 
	 * @param outputFile
	 * @param dataset
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
	public void writeXML(File outputFile, Processable<RecordType> dataset)
			throws ParserConfigurationException, TransformerException,
			FileNotFoundException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element root = createRootElement(doc);

		doc.appendChild(root);

		for (RecordType record : dataset.get()) {
			root.appendChild(createElementFromRecord(record, doc));
		}

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer;
		transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(root);
		StreamResult result = new StreamResult(new FileOutputStream(outputFile));

		transformer.transform(source, result);

	}
}
