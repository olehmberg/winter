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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * Super class for reading records from XML
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 */
public abstract class XMLMatchableReader<RecordType extends Matchable, SchemaElementType extends Matchable> {

	/**
	 * creates a RecordType record from an XML node
	 * 
	 * @param node
	 *            the XML node containing the data
	 * @param provenanceInfo            
	 * @return a RecordType record
	 */
	public abstract RecordType createModelFromElement(Node node,
			String provenanceInfo);

	/**
	 * returns a value from a child node of the first parameter. The child not
	 * must only have one value (lists will be ignored)
	 * 
	 * @param node
	 *            the node containing the data
	 * @param childName
	 *            the name of the child node
	 * @return The value of the specified child node
	 */
	protected String getValueFromChildElement(Node node, String childName) {

		// get all child nodes
		NodeList children = node.getChildNodes();

		// iterate over the child nodes until the node with childName is found
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);

			// check the node type and the name
			if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
					&& child.getNodeName().equals(childName)) {

				return child.getTextContent().trim();

			}
		}

		return null;
	}

	/**
	 * returns a list of values from a child node of the first parameter. The
	 * list values are expected to be atomic, i.e. no complex node structures
	 * 
	 * @param node
	 *            the node containing the data
	 * @param childName
	 *            the name of the child node
	 * @return a list of values from the specified child node
	 */
	protected List<String> getListFromChildElement(Node node, String childName) {

		// get all child nodes
		NodeList children = node.getChildNodes();

		// iterate over the child nodes until the node with childName is found
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);

			// check the node type and name
			if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
					&& child.getNodeName().equals(childName)) {

				// prepare a list to hold all values
				List<String> values = new ArrayList<>(child.getChildNodes()
						.getLength());

				// iterate the value nodes
				for (int i = 0; i < child.getChildNodes().getLength(); i++) {
					Node valueNode = child.getChildNodes().item(i);
					String value = valueNode.getTextContent().trim();

					// add the value
					values.add(value);
				}

				return values;
			}
		}

		return null;
	}

	/**
	 * returns a list of records from a child node of the first parameter. The
	 * list values are converted into records by the factory passed as third
	 * parameter.
	 * 
	 * @param node
	 *            the node containing the data
	 * @param childName
	 *            the name of the child node
	 * @param objectNodeName
	 *            the name of the nodes containing the object data
	 * @param factory
	 *            the factory converting child nodes into records of type
	 *            TValueModel
	 * @return a list of typed values from the specified child node
	 */
	protected <ItemType extends Matchable> List<ItemType> getObjectListFromChildElement(
			Node node, String childName, String objectNodeName,
			XMLMatchableReader<ItemType, SchemaElementType> factory, String provenanceInfo) {

		// get all child nodes
		NodeList children = node.getChildNodes();

		// iterate over the child nodes until the node with childName is found
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);

			// check the node type and name
			if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
					&& child.getNodeName().equals(childName)) {

				// prepare a list to hold all values
				List<ItemType> values = new ArrayList<>(child.getChildNodes()
						.getLength());

				// iterate the value nodes
				for (int i = 0; i < child.getChildNodes().getLength(); i++) {
					Node valueNode = child.getChildNodes().item(i);

					// check the node type and name
					if (valueNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
							&& valueNode.getNodeName().equals(objectNodeName)) {
						// add the value
						values.add(factory.createModelFromElement(valueNode,
								provenanceInfo));
					}
				}

				return values;
			}
		}

		return null;
	}

	protected void initialiseDataset(DataSet<RecordType, SchemaElementType> dataset) {
		
	}
	
	/**
	 * Loads a data set from an XML file
	 * 
	 * @param dataSource
	 *            the XML file containing the data
	 * @param recordPath
	 *            the XPath to the XML nodes representing the entries
	 * @param dataset
	 * 			  the dataset to fill
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public void loadFromXML(File dataSource,
			String recordPath,
			DataSet<RecordType, SchemaElementType> dataset)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		
		// initialise the dataset
		initialiseDataset(dataset);
		
		// create objects for reading the XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		Document doc = builder.parse(dataSource);

		// prepare the XPath that selects the entries
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		XPathExpression expr = xpath.compile(recordPath);

		// execute the XPath to get all entries
		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (list.getLength() == 0) {
			System.out.println("ERROR: no elements matching the XPath ("
					+ recordPath + ") found in the input file "
					+ dataSource.getAbsolutePath());
		} else {
			System.out.println(String.format("Loading %d elements from %s",
					list.getLength(), dataSource.getName()));

			// create entries from all nodes matching the XPath
			for (int i = 0; i < list.getLength(); i++) {

				// create the entry, use file name as provenance information
				RecordType record = createModelFromElement(
						list.item(i), dataSource.getName());

				if (record != null) {
					// add it to the data set
					dataset.add(record);
				} else {
					System.out.println(String.format(
							"Could not generate entry for ", list.item(i)
									.getTextContent()));
				}
			}
		}
	}
}
