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
package de.uni_mannheim.informatik.dws.winter.usecase.movies.model;

import org.joda.time.DateTime;
import org.w3c.dom.Node;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.XMLMatchableReader;

/**
 * A {@link XMLMatchableReader} for {@link Actor}s.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class ActorXMLReader extends XMLMatchableReader<Actor, Attribute> {

	@Override
	public Actor createModelFromElement(Node node, String provenanceInfo) {
		String id = getValueFromChildElement(node, "id");

		// create the object with id and provenance information
		Actor actor = new Actor(id, provenanceInfo);

		// fill the attributes
		actor.setName(getValueFromChildElement(node, "name"));
		actor.setBirthplace(getValueFromChildElement(node, "birthplace"));

		// convert the date string into a DateTime object
		try {
			String date = getValueFromChildElement(node, "birthday");
			if (date != null) {
				DateTime dt = DateTime.parse(date);
				actor.setBirthday(dt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return actor;
	}

}
