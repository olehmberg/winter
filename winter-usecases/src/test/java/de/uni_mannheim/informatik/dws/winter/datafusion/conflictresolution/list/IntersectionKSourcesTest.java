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

package de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.list.IntersectionKSources;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import junit.framework.TestCase;

public class IntersectionKSourcesTest extends TestCase {

	public void testResolveConflictCollectionOfFusableValueOfListOfValueTypeRecordType() {
		IntersectionKSources<String, Movie, Attribute> crf = new IntersectionKSources<>(
				2);
		List<FusibleValue<List<String>, Movie, Attribute>> cluster1 = new ArrayList<>();
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(
				new ArrayList<String>(), null, null));
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(
				new ArrayList<String>(), null, null));
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(
				new ArrayList<String>(), null, null));
		FusedValue<List<String>, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(0, resolvedValue.getValue().size());
	}

	public void testResolveConflictCollectionOfFusableValueOfListOfValueTypeRecordType2() {
		IntersectionKSources<String, Movie, Attribute> crf = new IntersectionKSources<>(
				2);
		List<FusibleValue<List<String>, Movie, Attribute>> cluster1 = new ArrayList<>();
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h0", "h1"), null, null));
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h1", "h2"), null, null));
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h2", "h3"), null, null));
		FusedValue<List<String>, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(2, resolvedValue.getValue().size());
	}
	
	public void testResolveConflictCollectionOfFusableValueOfListOfValueTypeRecordType3() {
		IntersectionKSources<String, Movie, Attribute> crf = new IntersectionKSources<>(
				1);
		List<FusibleValue<List<String>, Movie, Attribute>> cluster1 = new ArrayList<>();
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h0", "h1"), null, null));
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h1", "h2"), null, null));
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h2", "h3"), null, null));
		FusedValue<List<String>, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(4, resolvedValue.getValue().size());
	}
	
	public void testResolveConflictCollectionOfFusableValueOfListOfValueTypeRecordType4() {
		IntersectionKSources<String, Movie, Attribute> crf = new IntersectionKSources<>(
				3);
		List<FusibleValue<List<String>, Movie, Attribute>> cluster1 = new ArrayList<>();
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h0", "h1"), null, null));
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h1", "h2"), null, null));
		cluster1.add(new FusibleValue<List<String>, Movie, Attribute>(Arrays
				.asList("h2", "h1"), null, null));
		FusedValue<List<String>, Movie, Attribute> resolvedValue = crf
				.resolveConflict(cluster1);
		assertEquals(1, resolvedValue.getValue().size());
	}
}
