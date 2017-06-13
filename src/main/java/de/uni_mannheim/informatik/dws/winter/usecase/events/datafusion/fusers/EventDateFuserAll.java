package de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.list.Union;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;
import org.joda.time.DateTime;

import java.util.List;

/**
 * {@link AttributeValueFuser} for the dates of {@link Event}s.
 * Based on ActorFuserUnion. Created on 2017-01-06
 * @author Daniel Ringler
 *
 */
public class EventDateFuserAll extends AttributeValueFuser<List<DateTime>, Event, Attribute> {
    public EventDateFuserAll() {
        super(new Union<DateTime, Event, Attribute>());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<Attribute, Event> correspondence) {
        return record.hasValue(Event.DATES);
    }

    @Override
    protected List<DateTime> getValue(Event record, Correspondence<Attribute, Event> correspondence) {
        return record.getDates();
    }


    @Override
    public void fuse(RecordGroup<Event, Attribute> group, Event fusedRecord, Processable<Correspondence<Attribute, Event>> schemaCorrespondences, Attribute attribute) {
        FusedValue<List<DateTime>, Event, Attribute> fused = getFusedValue(group, schemaCorrespondences, attribute);
        fusedRecord.setDates(fused.getValue());
        fusedRecord.setAttributeProvenance(Event.DATES, fused.getOriginalIds());
    }

}
