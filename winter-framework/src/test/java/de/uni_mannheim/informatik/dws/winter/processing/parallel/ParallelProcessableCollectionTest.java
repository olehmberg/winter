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
package de.uni_mannheim.informatik.dws.winter.processing.parallel;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ParallelProcessableCollectionTest extends TestCase {

	public void testJoin() {
		
		Processable<String> data1 = new ParallelProcessableCollection<>();
		data1.add("a");
		data1.add("b");
		data1.add("c");
		Processable<String> data2 = new ParallelProcessableCollection<>();
		data2.add("a");
		data2.add("b");
		data2.add("c");
		
		Processable<Pair<String, String>> result = data1.join(data2, (s)->s);
		
		assertTrue(result instanceof ParallelProcessableCollection);
		
	}
	
}
