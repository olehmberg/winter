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
package de.uni_mannheim.informatik.dws.winter.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A pair of two objects.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <T>
 * @param <U>
 */
public class Pair<T, U> implements Serializable {

	private static final long serialVersionUID = 1L;
	private T first;
	private U second;

	public Pair() {
		
	}
	
	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public U getSecond() {
		return second;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pair))
			return false;
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

	public static <T,U> Map<T, U> toMap(Collection<Pair<T, U>> pairs) {
		Map<T, U> result = new HashMap<>();
		
		for(Pair<T, U> p : pairs) {
			result.put(p.getFirst(), p.getSecond());
		}
		
		return result;
	}
	
	public static <T,U> Collection<Pair<T,U>> fromMap(Map<T,U> map) {
		Collection<Pair<T,U>> result = new ArrayList<>();
		
		for(T key : map.keySet()) {
			result.add(new Pair<T, U>(key, map.get(key)));
		}
		
		return result;
	}
}
