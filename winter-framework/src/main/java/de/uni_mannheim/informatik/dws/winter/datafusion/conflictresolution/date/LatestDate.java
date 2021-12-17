package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.date;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

import java.time.LocalDateTime;
import java.util.Collection;

public class LatestDate<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends ConflictResolutionFunction<LocalDateTime, RecordType, SchemaElementType> {

	@Override
	public FusedValue<LocalDateTime, RecordType, SchemaElementType> resolveConflict(
			Collection<FusibleValue<LocalDateTime, RecordType, SchemaElementType>> values) {

		FusibleValue<LocalDateTime, RecordType, SchemaElementType> latest = null;

		for(FusibleValue<LocalDateTime, RecordType, SchemaElementType> value : values) {
			if(latest == null || value.getValue().isAfter(latest.getValue())) {
				latest = value;
			}
		}

		return new FusedValue<>(latest);
	}

}
