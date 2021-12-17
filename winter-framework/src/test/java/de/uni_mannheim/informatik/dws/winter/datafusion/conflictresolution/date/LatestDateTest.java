package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.date;

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

public class LatestDateTest extends TestCase {

    @Test
    public void testResolveConflict() {
        // Setup
        Collection<FusibleValue<LocalDateTime, Record, Attribute>> values = new LinkedList<>();
        LatestDate<Record,Attribute> latestDate = new LatestDate<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime firstDate = LocalDateTime.parse("2020-01-01 12:30", formatter);
        LocalDateTime secondDate = LocalDateTime.parse("2020-01-02 10:30", formatter);
        LocalDateTime thirdDate = LocalDateTime.parse("2021-01-01 09:30", formatter);

        // Run Tests
        FusedValue<LocalDateTime, Record, Attribute> result = latestDate.resolveConflict(values);
        assertNull(result.getValue());

        values.add(new FusibleValue<>(firstDate, null, null));
        result = latestDate.resolveConflict(values);
        assertEquals(firstDate, result.getValue());

        values.add(new FusibleValue<>(secondDate, null, null));
        result = latestDate.resolveConflict(values);
        assertEquals(secondDate, result.getValue());

        values.add(new FusibleValue<>(thirdDate, null, null));
        result = latestDate.resolveConflict(values);
        assertEquals(thirdDate, result.getValue());
    }


}