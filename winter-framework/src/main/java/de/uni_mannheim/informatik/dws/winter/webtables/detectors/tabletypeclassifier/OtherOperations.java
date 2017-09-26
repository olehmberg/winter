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

package de.uni_mannheim.informatik.dws.winter.webtables.detectors.tabletypeclassifier;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides Auxilary Methods to prepare the needed features.
 * 
 * @author Sanikumar Zope
 * @author Alexander Brinkmann
 *
 */

public class OtherOperations {
	@Deprecated
	public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {

		List<Map.Entry<K, V>> list = new LinkedList<Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
				new Comparator<Map.Entry<K, V>>() {
					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
						int res = e1.getValue().compareTo(e2.getValue());
						return res != 0 ? res : 1;
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	public String getColumnContentWithoutSpaces(String[] column) {
		String content = "";
		for (String cell : column) {
			if (cell == null)
				continue;
			else {
				if ((!cell.trim().isEmpty()) && (!cell.trim().equals("-")
						&& !cell.trim().equals("--")
						&& !cell.trim().equals("---")
						&& !cell.trim().equals("n/a")
						&& !cell.trim().equals("N/A")
						&& !cell.trim().equals("(n/a)")
						&& !cell.trim().equals("Unknown")
						&& !cell.trim().equals("unknown")
						&& !cell.trim().equals("?") && !cell.trim().equals("??")
						&& !cell.trim().equals(".")
						&& !cell.trim().equals("null")
						&& !cell.trim().equals("NULL")
						&& !cell.trim().equals("Null"))) {
					content += cell;
				} else {
					continue;
				}
			}
		}
		content = content.replaceAll("\\s", "");
		return content;
	}

}
