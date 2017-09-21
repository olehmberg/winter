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
package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.list;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * Union {@link ConflictResolutionFunction}: Returns the union of all lists of
 * values
 *
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <ValueType>	the type of the values that are fused
 * @param <RecordType>	the type that represents a record
 */
public class Union<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends
		ConflictResolutionFunction<List<ValueType>, RecordType, SchemaElementType> {

	@Override
	public FusedValue<List<ValueType>, RecordType, SchemaElementType> resolveConflict(
			Collection<FusibleValue<List<ValueType>, RecordType, SchemaElementType>> values) {

		HashSet<ValueType> union = new HashSet<>();

		for (FusibleValue<List<ValueType>, RecordType, SchemaElementType> value : values) {
			union.addAll(value.getValue());
		}
		List<ValueType> list = new LinkedList<>(union);
		FusedValue<List<ValueType>, RecordType, SchemaElementType> fused = new FusedValue<>(list);

		for (FusibleValue<List<ValueType>, RecordType, SchemaElementType> value : values) {
			fused.addOriginalRecord(value);
		}

		return fused;
	}

}
