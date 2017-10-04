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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * 
 * Collection of utility functions for maps
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class MapUtils {

	public static <T> Integer increment(Map<T, Integer> map, T keyValue) {
		Integer cnt = map.get(keyValue);
		
		if(cnt==null) {
			cnt = 0;
		}
		
		map.put(keyValue, cnt+1);
		
		return cnt+1;
	}
	
	public static <T> void add(Map<T, Integer> map, T keyValue, int value) {
		Integer cnt = map.get(keyValue);
		
		if(cnt==null) {
			cnt = 0;
		}
		
		map.put(keyValue, cnt+value);
	}
	
	public static <T> void add(Map<T, Double> map, T keyValue, double value) {
		Double sum = map.get(keyValue);
		
		if(sum==null) {
			sum = 0.0;
		}
		
		map.put(keyValue, sum+value);
	}
	
	public static <T, U extends Comparable<U>> T max(Map<T, U> map) {
		U iMax = null;
		T tMax = null;
		
		for(T t : map.keySet()) {
			U i = map.get(t);
			
//			if(iMax==null || i>iMax) {
			if(iMax==null || i.compareTo(iMax)>0) {
				iMax = i;
				tMax = t;
			}
		}
		
		return tMax;
	}
	
	/***
	 * @param map
	 * @param keyValue
	 * @param defaultValue
	 * @return returns the value for the specified keyValue from the map. If no entry exists, defaultValue is added to the map and returned.
	 */
	public static <T, U> U get(Map<T, U> map, T keyValue, U defaultValue) {
		U val = map.get(keyValue);
		
		if(val==null) {
			map.put(keyValue, defaultValue);
			return defaultValue;
		} else {
			return val;
		}
	}
	
	/***
	 * @param map
	 * @param keyValue
	 * @param createDefaultValue
	 * @return returns the value for the specified keyValue from the map. If no entry exists, defaultValue is added to the map and returned.
	 */
	public static <T, U> U getFast(Map<T, U> map, T keyValue, Function<T, U> createDefaultValue) {
		U val = map.get(keyValue);
		
		if(val==null) {
			val = createDefaultValue.apply(keyValue);
			map.put(keyValue, val);
		}
		
		return val;
	}
	
	public static <K, V> List<Map.Entry<K, V>> sort(Map<K, V> map, Comparator<Map.Entry<K, V>> comparator) {
		ArrayList<Map.Entry<K, V>> sorted = new ArrayList<>(map.size());
		for(Map.Entry<K, V> entry : map.entrySet()) {
			sorted.add(entry);
		}
		Collections.sort(sorted, comparator);
		return sorted;
	}
	
	/**
	 * inverts a key-value map such that all values will become keys and the keys will be the values.
	 * in case of duplicates, values are overwritten
	 * @param map
	 * @return The inverted map
	 */
	public static <K, V> Map<V, K> invert(Map<K, V> map) {
		HashMap<V, K> inverted = new HashMap<>();
		
		for(K key : map.keySet()) {
			inverted.put(map.get(key), key);
		}
		
		return inverted;
	}
}
