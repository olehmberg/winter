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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uni_mannheim.informatik.dws.winter.model.io.XMLFormatter;

/**
 * {@link XMLFormatter} for {@link Actor}s.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class ActorXMLFormatter extends XMLFormatter<Actor> {

	@Override
	public Element createRootElement(Document doc) {
		return doc.createElement("actors");
	}

	@Override
	public Element createElementFromRecord(Actor record, Document doc) {
		Element actor = doc.createElement("actor");

		actor.appendChild(createTextElement("name", record.getName(), doc));
		if(record.getBirthplace()!=null) {
			actor.appendChild(createTextElement("birthplace", record.getBirthplace(), doc));
		}
		if(record.getBirthday()!=null) {
			actor.appendChild(createTextElement("birthday", record.getBirthday().toString(), doc));
		}

		return actor;
	}

}
