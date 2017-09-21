/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.meta;

import java.util.Collection;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * Favour sources {@link ConflictResolutionFunction}: returns the value from the
 * dataset with the highest data set score, which can represent the rating of
 * this dataset or any other score
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <ValueType>	the type of the values that are fused
 * @param <RecordType>	the type that represents a record
 */
public class FavourSources<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable>
		extends ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> {

	@Override
	public FusedValue<ValueType, RecordType, SchemaElementType> resolveConflict(
			Collection<FusibleValue<ValueType, RecordType, SchemaElementType>> values) {

		FusibleValue<ValueType, RecordType, SchemaElementType> highestScore = null;

		for (FusibleValue<ValueType, RecordType, SchemaElementType> value : values) {
			if (highestScore == null
					|| value.getDataSourceScore() > highestScore
							.getDataSourceScore()) {
				highestScore = value;
			}
		}

		return new FusedValue<>(highestScore);
	}

}
