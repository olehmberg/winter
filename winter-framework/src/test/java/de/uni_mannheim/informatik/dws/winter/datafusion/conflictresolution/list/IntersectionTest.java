package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.list;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.*;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import junit.framework.TestCase;
public class IntersectionTest extends TestCase {

    @Test
    public void testResolveConflict() {
        Collection<FusibleValue<List<String>, Record, Attribute>> values = new LinkedList<>();
        Intersection<String, Record,Attribute> intersection = new Intersection<>();

        FusedValue<List<String>, Record, Attribute> result = intersection.resolveConflict(null);
        assertEquals(0, result.getValue().size());

        result = intersection.resolveConflict(values);
        assertEquals(0, result.getValue().size());

        values.add(new FusibleValue<List<String>,Record,Attribute>(Q.toList("a", "b", "c"), null, null));
        result = intersection.resolveConflict(values);
        assertEquals(Q.toList("a", "b", "c"), result.getValue());

        values.add(new FusibleValue<List<String>,Record,Attribute>(Q.toList("a", "d", "e"), null, null));
        result = intersection.resolveConflict(values);
        assertEquals(Q.toList("a"), result.getValue());

        values.add(new FusibleValue<List<String>,Record,Attribute>(Q.toList("a", "e", "f"), null, null));
        result = intersection.resolveConflict(values);
        assertEquals(Q.toList("a"), result.getValue());

        values.add(new FusibleValue<List<String>,Record,Attribute>(null, null, null));
        result = intersection.resolveConflict(values);
        assertEquals(0, result.getValue().size());
    }

}