package de.uni_mannheim.informatik.dws.winter.similarity.string.generator;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

public abstract class TokenGenerator<RecordType extends Matchable, CorrespondenceType extends Matchable>
	extends BlockingKeyGenerator<RecordType, CorrespondenceType, RecordType> {
    @Override
    public void generateBlockingKeys(RecordType record, Processable<Correspondence<CorrespondenceType, Matchable>> correspondences, DataIterator<Pair<String, RecordType>> resultCollector) {
        generateTokens(record, correspondences, resultCollector);
    }

    /**
     *
     * Generates the tokens for the given record.
     *
     * @param record		the record for which the blocking keys should be generated
     * @param correspondences	the correspondences for the record
     * @param resultCollector	the {@link DataIterator} that collects the results
     */
    public abstract void generateTokens(RecordType record, Processable<Correspondence<CorrespondenceType, Matchable>> correspondences, DataIterator<Pair<String, RecordType>> resultCollector);

    /**
     *
     * Tokenizes the provided String
     *
     * @param value		        the String value that should be tokenized
     * @return                  an arrary of tokenised Strings for the given value
     */
    public abstract String[] tokenizeString(String value);


}
