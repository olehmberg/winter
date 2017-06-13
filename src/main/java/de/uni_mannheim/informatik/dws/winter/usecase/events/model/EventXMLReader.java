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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusableFactory;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.XMLMatchableReader;

/**
 * A {@link XMLMatchableReader} for {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventXMLReader extends XMLMatchableReader<Event, Attribute> implements
        FusableFactory<Event, Attribute> {

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.model.io.XMLMatchableReader#initialiseDataset(de.uni_mannheim.informatik.wdi.model.DataSet)
     */
    @Override
    protected void initialiseDataset(DataSet<Event, Attribute> dataset) {
        super.initialiseDataset(dataset);

        // the schema is defined in the Movie class and not interpreted from the file, so we have to set the attributes manually
        dataset.addAttribute(Event.URIS);
        dataset.addAttribute(Event.LABELS);
        dataset.addAttribute(Event.DATES);
        dataset.addAttribute(Event.COORDINATES);
        dataset.addAttribute(Event.LOCATIONS);
        dataset.addAttribute(Event.SAMES);

    }

    @Override
    public Event createModelFromElement(Node node, String provenanceInfo) {
        Element element = (Element) node;
        String uri = element.getAttribute("uri");

        //getValueFromChildElement(node, "event");

        // create the object with id and provenance information
        Event event = new Event(uri, provenanceInfo);

        event.setSingleURI(uri);

        // fill the attributes

        //event.set(getValueFromChildElement(node, "director"));

        List<String> labels = getListFromChildElement(node, "label");

        //remove language tags from labels
        for (int i = 0; i < labels.size(); i++) {
            labels.set(i, removeLanguageTag(labels.get(i)));
        }

        event.setLabels(labels);

        //... uri

        // convert the date string into a DateTime object
        try {
            String date = getValueFromChildElement(node, "date");
            if (date != null && !date.isEmpty()) {
                DateTime dt = DateTime.parse(date.substring(0,10));
                event.setSingleDate(dt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // load the list of actors
        List<Location> locations = getObjectListFromChildElement(node, "locations",
                "location", new LocationXMLReader(), provenanceInfo);
        event.setLocations(locations);

        return event;
    }

    private String removeLanguageTag(String s) {
        if (s.contains("@")) {
            s =  s.substring(0, s.indexOf("@"));
        }
        return s;
    }

    @Override
    public Event createInstanceForFusion(RecordGroup<Event, Attribute> cluster) {

        List<String> ids = new LinkedList<>();

        for (Event m : cluster.getRecords()) {
            ids.add(m.getIdentifier());
        }

        Collections.sort(ids);

        String mergedId = StringUtils.join(ids, '+');

        return new Event(mergedId, "fused");
    }

}
