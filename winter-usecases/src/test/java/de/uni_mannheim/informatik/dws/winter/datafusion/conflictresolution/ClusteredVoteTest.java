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

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.ClusteredVote;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import junit.framework.TestCase;

public class ClusteredVoteTest extends TestCase {

	public void testResolveConflict() {
		ClusteredVote<String, Movie, Attribute> crf = new ClusteredVote<>(
				new LevenshteinSimilarity(), 0.0);

		List<FusibleValue<String, Movie, Attribute>> cluster1 = new ArrayList<>();
		cluster1.add(new FusibleValue<String, Movie, Attribute>("hi", null, null));
		cluster1.add(new FusibleValue<String, Movie, Attribute>("hi1", null, null));
		cluster1.add(new FusibleValue<String, Movie, Attribute>("hello1", null,
				null));
		cluster1.add(new FusibleValue<String, Movie, Attribute>("hello", null, null));
		cluster1.add(new FusibleValue<String, Movie, Attribute>("hello2", null,
				null));

		FusedValue<String, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals("hello1", resolvedValue.getValue());
	}

	public void testResolveConflict1() {
		ClusteredVote<String, Movie, Attribute> crf = new ClusteredVote<>(
				new LevenshteinSimilarity(), 0.0);

		List<FusibleValue<String, Movie, Attribute>> cluster1 = new ArrayList<>();

		FusedValue<String, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(null, resolvedValue.getValue());
	}
}
