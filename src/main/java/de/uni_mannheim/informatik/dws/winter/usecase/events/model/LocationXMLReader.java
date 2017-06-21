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

import de.uni_mannheim.informatik.dws.winter.model.Pair;
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
        // set labels
        List<String> labels = getListFromChildElement(node, "label");
        //remove language tags from labels
        for (int i = 0; i < labels.size(); i++) {
            labels.set(i, removeLanguageTag(labels.get(i)));
        }
        location.setLabels(labels);

        // set coordinates
        List<String> coordinateStrings = getListFromChildElement(node, "coordinates");
        if (coordinateStrings != null) {
            for (String coordinateString : coordinateStrings) {
                if (!coordinateString.contains("NAN")) { //check for NAN that can appear in DBpedia
                    String[] coordinatePair = coordinateString.split(",");
                    Pair<Double, Double> p = new Pair<>(
                            Double.valueOf(coordinatePair[0].substring(0, coordinatePair[0].indexOf("^"))),
                            Double.valueOf(coordinatePair[1].substring(0, coordinatePair[1].indexOf("^")))
                    );
                    location.addCoordinates(p);
                }
            }
        }
        // set sameAs links
        List<String> sames = getListFromChildElement(node, "same");
        if (sames != null) {
            location.setSames(sames);
        }

        return location;
    }

    private String removeLanguageTag(String s) {
        if (s.contains("@")) {
            s =  s.substring(0, s.indexOf("@"));
        }
        return s;
    }

}
