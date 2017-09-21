package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution;

import java.util.Collection;
import java.util.Random;

import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;


/**
 * Random {@link ConflictResolutionFunction}: Returns a random value.
 * @author Daniel Ringler
 *
 * @param <ValueType>	the type of the values that are fused
 * @param <RecordType>	the type that represents a record
 */

public class RandomValue<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends
            ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> {


        @Override
    public FusedValue<ValueType, RecordType, SchemaElementType> resolveConflict(
            Collection<FusibleValue<ValueType, RecordType, SchemaElementType>> values) {

        if (values.size()>0) {
            int randomItem = new Random().nextInt(values.size());
            int i = 0;
            for(FusibleValue<ValueType, RecordType, SchemaElementType> value : values) {
                if (randomItem == i) {
                    return new FusedValue<>(value);
                }
                i++;
            }
        }
        FusibleValue<ValueType, RecordType, SchemaElementType> random = null;
        return new FusedValue<>(random);

    }

}
