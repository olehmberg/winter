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
package de.uni_mannheim.informatik.dws.winter.DataIntegration.Restaurant;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusableFactory;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.XMLMatchableReader;

/**
 * A {@link XMLMatchableReader} for {@link Restaurant}s.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class RestaurantXMLReader extends XMLMatchableReader<Restaurant, Attribute> implements
		FusableFactory<Restaurant, Attribute> {

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.io.XMLMatchableReader#initialiseDataset(de.uni_mannheim.informatik.wdi.model.DataSet)
	 */
	@Override
	protected void initialiseDataset(DataSet<Restaurant, Attribute> dataset) {
		super.initialiseDataset(dataset);
		
		// the schema is defined in the Restaurant class and not interpreted from the file, so we have to set the attributes manually
		dataset.addAttribute(Restaurant.NAME);
		dataset.addAttribute(Restaurant.ADDRESS);
		dataset.addAttribute(Restaurant.CITY);
		dataset.addAttribute(Restaurant.PHONE);
		dataset.addAttribute(Restaurant.STYLE);
		
	}
	
	@Override
	public Restaurant createModelFromElement(Node node, String provenanceInfo) {
		String id = getValueFromChildElement(node, "id");

		// create the object with id and provenance information
		Restaurant Restaurant = new Restaurant(id, provenanceInfo);

		// fill the attributes
		Restaurant.setName(getValueFromChildElement(node, "Name"));
		Restaurant.setAddress(getValueFromChildElement(node, "Address"));
		Restaurant.setCity(getValueFromChildElement(node, "City"));
		Restaurant.setPhone(getValueFromChildElement(node, "Phone"));
		Restaurant.setStyle(getValueFromChildElement(node, "Style"));
		
		return Restaurant;
	}

	@Override
	public Restaurant createInstanceForFusion(RecordGroup<Restaurant, Attribute> cluster) {

		List<String> ids = new LinkedList<>();

		for (Restaurant m : cluster.getRecords()) {
			ids.add(m.getIdentifier());
		}

		Collections.sort(ids);

		String mergedId = StringUtils.join(ids, '+');

		return new Restaurant(mergedId, "fused");
	}

}
