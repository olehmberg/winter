package de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers;

import org.joda.time.DateTime;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.meta.FavourSources;
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
public class EventDateFuserFavourSources extends AttributeValueFuser<DateTime, Event, Attribute> {
    public EventDateFuserFavourSources() {
        super(new FavourSources<DateTime, Event, Attribute>());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        return record.hasValue(Event.DATES);
    }

    @Override
    public DateTime getValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        if (record.getDates().size()>0) {
            for(DateTime date : record.getDates()) {
                    return date;
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
