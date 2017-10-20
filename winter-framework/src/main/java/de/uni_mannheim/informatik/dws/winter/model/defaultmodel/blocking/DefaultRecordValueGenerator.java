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
package de.uni_mannheim.informatik.dws.winter.model.defaultmodel.blocking;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;

/**
 * A blocking key generator that uses the values of a record as blocking keys
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class DefaultRecordValueGenerator extends BlockingKeyGenerator<Record, MatchableValue, Record> {

	private static final long serialVersionUID = 1L;
	DataSet<Attribute, Attribute> schema;
	
	public DefaultRecordValueGenerator(DataSet<Attribute, Attribute> schema) {
		this.schema = schema;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.generators.BlockingKeyGenerator#mapRecordToKey(de.uni_mannheim.informatik.wdi.model.Pair, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecordToKey(Pair<Record, Processable<Correspondence<MatchableValue, Matchable>>> pair,
			DataIterator<Pair<String, Pair<Record, Processable<Correspondence<MatchableValue, Matchable>>>>> resultCollector) {

		Record record = pair.getFirst();
		for(Attribute a : schema.get()) {
			if(record.hasValue(a)) {
				
				Processable<Correspondence<MatchableValue, Matchable>> causes = new ProcessableCollection<>();
				MatchableValue value = new MatchableValue(record.getValue(a), record.getIdentifier(), a.getIdentifier());
				Correspondence<MatchableValue, Matchable> causeCor = new Correspondence<>(value, value, 1.0);
				causes.add(causeCor);
				
				resultCollector.next(new Pair<>(value.getValue().toString(), new Pair<>(record, causes)));
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.generators.BlockingKeyGenerator#generateBlockingKeys(de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.Result, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void generateBlockingKeys(Record record, Processable<Correspondence<MatchableValue, Matchable>> correspondences,
			DataIterator<Pair<String, Record>> resultCollector) {
	}

}
