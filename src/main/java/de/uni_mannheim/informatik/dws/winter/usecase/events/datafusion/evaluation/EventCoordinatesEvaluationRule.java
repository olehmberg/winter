package de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.evaluation;

import de.uni_mannheim.informatik.dws.winter.datafusion.EvaluationRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;



/**
 * {@link EvaluationRule} for the coordinates of {@link Event}s. The rule simply
 * compares the coordinates of two {@link Event}s and returns true, in case two
 * coordinates are exactly the same.
 * Created on 2017-01-04
 * @author Daniel Ringler
 *
 */
public class EventCoordinatesEvaluationRule extends EvaluationRule<Event, Attribute> {

    @Override
    public boolean isEqual(Event record1, Event record2, Attribute attribute) {
        //compare all labels
        for (Pair<Double, Double> r1p : record1.getCoordinates()) {
            for (Pair<Double, Double> r2p : record2.getCoordinates()) {
                if (r1p.equals(r2p))
                    return true;
            }
        }
        //return false if none of the coordinate pairs matches
        return false;
    }

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.datafusion.EvaluationRule#isEqual(java.lang.Object, java.lang.Object, de.uni_mannheim.informatik.wdi.model.Correspondence)
     */
    @Override
    public boolean isEqual(Event record1, Event record2,
                           Correspondence<Attribute, Event> schemaCorrespondence) {
        return isEqual(record1, record2, (Attribute) null);
    }

}
