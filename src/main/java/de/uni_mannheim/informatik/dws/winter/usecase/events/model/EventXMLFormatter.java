package de.uni_mannheim.informatik.dws.winter.usecase.events.model;


import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.io.XMLFormatter;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * {@link XMLFormatter} for {@link Event}s.
 *
 *
 * @author Daniel Ringler
 * based on MovieXMLFormatter by Oliver Lehmberg
 *
 */
public class EventXMLFormatter extends XMLFormatter<Event> {

    LocationXMLFormatter locationXMLFormatter = new LocationXMLFormatter();

    @Override
    public Element createRootElement(Document doc) {
        return doc.createElement("events");
    }

    @Override
    public Element createElementFromRecord(Event record, Document doc) {
        Element event = doc.createElement("event");

        event.appendChild(createTextElement("uri", record.getIdentifier(), doc));

        for (String label : record.getLabels()) {
            event.appendChild(createTextElementWithProvenance("label",
                    label,
                    record.getMergedAttributeProvenance(Event.LABELS), doc));
        }
        for (DateTime date : record.getDates()) {
            event.appendChild(createTextElementWithProvenance("date",
                    date.toString(),
                    record.getMergedAttributeProvenance(Event.DATES), doc));
        }

        for (Pair<Double, Double> coordinatesPair : record.getCoordinates()) {
            event.appendChild(createTextElementWithProvenance("coordinates",
                    coordinatesPair.toString(),
                    record.getMergedAttributeProvenance(Event.COORDINATES), doc));
        }


        for (String same : record.getSames()) {
            event.appendChild(createTextElementWithProvenance("same",
                    same,
                    record.getMergedAttributeProvenance(Event.SAMES), doc));
        }

        event.appendChild(createLocationElement(record, doc));

        return event;
    }

    protected Element createTextElementWithProvenance(String name,
                                                      String value, String provenance, Document doc) {
        Element elem = createTextElement(name, value, doc);
        elem.setAttribute("provenance", provenance);
        return elem;
    }

    protected Element createLocationElement(Event record, Document doc) {
        Element locationRoot = locationXMLFormatter.createRootElement(doc);
        locationRoot.setAttribute("provenance",
                record.getMergedAttributeProvenance(Event.LOCATIONS));

        for (Location l : record.getLocations()) {
            locationRoot.appendChild(locationXMLFormatter
                    .createElementFromRecord(l, doc));
        }

        return locationRoot;
    }

}
