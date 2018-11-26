package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.numeric;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.*;

import junit.framework.TestCase;
public class MedianTest extends TestCase {

    @Test
    public void testResolveConflict() {
        Collection<FusibleValue<Double, Record, Attribute>> values = new LinkedList<>();
        Median<Record,Attribute> median = new Median<>();

        FusedValue<Double, Record, Attribute> result = median.resolveConflict(values);
        assertNull(result.getValue());

        values.add(new FusibleValue<Double,Record,Attribute>(1.0, null, null));
        result = median.resolveConflict(values);
        assertEquals(1.0d, result.getValue());

        values.add(new FusibleValue<Double,Record,Attribute>(2.0, null, null));
        result = median.resolveConflict(values);
        assertEquals(1.5d, result.getValue());

        values.add(new FusibleValue<Double,Record,Attribute>(3.0, null, null));
        result = median.resolveConflict(values);
        assertEquals(2.0d, result.getValue());
    }

}