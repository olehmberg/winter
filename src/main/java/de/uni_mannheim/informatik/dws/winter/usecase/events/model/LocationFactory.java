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

import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.List;

/**
 * A {@link MatchableFactory} for {@link Location}s.
 *
 * @author Daniel Ringler
 * based on ActorFactory by Oliver Lehmberg (oli@dwslab.de)
 *
 */

/*public class LocationFactory extends MatchableFactory<Location> {

    @Override
    public Location createModelFromElement(Node node, String provenanceInfo) {
        String uri = getAttributeValueFromNode(node, "uri");

        // create the object with id and provenance information
        Location location = new Location(uri, provenanceInfo);

        // fill the attributes
        //labels
        List<String> labelList = getListFromChildElement(node, "label");
        if (labelList != null) {
            for (String label : labelList) {
                if (label.contains("@"))
                    label = label.substring(0, label.indexOf("@"));
                location.addLabel(label);
            }
        }
        //sames
        List<String> sameList = getListFromChildElement(node, "same");
        if (sameList != null)
            location.setSames(sameList);
        //coordinates
        // get coordinates
        List<String> coordinateStrings = getListFromChildElement(node, "coordinates");
        if (coordinateStrings != null) {
            for (String coordinateString : coordinateStrings) {
                if (!coordinateString.contains("NAN")) { //check for NAN that can appear in DBpedia
                    String[] coorindatePair = coordinateString.split(",");
                    Pair<Double, Double> p = new Pair<>(
                            Double.valueOf(coorindatePair[0].substring(0, coorindatePair[0].indexOf("^"))),
                            Double.valueOf(coorindatePair[1].substring(0, coorindatePair[1].indexOf("^")))
                    );
                    location.addCoordinates(p);
                }
            }
        }
        return location;
    }
    public Location createModelFromTSVline(String[] values, String provenanceInfo) {
        return null;
    }

    @Override
    public Location createModelFromMultpleTSVline(HashSet<String[]> gatheredValues, String proveranaceInfo, char separator, DateTimeFormatter dateTimeFormatter, boolean filterFrom, LocalDate fromDate, boolean filterTo, LocalDate toDate, boolean filterByKeyword, String keyword) {
        return null;
    }

}
*/