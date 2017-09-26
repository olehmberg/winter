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
 * Utility class for enforcing string interning. Strings with the same sequence of characters obtained via this cache will be represented by the same object in memory.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class StringCache {

	private static Map<String, String> cache = new HashMap<>();
	
	public static String get(String s) {
		if(s==null) {
			return null;
		} else {
			String s2 = cache.get(s);
			if(s2==null) {
				// make sure that there is no longer string referenced by the value
				// http://www.javamex.com/tutorials/memory/string_memory_usage.shtml
				s2 = new String(s);
				cache.put(s2, s2);
				return s2;
			} else {
				return s2;
			}
		}
	}

	public static void clear() {
		cache.clear();
	}
}
