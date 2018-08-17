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
package de.uni_mannheim.informatik.dws.winter.matching.blockers;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * Implementation of a standard {@link AbstractBlocker} based on blocking keys.
 * All records for which the same blocking key is generated are returned as
 * pairs.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 * @param <RecordType>
 *            the type of records which are the input for the blocking operation
 * @param <SchemaElementType>
 *            the type of schema elements that are used in the schema of
 *            RecordType
 * @param <CorrespondenceType>
 *            the type of correspondences which are the input for the blocking
 *            operation
 */
public class GoldStandardBlocker<RecordType extends Matchable, SchemaElementType extends Matchable, CorrespondenceType extends Matchable>
		extends AbstractBlocker<RecordType, SchemaElementType, CorrespondenceType>
		implements Blocker<RecordType, SchemaElementType, RecordType, CorrespondenceType> {

	private MatchingGoldStandard goldstandard;

	private static final Logger logger = WinterLogManager.getLogger();

	public GoldStandardBlocker(MatchingGoldStandard goldstandard) {
		this.goldstandard = goldstandard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_mannheim.informatik.wdi.matching.blocking.Blocker#runBlocking(de.
	 * uni_mannheim.informatik.wdi.model.DataSet,
	 * de.uni_mannheim.informatik.wdi.model.DataSet,
	 * de.uni_mannheim.informatik.wdi.model.ResultSet,
	 * de.uni_mannheim.informatik.wdi.matching.MatchingEngine)
	 */
	@Override
	public Processable<Correspondence<RecordType, CorrespondenceType>> runBlocking(
			DataSet<RecordType, SchemaElementType> dataset1, DataSet<RecordType, SchemaElementType> dataset2,
			Processable<Correspondence<CorrespondenceType, Matchable>> schemaCorrespondences) {

		ProcessableCollection<Correspondence<RecordType, CorrespondenceType>> result = new ProcessableCollection<Correspondence<RecordType, CorrespondenceType>>();

		for (Pair<String, String> positivePair : this.goldstandard.getNegativeExamples()) {
			RecordType record1 = dataset1.getRecord(positivePair.getFirst());
			if (record1 != null) {
				RecordType record2 = dataset2.getRecord(positivePair.getSecond());
				if (record2 != null) {
					result.add(new Correspondence<RecordType, CorrespondenceType>(record1, record2, 1.0, null));
				}
			} else {
				record1 = dataset1.getRecord(positivePair.getSecond());
				if (record1 != null) {
					RecordType record2 = dataset2.getRecord(positivePair.getFirst());
					if (record2 != null) {
						result.add(new Correspondence<RecordType, CorrespondenceType>(record1, record2, 0.0, null));
					}
				}
			}
		}

		for (Pair<String, String> negativePair : this.goldstandard.getNegativeExamples()) {
			RecordType record1 = dataset1.getRecord(negativePair.getFirst());
			if (record1 != null) {
				RecordType record2 = dataset2.getRecord(negativePair.getSecond());
				if (record2 != null) {
					result.add(new Correspondence<RecordType, CorrespondenceType>(record1, record2, 1.0, null));
				}
			} else {
				record1 = dataset1.getRecord(negativePair.getSecond());
				if (record1 != null) {
					RecordType record2 = dataset2.getRecord(negativePair.getFirst());
					if (record2 != null) {
						result.add(new Correspondence<RecordType, CorrespondenceType>(record1, record2, 1.0, null));
					}
				}
			}
		}
		logger.info(String.format("Created %d blocked pairs from the goldstandard!", result.size()));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_mannheim.informatik.wdi.matching.blocking.Blocker#runBlocking(de.
	 * uni_mannheim.informatik.wdi.model.DataSet, boolean,
	 * de.uni_mannheim.informatik.wdi.model.ResultSet,
	 * de.uni_mannheim.informatik.wdi.matching.MatchingEngine)
	 */


}
