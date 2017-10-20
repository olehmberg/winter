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
package de.uni_mannheim.informatik.dws.winter.processing;

import java.io.Serializable;

import de.uni_mannheim.informatik.dws.winter.model.Pair;

/**
 * 
 * Interface for aggregation operations.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public interface DataAggregator<KeyType, RecordType, ResultType> extends Serializable{

	Pair<ResultType,Object> initialise(KeyType keyValue);
	
	Pair<ResultType,Object> aggregate(ResultType previousResult, RecordType record, Object state);
	
	Pair<ResultType,Object> merge(Pair<ResultType, Object> intermediateResult1, Pair<ResultType, Object> intermediateResult2);
	
	default ResultType createFinalValue(KeyType keyValue, ResultType result, Object state) {
		return result;
	}
	
	default Pair<ResultType, Object> stateless(ResultType result) {
		return new Pair<>(result,null);
	}
	
	default Pair<ResultType, Object> state(ResultType result, Object state) {
		return new Pair<>(result,state);
	}
	
}
