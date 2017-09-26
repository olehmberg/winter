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
package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * Vote {@link ConflictResolutionFunction}: returns the most frequent value, in
 * case of two or more similar frequent values the first one in the list of
 * {@link FusibleValue}s is returned.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <ValueType>	the type of the values that are fused
 * @param <RecordType>	the type that represents a record
 */
public class Voting<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends
		ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> {

	@Override
	public FusedValue<ValueType, RecordType, SchemaElementType> resolveConflict(
			Collection<FusibleValue<ValueType, RecordType, SchemaElementType>> values) {

		// determine the frequencies of all values
		Map<ValueType, Integer> frequencies = new HashMap<>();

		for (FusibleValue<ValueType, RecordType, SchemaElementType> value : values) {
			Integer freq = frequencies.get(value.getValue());
			if (freq == null) {
				freq = 0;
			}
			frequencies.put(value.getValue(), freq + 1);
		}

		// find the most frequent value
		ValueType mostFrequent = null;

		for (ValueType value : frequencies.keySet()) {
			if (mostFrequent == null
					|| frequencies.get(value) > frequencies.get(mostFrequent)) {
				mostFrequent = value;
			}
		}

		FusedValue<ValueType, RecordType, SchemaElementType> result = new FusedValue<>(
				mostFrequent);

		// collect all original records with the most frequent value
		for (FusibleValue<ValueType, RecordType, SchemaElementType> value : values) {
			if (value.getValue().equals(mostFrequent)) {
				result.addOriginalRecord(value);
			}
		}

		return result;
	}

}
