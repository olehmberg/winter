package de.uni_mannheim.informatik.dws.winter.matching.rules.comparators;

import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.RecordComparatorJaccard;

import java.util.List;

/**
 * Interface for all {@link AbstractRecord} comparators.
 *
 * A {@link MissingValueComparator} comparator is a special {@link Comparator},
 * which checks if one of the compared values is null.
 * If a null value is detected, the upfront provided list of penalised comparators
 * can be used to ignore similarity scores calculated by these comparators.
 * Instead the aggregated similarity can be penalised by a penalty value.
 *
 * For an example of a specific attribute comparator, see
 * {@link RecordComparatorJaccard}.
 *
 *
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 *
 * @param <RecordType>
 *            the type of records that are compared with this comparator
 * @param <SchemaElementType>
 *            the type of schema elements that are used in the schema of
 *            RecordType
 */

public interface MissingValueComparator<RecordType extends Matchable, SchemaElementType extends Matchable>
        extends Comparator<RecordType, SchemaElementType>{

    /**
     * Returns a list of penalised comparators
     * @return List of penalised comparators
     */
    List<Comparator<RecordType, SchemaElementType>> getPenalisedComparators();

    /**
     * Add a penalised Comparator
     * @param comparator This comparator can be penalised if a null value is detected.
     */
    void addPenalisedComparator(Comparator<RecordType, SchemaElementType> comparator);

    /**
     * Returns a penalty value
     * @return Penalty value
     */
    Double getPenalty();
}
