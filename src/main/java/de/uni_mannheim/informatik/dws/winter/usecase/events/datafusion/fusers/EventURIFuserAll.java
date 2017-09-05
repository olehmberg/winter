package de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers;

import java.util.List;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.list.Union;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;


/**
 * {@link AttributeValueFuser} for the URIs of {@link Event}s.
 * based on ActorFuserUnion
 * created on 2017-01-06
 * @author Daniel Ringler
 *
 */
public class EventURIFuserAll extends
        AttributeValueFuser<List<String>, Event, Attribute> {


    public EventURIFuserAll() {
        super(new Union<String, Event, Attribute>());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        return record.hasValue(Event.URIS);
    }

    @Override
    protected List<String> getValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        return record.getUris();
    }

    @Override
    public void fuse(RecordGroup<Event, Attribute> group, Event fusedRecord, Processable<Correspondence<Attribute, Matchable>> schemaCorrespondences, Attribute attribute) {

        // get the fused value
        FusedValue<List<String>, Event, Attribute> fused = getFusedValue(group, schemaCorrespondences, attribute);

        // set the value for the fused record
        fusedRecord.setURIs(fused.getValue());

        // add provenance info
        fusedRecord.setAttributeProvenance(Event.URIS, fused.getOriginalIds());
    }
}
