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
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollector;
import de.uni_mannheim.informatik.dws.winter.processing.RecordKeyValueMapper;
import de.uni_mannheim.informatik.dws.winter.processing.RecordMapper;

/**
 * Interface for all blocking functions. A blocking function returns the
 * blocking key for a given record.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>			the type of records which are the input for the blocking operation 
 * @param <CorrespondenceType>	the type of correspondences which are the input for the blocking operation
 * @param <BlockedType>			the type of record which is actually blocked
 */
public abstract class BlockingKeyGenerator<RecordType extends Matchable, CorrespondenceType extends Matchable, BlockedType extends Matchable> 
	implements RecordKeyValueMapper<String, Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>, Pair<BlockedType, Processable<Correspondence<CorrespondenceType, Matchable>>>>,
	RecordMapper<Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>>, Pair<String, Pair<BlockedType,Processable<Correspondence<CorrespondenceType, Matchable>>>>>
{
	
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.RecordMapper#mapRecord(java.lang.Object, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecord(Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>> record, DataIterator<Pair<String, Pair<BlockedType,Processable<Correspondence<CorrespondenceType, Matchable>>>>> resultCollector) {
		mapRecordToKey(record, resultCollector);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.processing.RecordKeyValueMapper#mapRecordToKey(java.lang.Object, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void mapRecordToKey(Pair<RecordType, Processable<Correspondence<CorrespondenceType, Matchable>>> record,
			DataIterator<Pair<String, Pair<BlockedType,Processable<Correspondence<CorrespondenceType, Matchable>>>>> resultCollector) {
		
		ProcessableCollector<Pair<String, BlockedType>> collector = new ProcessableCollector<>();
		collector.setResult(new ProcessableCollection<>());
		collector.initialise();
		
		// execute the blocking funtion
		generateBlockingKeys(record.getFirst(), record.getSecond(), collector);
		
		collector.finalise();
		
		for(Pair<String, BlockedType> p : collector.getResult().get()) {
			// and make sure that the correspondences are available with the result
			resultCollector.next(new Pair<>(p.getFirst(), new Pair<>(p.getSecond(), record.getSecond())));
		}
	}
	
	/**
	 * 
	 * Generates the blocking key(s) for the given record.
	 * 
	 * @param record		the record for which the blocking keys should be generated
	 * @param correspondences	the correspondences for the record
	 * @param resultCollector	the {@link DataIterator} that collects the results
	 */
	public abstract void generateBlockingKeys(RecordType record, Processable<Correspondence<CorrespondenceType, Matchable>> correspondences, DataIterator<Pair<String, BlockedType>> resultCollector);

}
