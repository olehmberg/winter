package de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.string.ShortestString;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;

/**
 * {@link AttributeValueFuser} for the titles of {@link Event}s.
 * based on TitleFuserShortestString
 * created on 2017-01-04
 * @author Daniel Ringler
 *
 */
public class EventLabelFuserShortestString extends
        AttributeValueFuser<String, Event, Attribute> {
    public EventLabelFuserShortestString() {
        super(new ShortestString<Event, Attribute>());
    }

    @Override
    public void fuse(RecordGroup<Event, Attribute> group, Event fusedRecord, Processable<Correspondence<Attribute, Matchable>> schemaCorrespondences, Attribute attribute) {
        // get the fused value
        FusedValue<String, Event, Attribute> fused = getFusedValue(group, schemaCorrespondences, attribute);

        // set the value for the fused record
        fusedRecord.setSingleLabel(fused.getValue());

        // add provenance info
        fusedRecord.setAttributeProvenance(Event.LABELS, fused.getOriginalIds());
    }

    @Override
    public boolean hasValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        return record.hasValue(Event.LABELS);
    }

    @Override
    protected String getValue(Event record, Correspondence<Attribute, Matchable> correspondence) {
        String labels = "";
        for (String label : record.getLabels()) {
            labels += label + ",";
        }
        if (labels.length()>0)
            labels = labels.substring(0,labels.length()-1);
        return labels;
    }

}
