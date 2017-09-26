package de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers;

import java.util.List;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.list.Union;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;

/**
 * {@link AttributeValueFuser} for the coordinates of {@link Event}s.
 * Created on 2017-01-06
 * @author Daniel Ringler
 *
 */
public class EventCoordinatesFuserAll extends AttributeValueFuser<List<Pair<Double, Double>>, Event, Attribute> {
    public EventCoordinatesFuserAll() {
        super(new Union<Pair<Double, Double>, Event, Attribute>());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        return record.hasValue(Event.COORDINATES);
    }

    @Override
    protected List<Pair<Double, Double>> getValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        return record.getCoordinates();
    }

    @Override
    public void fuse(RecordGroup<Event, Attribute> group, Event fusedRecord, Processable<Correspondence<Attribute, Matchable>> schemaCorrespondences, Attribute attribute) {
        FusedValue<List<Pair<Double, Double>>, Event, Attribute> fused = getFusedValue(group, schemaCorrespondences, attribute);
        fusedRecord.setCoordinates(fused.getValue());
        fusedRecord.setAttributeProvenance(Event.COORDINATES, fused.getOriginalIds());
    }

}
