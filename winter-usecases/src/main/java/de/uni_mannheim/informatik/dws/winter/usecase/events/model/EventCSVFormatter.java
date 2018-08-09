package de.uni_mannheim.informatik.dws.winter.usecase.events.model;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVDataSetFormatter;


/**
 * {@link CSVDataSetFormatter} for {@link Event}s.
 *
 * @author Daniel Ringler
 *
 */
public class EventCSVFormatter extends CSVDataSetFormatter<Event,Attribute> {

    @Override
    public String[] getHeader(List<Attribute> orderedHeader) {
		List<String> names = new ArrayList<>();

		for (Attribute att : orderedHeader) {
			names.add(att.getIdentifier());
		}

		return names.toArray(new String[names.size()]);
    }

    @Override
    public String[] format(Event record, DataSet<Event, Attribute> dataset, List<Attribute> orderedHeader) {//}, char s) {
        return record.getAllAttributeValues(',');//s);
    }
}
