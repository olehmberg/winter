package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.date;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.numeric.Maximum;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import junit.framework.TestCase;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;

public class EarliestDateTest extends TestCase {

    @Test
    public void testResolveConflict() {
        // Setup
        Collection<FusibleValue<LocalDateTime, Record, Attribute>> values = new LinkedList<>();
        EarliestDate<Record,Attribute> earliestDate = new EarliestDate<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime firstDate = LocalDateTime.parse("2020-01-01 12:30", formatter);
        LocalDateTime secondDate = LocalDateTime.parse("2020-01-02 10:30", formatter);
        LocalDateTime thirdDate = LocalDateTime.parse("2021-01-01 09:30", formatter);

        // Run Tests
        FusedValue<LocalDateTime, Record, Attribute> result = earliestDate.resolveConflict(values);
        assertNull(result.getValue());

        values.add(new FusibleValue<>(firstDate, null, null));
        result = earliestDate.resolveConflict(values);
        assertEquals(firstDate, result.getValue());

        values.add(new FusibleValue<>(secondDate, null, null));
        result = earliestDate.resolveConflict(values);
        assertEquals(firstDate, result.getValue());

        values.add(new FusibleValue<>(thirdDate, null, null));
        result = earliestDate.resolveConflict(values);
        assertEquals(firstDate, result.getValue());
    }


}