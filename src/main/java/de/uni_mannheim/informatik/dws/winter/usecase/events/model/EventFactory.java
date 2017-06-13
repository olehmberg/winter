package de.uni_mannheim.informatik.dws.winter.usecase.events.model;

import de.uni_mannheim.informatik.dws.winter.model.FusableFactory;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.w3c.dom.Node;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * A {@link MatchableFactory} for {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
/*public class EventFactory extends MatchableFactory<Event> implements
        FusableFactory<Event, Attribute> {

    private DateTimeFormatter dateTimeFormatter;
    private boolean filterFrom;
    private DateTime fromDate;
    private boolean filterTo;
    private DateTime toDate;
    private boolean applyKeywordSearch;
    private String keyword;
    private int dateNotParsedCounter;

    public void printDateNotParsedCounter() {
        System.out.println("dataSetD was loaded from XML. " + this.dateNotParsedCounter + " date attributes could not be parsed.");
    }

    //constructor
    public EventFactory(DateTimeFormatter dateTimeFormatter,
                        boolean filterFrom, DateTime fromDate,
                        boolean filterTo, DateTime toDate,
                        boolean applyKeywordSearch, String keyword) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.filterFrom = filterFrom;
        this.fromDate = fromDate;
        this.filterTo = filterTo;
        this.toDate = toDate;
        this.applyKeywordSearch = applyKeywordSearch;
        this.keyword = keyword;
        this.dateNotParsedCounter = 0;
    }

    @Override
    public Event createModelFromElement(Node node, String provenanceInfo) {
        String uri = getAttributeValueFromNode(node, "uri");
                //getValueFromChildElement(node, "event");

        // create the object with id and provenance information
        Event event = new Event(uri, provenanceInfo);

        event.addURI(uri);
        //get all labels, remove the language tag, add to event label list
        List<String> labelList = getListFromChildElement(node, "label");
        if (labelList != null) {
            for (String label : labelList) {
                if (label.contains("@"))
                    label = label.substring(0, label.indexOf("@"));
                event.addLabel(label);
            }

            //filter labels by keyword
            if(this.applyKeywordSearch) {
                if (!event.getLabels().stream().anyMatch(label -> label.trim().toLowerCase().contains(this.keyword.toLowerCase()))) {
                    //return null if keyword is not found in any label
                    //System.out.println(event.getLabels().toString() + " does not contain the keyword " + this.keyword + ". return null.");
                    return null;
                //} else {
                    //System.out.println(event.getLabels().toString() + " contains the keyword " + this.keyword);
                }
            }
        }

        //try to convert dates into LocalDate object
        try {
            List<String> dateStrings= getListFromChildElement(node, "date");
            if (dateStrings != null) {
                List<DateTime> localDates = new ArrayList<>();
                for (String dateString : dateStrings) {
                    if (dateString != null && !dateString.isEmpty()
                            //incomplete date
                            && !dateString.contains("##")
                            //negative date
                            && dateString.indexOf('-') != 0) {
                        dateString = dateString.substring(0, dateString.indexOf("^"));
                        DateTime localDate = DateTime.parse(dateString);//, dateTimeFormatter);
                        localDates.add(localDate);
                    }
                }
                if (this.filterFrom) { //&& filterTo) {
                    //if (localDate.isAfter(toDate) || localDate.isBefore(fromDate)) {
                    if (localDates.size() == 0 || localDates.stream().anyMatch(localDate -> localDate.isBefore(this.fromDate))) {
                        //System.out.println(localDates.toString() + " contains a date that is before " + this.fromDate + ". return null.");
                        return null;
                    //} else {
                    //    System.out.println(localDates.toString() + " contains a date that is not before " + this.fromDate);
                    }
                }
                if (this.filterTo) {
                    if (localDates.size() == 0 || localDates.stream().anyMatch(localDate -> localDate.isAfter(this.toDate))) {
                    //    System.out.println(localDates.toString() + " contains a date that is after " + this.toDate + ". return null.");
                        return null;
                    //} else {
                    //    System.out.println(localDates.toString() + " contains a date that is not after " + this.toDate);
                    }
                }
                event.setDates(localDates);
            } else { //dateStrings == null
                //no date available: return null if date should be filtered
                if (this.filterFrom || this.filterTo) {
                    return null;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            this.dateNotParsedCounter++;
        }

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
                    event.addCoordinates(p);
                }
            }
        }
        // get owl:sameAs links
        List<String> sameList = getListFromChildElement(node, "same");
        if (sameList != null)
            event.setSames(sameList);

        // get location
        List<Location> locations = getObjectListFromChildElement(node, "locations",
                "location", new LocationFactory(), provenanceInfo);
        if (locations != null)
            event.setLocations(locations);

        return event;
    }
    public Event createModelFromTSVline(String[] values, String provenanceInfo) {
        //values for basic data file: 0:uri, 1:label, 2:date, 3:lat, 4:long
        Event event = new Event(values[0], provenanceInfo);

        //fill the attributes
        event.addURI(values[0]);

        event.addLabel(values[1]);

        // 1214-07-27^^http://www.w3.org/2001/XMLSchema#date
        event.addDate(DateTime.parse(values[2].substring(0, values[2].indexOf("^"))));

        //50.5833^^http://www.w3.org/2001/XMLSchema#float	3.225^^http://www.w3.org/2001/XMLSchema#float
        //event.setLat(Double.valueOf(values[3].substring(0, values[3].indexOf("^"))));
        //event.setLon(Double.valueOf(values[4].substring(0, values[4].indexOf("^"))));
        Pair<Double, Double> p = new Pair<>(
                Double.valueOf(values[3].substring(0, values[3].indexOf("^"))),
                Double.valueOf(values[4].substring(0, values[4].indexOf("^")))
        );
        event.addCoordinates(p);

        event.addSame(values[5]);
        Location location = new Location(values[6], provenanceInfo);
        event.addLocation(location);

        return event;
    }
    */

    /**
     * Returns an event with all gathered values (have the same URI)
     * @param gatheredValues
     * @param provenanceInfo
     * @param separator
     * @param dateTimeFormatter
     * @param filterFrom
     * @param fromDate
     * @param filterTo
     * @param toDate
     * @param filterByKeyword
     * @param keyword
     * @return Event
     */
  /*  public Event createModelFromMultpleTSVline(HashSet<String[]> gatheredValues,
                                               String provenanceInfo,
                                               char separator,
                                               DateTimeFormatter dateTimeFormatter,
                                               boolean filterFrom,
                                               DateTime fromDate,
                                               boolean filterTo,
                                               DateTime toDate,
                                               boolean filterByKeyword,
                                               String keyword) {

        Event event = null;
        boolean firstLine = true;
        for (String[] values : gatheredValues) {
            if (firstLine) {
                event = new Event(values[0], provenanceInfo);
                firstLine = false;
            }
            //fill the attributes
            //add uri
            event.addURI(values[0]);

            //add label after removing the language tag
            if (values[1].contains("@"))
                event.addLabel(values[1].substring(0, values[1].indexOf("@")));
            else
                event.addLabel(values[1]);


            // 1214-07-27^^http://www.w3.org/2001/XMLSchema#date
            //incomplete date 1863-##-##^^http://www.w3.org/2001/XMLSchema#date
            String date = values[2].replace("##", "01");
            if (values[2].contains("^"))
                date = date.substring(0, date.indexOf("^"));

            try {
                DateTime localDate = DateTime.parse(date, dateTimeFormatter);
                //check date against user input parameters
                if (filterFrom) { //&& filterTo) {
                    //if (localDate.isAfter(toDate) || localDate.isBefore(fromDate)) {
                    if (localDate.isBefore(fromDate)) {
                        return null;
                    }
                }
                if (filterTo) {
                    if (localDate.isAfter(toDate)) {
                        return null;
                    }
                }
                event.addDate(localDate);
            } catch (DateTimeParseException e) {
                //System.out.println(values[0] + " " + date);
                return null;
            }



            //50.5833^^http://www.w3.org/2001/XMLSchema#float	3.225^^http://www.w3.org/2001/XMLSchema#float
            //event.setLat(Double.valueOf(values[3].substring(0, values[3].indexOf("^"))));
            //event.setLon(Double.valueOf(values[4].substring(0, values[4].indexOf("^"))));
            if (values.length>4) {
                String latString = values[3];
                if (latString.contains("^"))
                    latString = latString.substring(0, latString.indexOf("^"));
                String longString = values[4];
                if (longString.contains("^"))
                    longString = longString.substring(0, longString.indexOf("^"));

                Pair<Double, Double> p = new Pair<>(
                        Double.valueOf(latString),
                        Double.valueOf(longString)
                );
                event.addCoordinates(p);
            }
            if (values.length>5) {
                event.addSame(values[5]);
            }
            if (values.length>6) {
                Location location = new Location(values[6], provenanceInfo);
                event.addLocation(location);
            }
        }

        //filter labels by keyword
        if(filterByKeyword) {
            if (!event.getLabels().stream().anyMatch(label -> label.trim().toLowerCase().contains(keyword.toLowerCase()))) {
                return null;
            } //else {
                //System.out.println(keyword + " found for " + event.getLabels());
            }
        }

        return event;
    }

    //@Override
    public Event createInstanceForFusion(RecordGroup<Event, Attribute> cluster) {

        List<String> ids = new LinkedList<>();

        for (Event m : cluster.getRecords()) {
            ids.add(m.getIdentifier());
        }

        Collections.sort(ids);

        String mergedId = StringUtils.join(ids, '+');

        return new Event(mergedId, "fused");
    }
*/
//}
