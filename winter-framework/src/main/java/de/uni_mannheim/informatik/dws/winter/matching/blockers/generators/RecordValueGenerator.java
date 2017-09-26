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
package de.uni_mannheim.informatik.dws.winter.matching.blockers.generators;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollector;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public abstract class RecordValueGenerator<RecordType extends Matchable> extends BlockingKeyGenerator<RecordType, MatchableValue, RecordType> {

	private static final long serialVersionUID = 1L;

	@Override
	public void mapRecordToKey(Pair<RecordType, Processable<Correspondence<MatchableValue, Matchable>>> pair,
			DataIterator<Pair<String, Pair<RecordType, Processable<Correspondence<MatchableValue, Matchable>>>>> resultCollector) {

		
		ProcessableCollector<Pair<String, RecordType>> collector = new ProcessableCollector<>();
		collector.setResult(new ProcessableCollection<>());
		collector.initialise();
		
		// execute the blocking function
		generateBlockingKeys(pair.getFirst(), pair.getSecond(), collector);
		
		collector.finalise();
		
		for(Pair<String, RecordType> p : collector.getResult().get()) {
			
			// create causal correspondence from the blocking key
			Processable<Correspondence<MatchableValue, Matchable>> causes = new ProcessableCollection<>();
			MatchableValue matchableValue = new MatchableValue(p.getFirst(), pair.getFirst().getIdentifier(), p.getSecond().getIdentifier());
			Correspondence<MatchableValue, Matchable> causeCor = new Correspondence<>(matchableValue, matchableValue, 1.0);
			causes.add(causeCor);
			
			resultCollector.next(new Pair<>(matchableValue.getValue().toString(), new Pair<>(p.getSecond(), causes)));
			
		}
		
	}
	
}
