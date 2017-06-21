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

package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.numeric;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.numeric.Median;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import junit.framework.TestCase;

public class MedianTest extends TestCase {

	public void testResolveConflictCollectionOfFusableValueOfDoubleRecordType() {
		Median<Movie, Attribute> crf = new Median<>();
		List<FusibleValue<Double, Movie, Attribute>> cluster1 = new ArrayList<>();
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(1.0, null, null));
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(1.0, null, null));
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(3.0, null, null));
		FusedValue<Double, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(1.0, resolvedValue.getValue());

	}

	public void testResolveConflictCollectionOfFusableValueOfDoubleRecordType2() {
		Median<Movie, Attribute> crf = new Median<>();
		List<FusibleValue<Double, Movie, Attribute>> cluster2 = new ArrayList<>();
		FusedValue<Double, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster2);
		assertEquals(null, resolvedValue.getValue());
	}

}
