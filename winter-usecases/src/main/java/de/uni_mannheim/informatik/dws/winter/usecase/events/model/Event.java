package de.uni_mannheim.informatik.dws.winter.usecase.events.model;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import org.apache.commons.lang3.StringUtils;


import java.io.Serializable;
import org.joda.time.DateTime;
import java.util.*;

/**
 * A {@link Record} which represents an actor
 *
 * @author Daniel Ringler
 *
 */
public class Event extends AbstractRecord<Attribute> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> uris = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private List<DateTime> dates = new ArrayList<>();
    private List<Pair<Double, Double>> coordinates = new ArrayList<>();
    private List<String> sames = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();

    public Event(String identifier, String provenance) {
        super(identifier, provenance);
    }

    private String[] attributeNames = {"URIs", "Labels", "Dates", "Lat", "Long", "Locations", "Sames"};
    public String[] getAttributeNames() {
        return attributeNames;
    }

    public String[] getAllAttributeValues(char separator) {
        //get URIs
        String allURIs = "";
        if (hasValue(URIS)) {
            Collections.sort(uris);
            for (String uri : uris) {
                allURIs += uri + separator;
            }
            allURIs = allURIs.substring(0, allURIs.length()-1);
        }
        //get Labels
        String allLabels = "";
        if (hasValue(LABELS)) {
            for (String label : labels) {
                allLabels += label + separator;
            }
            allLabels = allLabels.substring(0,allLabels.length()-1);
        }
        //get Dates
        String allDates = "";
        if (hasValue(DATES)) {
            for (DateTime date : dates) {
                allDates += date.toString() + separator;
            }
            allDates = allDates.substring(0, allDates.length()-1);
        }
        //get coordinates
        String allLat = "";
        if (hasValue(COORDINATES)) {
            for (Pair<Double, Double> p : coordinates) {
                allLat += p.getFirst().toString() + separator;
            }
            allLat = allLat.substring(0, allLat.length()-1);
        }
        String allLong = "";
        if (hasValue(COORDINATES)) {
            for (Pair<Double, Double> p : coordinates) {
                allLong += p.getSecond().toString() + separator;
            }
            allLong = allLong.substring(0, allLong.length()-1);
        }

        //get locations
       String allLocations = "";
       /*  if (hasValue(LOCATIONS)) {
            for (Location location : locations) {

            }
        }*/

        //get Sames
        String allSames = "";
        String [] allValues = {allURIs, allLabels, allDates, allLat, allLong, allLocations, allSames};//allParticipants,
        return allValues;
    }

    //getter
    public List<String> getUris() {
        return uris;
    }
    public List<String> getLabels() {
		return labels;
    }
    public List<DateTime> getDates() { return dates; }
    public List<Pair<Double, Double>> getCoordinates() {
        return coordinates;
    }
    public List<Location> getLocations() {
        return locations;
    }
    public List<String> getSames() {
        return sames;
    }

    //setter
    public void setURIs(List<String> uris) {
        this.uris = uris;
    }
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
    public void setDates(List<DateTime> dates) {
        this.dates = dates;
    }
    public void setCoordinates(List<Pair<Double, Double>> coordinates) { this.coordinates = coordinates;  }
    public void setLocations(List<Location> locations) { this.locations = locations; }
    public void setSames(List<String> sames) { this.sames = sames; }
    public void setSingleURI(String uri) {
        this.uris.clear();
        this.uris.add(uri);
    }
    /*
    * clear labels and add one new label
    * @param label
    */
    public void setSingleLabel(String label) {
        this.labels.clear();
        this.labels.add(label);
    }

    public void setSingleDate(DateTime date) {
        this.dates.clear();
        this.dates.add(date);
    }

    public void setSingleCoordinates(Pair<Double, Double> p) {
        this.coordinates.clear();
        if (p != null)
            this.coordinates.add(p);
    }


    //adder
    public void addURI(String uri) {
        if (!this.uris.contains(uri))
            this.uris.add(uri);
    }
    public void addLabel(String label) {
        if (!this.labels.contains(label))
            this.labels.add(label);
    }

    public void addDate(DateTime date) {
        if (!this.dates.contains(date))
            this.dates.add(date);

    }

    public void addCoordinates(Pair<Double, Double> coordinates) {
        if(!this.coordinates.contains(coordinates))
            this.coordinates.add(coordinates);
    }

    public void addLocation(Location location) {
        if(!this.locations.contains(location))
            this.locations.add(location);
    }

    public void addSame(String same) {
        if(!this.sames.contains(same))
            this.sames.add(same);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    /*public int hashCode() {
        int result = 31 + ((labels == null) ? 0 : labels.hashCode());
        return result;
    }*/
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Event){
            return this.getIdentifier().equals(((Event) obj).getIdentifier());
        }else
            return false;
    /*    if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        if (labels == null) {
            if (other.labels != null)
                return false;
        } else if (!labels.equals(other.labels))
            return false;
        return true;*/
    }

    public static final Attribute URIS = new Attribute("URIs");
    public static final Attribute LABELS = new Attribute("Labels");
    public static final Attribute DATES = new Attribute("Dates");
    public static final Attribute COORDINATES = new Attribute("Coordinates");
    public static final Attribute LOCATIONS = new Attribute("Locations");
    public static final Attribute SAMES = new Attribute("Sames");

    public Attribute[] getDefaultSchemaElements() {
        Attribute [] allDefaultSchemaElements = {URIS, LABELS, DATES, COORDINATES, LOCATIONS, SAMES};//PARTICIPANTS,
        return allDefaultSchemaElements;
    }

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.model.Record#hasValue(java.lang.Object)
     */
    @Override
    public boolean hasValue(Attribute attribute) {
        if(attribute==URIS)
            return uris.size()>0;
        if(attribute==LABELS)
            return labels.size()>0;
        else if(attribute==DATES)
            return dates.size()>0;
        else if(attribute==COORDINATES)
            return coordinates.size()>0;//!=null;//!Double.isNaN(lon);
        else if (attribute==LOCATIONS)
            return locations.size()>0;
        else if (attribute==SAMES)
            return sames.size()>0;
        return false;
    }

    public int getNumberOfValues(Attribute attribute) {
        if(attribute==URIS)
            return uris.size();
        if(attribute==LABELS)
            return labels.size();
        else if(attribute==DATES)
            return dates.size();
        else if(attribute==COORDINATES)
            return coordinates.size();//!=null;//!Double.isNaN(lon);
        else if (attribute==LOCATIONS)
            return locations.size();
        /*else if (attribute==PARTICIPANTS)
            return participants.size()>0;*/
        else if (attribute==SAMES)
            return sames.size();
        return 0;
    }

    private Map<Attribute, Collection<String>> provenance = new HashMap<>();
    private Collection<String> recordProvenance;

    public void setRecordProvenance(Collection<String> provenance) {
        //this.provenance.put("RECORD", provenance);
        recordProvenance = provenance;
    }

    public Collection<String> getRecordProvenance() {
        //return provenance.get("RECORD");
        return recordProvenance;
    }

    public void setAttributeProvenance(Attribute attribute,
                                       Collection<String> provenance) {
        this.provenance.put(attribute, provenance);
    }

    public Collection<String> getAttributeProvenance(String attribute) {
        return provenance.get(attribute);
    }

    public String getMergedAttributeProvenance(Attribute attribute) {
        Collection<String> prov = provenance.get(attribute);

        if (prov != null) {
            return StringUtils.join(prov, "+");
        } else {
            return "";
        }
    }


}
