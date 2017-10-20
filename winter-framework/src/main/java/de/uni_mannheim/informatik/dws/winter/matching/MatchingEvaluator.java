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
package de.uni_mannheim.informatik.dws.winter.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Performance;

/**
 * Evaluates a set of {@link Correspondence}s against a
 * {@link MatchingGoldStandard}.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>	the type of records between which the correspondences exist
 * @param <SchemaElementType>	the type of the causal correspondences
 */
public class MatchingEvaluator<RecordType extends Matchable, SchemaElementType extends Matchable> {

	private boolean verbose = false;

	public MatchingEvaluator() {
	}

	public MatchingEvaluator(boolean isVerbose) {
		verbose = isVerbose;
	}

	/**
	 * Evaluates the given correspondences against the gold standard
	 * 
	 * @param correspondences
	 *            the correspondences to evaluate
	 * @param goldStandard
	 *            the gold standard
	 * @return the result of the evaluation
	 */
	public Performance evaluateMatching(
			Collection<Correspondence<RecordType, SchemaElementType>> correspondences,
			MatchingGoldStandard goldStandard) {
		int correct = 0;
		int matched = 0;
		int correct_max = goldStandard.getPositiveExamples().size();

		// keep a list of all unmatched positives for later output
		List<Pair<String, String>> positives = new ArrayList<>(
				goldStandard.getPositiveExamples());

		for (Correspondence<RecordType, SchemaElementType> correspondence : correspondences) {
			if (goldStandard.containsPositive(correspondence.getFirstRecord(),
					correspondence.getSecondRecord())) {
				correct++;
				matched++;

				if (verbose) {
					System.out.println(String
							.format("[correct] %s,%s,%s", correspondence
									.getFirstRecord().getIdentifier(),
									correspondence.getSecondRecord()
											.getIdentifier(), Double
											.toString(correspondence
													.getSimilarityScore())));

					// remove pair from positives
					Iterator<Pair<String, String>> it = positives.iterator();
					while (it.hasNext()) {
						Pair<String, String> p = it.next();
						String id1 = correspondence.getFirstRecord()
								.getIdentifier();
						String id2 = correspondence.getSecondRecord()
								.getIdentifier();

						if (p.getFirst().equals(id1)
								&& p.getSecond().equals(id2)
								|| p.getFirst().equals(id2)
								&& p.getSecond().equals(id1)) {
							it.remove();
						}
					}
				}
			} else if (goldStandard.isComplete() || goldStandard.containsNegative(
					correspondence.getFirstRecord(),
					correspondence.getSecondRecord())) {
				matched++;

				if (verbose) {
					System.out.println(String
							.format("[wrong] %s,%s,%s", correspondence
									.getFirstRecord().getIdentifier(),
									correspondence.getSecondRecord()
											.getIdentifier(), Double
											.toString(correspondence
													.getSimilarityScore())));
				}
			}
		}

		if (verbose) {
			// print all missing positive examples
			for (Pair<String, String> p : positives) {
				System.out.println(String.format("[missing] %s,%s",
						p.getFirst(), p.getSecond()));
			}
		}

		return new Performance(correct, matched, correct_max);
	}

}
