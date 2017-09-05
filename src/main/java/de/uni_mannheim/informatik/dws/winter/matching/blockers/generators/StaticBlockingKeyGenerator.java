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

import de.uni_mannheim.informatik.dws.winter.matching.blockers.AbstractBlocker;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Implementation of a {@link BlockingKeyGenerator} which assigns to all given
 * records, always the same static key. Which means that a {@link AbstractBlocker}
 * making use of this {@link BlockingKeyGenerator} will not do any sophisticated
 * blocking.
 * 
 * @author Robert Meusel (robert@dwslab.de)
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * 
 */
public class StaticBlockingKeyGenerator<RecordType extends Matchable, CorrespondenceType extends Matchable> extends
		BlockingKeyGenerator<RecordType, CorrespondenceType, RecordType> {

	private static final long serialVersionUID = 1L;
	/**
	 * Could be anything
	 */
	private static final String STATIC_BLOCKING_KEY = "AAA";


	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.matching.blocking.BlockingKeyGenerator#generateBlockingKeys(de.uni_mannheim.informatik.wdi.model.Matchable, de.uni_mannheim.informatik.wdi.model.Result, de.uni_mannheim.informatik.wdi.processing.DatasetIterator)
	 */
	@Override
	public void generateBlockingKeys(RecordType record,
			Processable<Correspondence<CorrespondenceType, Matchable>> correspondences,
			DataIterator<Pair<String, RecordType>> resultCollector) {
		resultCollector.next(new Pair<>(STATIC_BLOCKING_KEY, record));
	}

}
