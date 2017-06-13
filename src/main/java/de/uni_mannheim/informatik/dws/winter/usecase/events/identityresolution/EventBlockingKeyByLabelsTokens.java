package de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.RecordBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.DatasetIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;

/**
 * {@link BlockingKeyGenerator} for {@link Event}s, which generates a blocking
 * key based on all tokens of all labels
 *
 * @author Daniel Ringler
 *
 */
public class EventBlockingKeyByLabelsTokens extends
        RecordBlockingKeyGenerator<Event, Attribute> {

    private static final long serialVersionUID = 1L;


    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.wdi.matching.blocking.generators.BlockingKeyGenerator#generateBlockingKeys(de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.Result, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
     */
    @Override
    public void generateBlockingKeys(Event record, Processable<Correspondence<Attribute, Matchable>> correspondences,
                                     DatasetIterator<Pair<String, Event>> resultCollector) {
        for (String label : record.getLabels()) {
            for (String token : label.split("\\s+")) {
                resultCollector.next(new Pair<>(token, record));
            }
        }
    }

}