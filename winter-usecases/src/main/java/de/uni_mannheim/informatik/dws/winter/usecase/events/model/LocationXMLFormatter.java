/**
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.dws.winter.usecase.events.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uni_mannheim.informatik.dws.winter.model.io.XMLFormatter;

/**
 * {@link XMLFormatter} for {@link Location}s.
 *
 * @author Daniel Ringler
 * based on ActorXMLFormatter by Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class LocationXMLFormatter extends XMLFormatter<Location> {

    @Override
    public Element createRootElement(Document doc) {
        return doc.createElement("locations");
    }

    @Override
    public Element createElementFromRecord(Location record, Document doc) {
        Element location = doc.createElement("location");

        for (String label : record.getLabels())
            location.appendChild(createTextElement("label", label, doc));

        // coordinates

        for (String same : record.getSames())
            location.appendChild(createTextElement("same", same, doc));

        return location;
    }

}
