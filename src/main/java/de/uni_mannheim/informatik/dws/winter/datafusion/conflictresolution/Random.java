package de.uni_mannheim.informatik.wdi.datafusion.conflictresolution;

import de.uni_mannheim.informatik.wdi.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.wdi.model.Fusable;
import de.uni_mannheim.informatik.wdi.model.FusableValue;
import de.uni_mannheim.informatik.wdi.model.FusedValue;
import de.uni_mannheim.informatik.wdi.model.Matchable;

import java.util.Collection;
import java.util.Random;


/**
 * Random {@link ConflictResolutionFunction}: Returns a random value.
 * @author Daniel Ringler
 *
 * @param <ValueType>
 * @param <RecordType>
 */
public class Random<ValueType, RecordType extends Matchable & Fusable<SchemaElementType>, SchemaElementType> extends ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> {

    @Override
    public FusedValue<ValueType, RecordType, SchemaElementType> resolveConflict(
            Collection<FusableValue<ValueType, RecordType, SchemaElementType>> values) {

        if (values.size()>0) {
            int randomItem = new Random().nextInt(values.size());
            int i = 0;
            for(FusableValue<ValueType, RecordType, SchemaElementType> value : values) {
                if (randomItem == i) {
                    return new FusedValue<>(value);
                }
                i++;
            }
        }
        FusableValue<ValueType, RecordType, SchemaElementType> random = null;
        return new FusedValue<>(random);





    }

}
