package de.uni_mannheim.informatik.dws.winter.usecase.events.model;


import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.*;
import org.joda.time.DateTime;

/**
 * A {@link AbstractRecord} which represents an actor
 *
 * @author Daniel Ringler
 *
 */
public class Location extends AbstractRecord<Attribute> implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<String> labels = new ArrayList<>();
    private List<Pair<Double, Double>> coordinates = new ArrayList<>();
    private List<String> sames = new ArrayList<>();


    public Location(String identifier, String provenance) {
        super(identifier, provenance);
    }

    //GETTER
    public List<String> getLabels() {
        return labels;
    }
    public List<Pair<Double, Double>> getCoordinates() {
        return coordinates;
    }
    public List<String> getSames() {
        return sames;
    }

    //SETTER
    public void setLabels(List<String> labels) {this.labels = labels;}
    public void setSames(List<String> sames) {this.sames = sames;}
    public void setCoordinates(List<Pair<Double, Double>> coordinates) {this.coordinates = coordinates;}

    //ADDER
    public void addLabel(String label) {
        if (!this.labels.contains(label))
            this.labels.add(label);
    }

    public void addCoordinates(Pair<Double, Double> coordinates) {
        if(!this.coordinates.contains(coordinates))
            this.coordinates.add(coordinates);
    }

    public void addSame(String same) {
        if (!this.sames.contains(same))
            this.sames.add(same);
    }


    public static final Attribute LABELS = new Attribute("Labels");
    public static final Attribute COORDINATES = new Attribute("Coordinates");
    public static final Attribute SAMES = new Attribute("Sames");


    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.model.Record#hasValue(java.lang.Object)
     */
    @Override
    public boolean hasValue(Attribute attribute) {
        if(attribute==LABELS)
            return labels.size()>0;
        else if(attribute==COORDINATES)
            return coordinates.size()>0;//!Double.isNaN(lon);
        else if (attribute==SAMES)
            return sames.size()>0;
        return false;
    }

    public int getNumberOfValues(Attribute attribute) {
        if(attribute==LABELS)
            return labels.size();
     /*   else if(attribute==TYPES)
            return types.size();*/
        else if(attribute==COORDINATES)
            return coordinates.size();//!Double.isNaN(lon);
        else if (attribute==SAMES)
            return sames.size();
        return 0;
    }

    public Attribute[] getDefaultSchemaElements() {
        Attribute [] allDefaultSchemaElements = {LABELS, COORDINATES, SAMES};//TYPES
        return allDefaultSchemaElements;
    }


    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
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
        if(obj instanceof Location){
            return this.getIdentifier().equals(((Location) obj).getIdentifier());
        }else
            return false;
    }




}
