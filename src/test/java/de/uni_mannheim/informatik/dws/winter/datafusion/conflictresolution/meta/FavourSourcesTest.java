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

package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.meta;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import junit.framework.TestCase;

public class FavourSourcesTest extends TestCase {

	public void testResolveConflict() {

		FavourSources<Double, Movie, Attribute> crf = new FavourSources<>();
		List<FusibleValue<Double, Movie, Attribute>> cluster1 = new ArrayList<>();
		FusibleDataSet<Movie, Attribute> ds1 = new FusibleHashedDataSet<>();
		ds1.setScore(1.0);
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(1.0, null, ds1));
		FusibleDataSet<Movie, Attribute> ds2 = new FusibleHashedDataSet<>();
		ds2.setScore(0.5);
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(2.0, null, ds2));
		FusibleDataSet<Movie, Attribute> ds3 = new FusibleHashedDataSet<>();
		ds3.setScore(0.1);
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(3.0, null, ds3));
		FusedValue<Double, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(1.0, resolvedValue.getValue());

	}

	public void testResolveConflict1() {

		FavourSources<Double, Movie, Attribute> crf = new FavourSources<>();
		List<FusibleValue<Double, Movie, Attribute>> cluster1 = new ArrayList<>();
		FusedValue<Double, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(null, resolvedValue.getValue());

	}
	
	public void testResolveConflict2() {

		FavourSources<Double, Movie, Attribute> crf = new FavourSources<>();
		List<FusibleValue<Double, Movie, Attribute>> cluster1 = new ArrayList<>();
		FusibleDataSet<Movie, Attribute> ds1 = new FusibleHashedDataSet<>();
		ds1.setScore(1.0);
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(1.0, null, ds1));
		FusibleDataSet<Movie, Attribute> ds2 = new FusibleHashedDataSet<>();
		ds2.setScore(0.5);
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(2.0, null, ds2));
		FusibleDataSet<Movie, Attribute> ds3 = new FusibleHashedDataSet<>();
		ds3.setScore(10.1);
		cluster1.add(new FusibleValue<Double, Movie, Attribute>(3.0, null, ds3));
		FusedValue<Double, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(3.0, resolvedValue.getValue());

	}

}
