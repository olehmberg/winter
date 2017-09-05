package de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers;

import java.util.Random;

import org.joda.time.DateTime;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.RandomValue;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;

/**
 * {@link AttributeValueFuser} for the date of {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventDateFuserRandom extends AttributeValueFuser<DateTime, Event, Attribute> {
    public EventDateFuserRandom() {
    super(new RandomValue<DateTime, Event, Attribute>());
}

    @Override
    public boolean hasValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        return record.hasValue(Event.DATES);
    }

    @Override
    protected DateTime getValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        if (record.getDates().size()>0) {
            int randomItem = new Random().nextInt(record.getDates().size());
            int i = 0;
            for(DateTime date : record.getDates()) {
                if (randomItem == i) {
                    return date;
                }
                i++;
            }
        }
        return null;
    }



    @Override
    public void fuse(RecordGroup<Event, Attribute> group, Event fusedRecord, Processable<Correspondence<Attribute, Matchable>> schemaCorrespondences, Attribute attribute) {
        FusedValue<DateTime, Event, Attribute> fused = getFusedValue(group, schemaCorrespondences, attribute);
        fusedRecord.setSingleDate(fused.getValue());
        fusedRecord.setAttributeProvenance(Event.DATES, fused.getOriginalIds());
    }

}
