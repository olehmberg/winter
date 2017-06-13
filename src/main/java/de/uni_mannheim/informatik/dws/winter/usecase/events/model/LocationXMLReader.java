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
package de.uni_mannheim.informatik.dws.winter.usecase.events.model;

import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.XMLMatchableReader;

import java.util.List;

/**
 * A {@link XMLMatchableReader} for {@link Location}s.
 *
 * @author Daniel Ringler
 *
 */
public class LocationXMLReader extends XMLMatchableReader<Location, Attribute> {

    @Override
    public Location createModelFromElement(Node node, String provenanceInfo) {
        Element element = (Element) node;
        String uri = element.getAttribute("uri");

        // create the object with id and provenance information
        Location location = new Location(uri, provenanceInfo);

        // fill the attributes
        List<String> labels = getListFromChildElement(node, "label");
        location.setLabels(labels);

        //...



        return location;
    }

}
