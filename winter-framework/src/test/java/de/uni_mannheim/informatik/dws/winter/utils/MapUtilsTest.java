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
package de.uni_mannheim.informatik.dws.winter.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class MapUtilsTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.utils.MapUtils#increment(java.util.Map, java.lang.Object)}.
	 */
	public void testIncrement() {
		Map<String, Integer> map = new HashMap<>();
		
		map.put("a", 1);
		map.put("b", 2);
		
		MapUtils.increment(map, "a");
		
		assertEquals(new Integer(2), map.get("a"));
		assertEquals(new Integer(2), map.get("b"));
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.utils.MapUtils#add(java.util.Map, java.lang.Object, int)}.
	 */
	public void testAddMapOfTIntegerTInt() {
		Map<String, Integer> map = new HashMap<>();
		
		map.put("a", 1);
		map.put("b", 2);
		
		MapUtils.add(map, "a", 1);
		
		assertEquals(new Integer(2), map.get("a"));
		assertEquals(new Integer(2), map.get("b"));
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.utils.MapUtils#add(java.util.Map, java.lang.Object, double)}.
	 */
	public void testAddMapOfTDoubleTDouble() {
		Map<String, Double> map = new HashMap<>();
		
		map.put("a", 0.1);
		map.put("b", 0.2);
		
		MapUtils.add(map, "a", 0.1);
		
		assertEquals(new Double(0.2), map.get("a"));
		assertEquals(new Double(0.2), map.get("b"));
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.utils.MapUtils#max(java.util.Map)}.
	 */
	public void testMax() {
		Map<String, Integer> map = new HashMap<>();
		
		map.put("a", 1);
		map.put("b", 2);
		
		String max = MapUtils.max(map);
		
		assertEquals("b", max);
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.utils.MapUtils#get(java.util.Map, java.lang.Object, java.lang.Object)}.
	 */
	public void testGet() {
		Map<String, Integer> map = new HashMap<>();
		
		map.put("a", 1);
		map.put("b", 2);
		
		Integer valueA = MapUtils.get(map, "a", 0);
		Integer valueC = MapUtils.get(map, "c", 0);
		
		assertEquals(new Integer(1), valueA);
		assertEquals(new Integer(0), valueC);
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.utils.MapUtils#getFast(java.util.Map, java.lang.Object, java.util.function.Function)}.
	 */
	public void testGetFast() {
		Map<String, Integer> map = new HashMap<>();
		
		map.put("a", 1);
		map.put("b", 2);
		
		Integer valueA = MapUtils.getFast(map, "a", (key)->0);
		Integer valueC = MapUtils.getFast(map, "c", (key)->0);
		
		assertEquals(new Integer(1), valueA);
		assertEquals(new Integer(0), valueC);
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.utils.MapUtils#sort(java.util.Map, java.util.Comparator)}.
	 */
	public void testSort() {
		Map<String, Integer> map = new HashMap<>();
		
		map.put("a", 2);
		map.put("b", 1);
		
		List<Entry<String, Integer>> sorted = MapUtils.sort(map, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return Integer.compare(o1.getValue(), o2.getValue());
			}
		});
		
		assertEquals("b", sorted.get(0).getKey());
		assertEquals("a", sorted.get(1).getKey());
	}

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.utils.MapUtils#invert(java.util.Map)}.
	 */
	public void testInvert() {
		Map<String, Integer> map = new HashMap<>();
		
		map.put("a", 1);
		map.put("b", 2);
		
		Map<Integer, String> inverted = MapUtils.invert(map);
		
		assertEquals("a", inverted.get(1));
		assertEquals("b", inverted.get(2));
	}

}
