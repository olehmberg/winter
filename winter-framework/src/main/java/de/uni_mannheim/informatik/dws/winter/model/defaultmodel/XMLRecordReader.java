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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.uni_mannheim.informatik.dws.winter.model.io.XMLMatchableReader;

/**
 * Factory class for creating DefaultModel records from an XML node. All child
 * nodes on the first level are added as attributes.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class XMLRecordReader extends XMLMatchableReader<Record, Attribute> {

	private String idAttributeName;
	private Map<String, Attribute> attributeMapping;
	
	public XMLRecordReader(String idAttributeName, Map<String, Attribute> attributeMapping) {
		this.idAttributeName = idAttributeName;
		this.attributeMapping = attributeMapping;
	}

	@Override
	public Record createModelFromElement(Node node, String provenanceInfo) {
		String id = getValueFromChildElement(node, idAttributeName);

		Record model = new Record(id, provenanceInfo);

		// get all child nodes
		NodeList children = node.getChildNodes();

		// iterate over the child nodes until the node with childName is found
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);

			// check the node type
			if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
					&& child.getChildNodes().getLength() > 0) {

				// single value or list?
				if (child.getChildNodes().getLength() == 1) {

					// single value
					Attribute att = attributeMapping.get(child.getNodeName());
					model.setValue(att, child.getTextContent()
							.trim());

				} else {

					// list
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

					Attribute att = attributeMapping.get(child.getNodeName());
					model.setList(att, values);

				}

			}

		}

		return model;
	}

}
