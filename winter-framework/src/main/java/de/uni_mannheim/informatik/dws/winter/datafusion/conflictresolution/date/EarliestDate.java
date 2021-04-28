package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.date;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

import java.time.LocalDateTime;
import java.util.Collection;

public class EarliestDate<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends ConflictResolutionFunction<LocalDateTime, RecordType, SchemaElementType> {

	@Override
	public FusedValue<LocalDateTime, RecordType, SchemaElementType> resolveConflict(
			Collection<FusibleValue<LocalDateTime, RecordType, SchemaElementType>> values) {

		FusibleValue<LocalDateTime, RecordType, SchemaElementType> earliest = null;

		for(FusibleValue<LocalDateTime, RecordType, SchemaElementType> value : values) {
			if(earliest == null || value.getValue().isBefore(earliest.getValue())) {
				earliest = value;
			}
		}
		
		return new FusedValue<>(earliest);
	}

}
