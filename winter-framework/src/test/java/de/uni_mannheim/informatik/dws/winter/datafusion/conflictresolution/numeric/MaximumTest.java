package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.numeric;

import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;

public class MaximumTest extends TestCase {

    @Test
    public void testResolveConflict() {
        Collection<FusibleValue<Double, Record, Attribute>> values = new LinkedList<>();
        Maximum<Record,Attribute> maximum = new Maximum<>();

        FusedValue<Double, Record, Attribute> result = maximum.resolveConflict(values);
        assertNull(result.getValue());

        values.add(new FusibleValue<>(1.0, null, null));
        result = maximum.resolveConflict(values);
        assertEquals(1.0d, result.getValue());

        values.add(new FusibleValue<>(2.0, null, null));
        result = maximum.resolveConflict(values);
        assertEquals(2.0d, result.getValue());

        values.add(new FusibleValue<>(3.0, null, null));
        result = maximum.resolveConflict(values);
        assertEquals(3.0d, result.getValue());
    }

}