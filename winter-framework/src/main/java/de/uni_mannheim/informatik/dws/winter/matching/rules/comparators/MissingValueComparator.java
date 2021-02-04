package de.uni_mannheim.informatik.dws.winter.matching.rules.comparators;

import de.uni_mannheim.informatik.dws.winter.model.Matchable;

import java.io.Serializable;
import java.util.List;

public interface MissingValueComparator<RecordType extends Matchable, SchemaElementType extends Matchable>
        extends Comparator<RecordType, SchemaElementType>{

    List<Comparator<RecordType, SchemaElementType>> getPenalisedComparators();

    void addPenalisedComparator(Comparator<RecordType, SchemaElementType> comparator);

    Double getPenalty();
}
