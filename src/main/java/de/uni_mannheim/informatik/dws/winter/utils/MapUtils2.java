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

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Utility Class for Maps of Maps (Maps with 2 indices)
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class MapUtils2 {

	public static <K,V> void put(Map<K, Map<K, V>> map, K index1, K index2, V value) {
		
		Map<K,V> innerMap = MapUtils.get(map, index1, new HashMap<K,V>());
		
		innerMap.put(index2, value);
		
	}
	
	public static <K,V> V get(Map<K, Map<K,V>> map, K index1, K index2) {
		
		Map<K,V> innerMap = map.get(index1);
		
		if(innerMap!=null) {
			return innerMap.get(index2);
		} else {
			return null;
		}
		
	}
	
	public static <K,V> V get(Map<K, Map<K,V>> map, K index1, K index2, V defaultValue) {
		
		Map<K,V> innerMap = map.get(index1);
		
		if(innerMap==null) {
			innerMap = new HashMap<>();
			map.put(index1, innerMap);
		}
			
		return MapUtils.get(innerMap, index2, defaultValue);

	}
	
	public static <K, V> void remove(Map<K, Map<K, V>> map, K index1, K index2) {
		if(map.containsKey(index1)) {
			map.get(index1).remove(index2);
			if(map.get(index1).size()==0) {
				map.remove(index1);
			}
		}
	}
}
