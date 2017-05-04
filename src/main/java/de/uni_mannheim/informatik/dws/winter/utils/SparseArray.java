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

/**
 * 
 * Utility class for compressing sparse arrays.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <T>
 */
public class SparseArray<T> {

	private T[] values;
	private int[] indices;
	
	public T[] getValues() {
		return values;
	}
	
	public int[] getIndices() {
		return indices;
	}
	
	@SuppressWarnings("unchecked")
	public SparseArray(T[] sparseValues) {
		ArrayList<T> valueList = new ArrayList<>();
		ArrayList<Integer> indexList = new ArrayList<>();
		
		for (int i = 0; i < sparseValues.length; i++) {
			if(sparseValues[i]!=null) {
				valueList.add(sparseValues[i]);
				indexList.add(i);
			}
		}
		
//		values = valueList.toArray(new T[valueList.size()]);
//		indices = indexList.toArray(new int[indexList.size()]);
		
		values = (T[])valueList.toArray();
		indices = new int[indexList.size()];
		for (int i = 0; i < indexList.size(); i++) {
			indices[i] = indexList.get(i);
		}
	}
	
	public T get(int index) {
		return get(index, values, indices);
	}
	
	public static <T> T get(int index, T[] values, int[] indices) {
		int translatedIndex = translateIndex(index, indices);
		
		if(translatedIndex==-1) {
			return null;
		} else {
			return values[translatedIndex];
		}
	}
	
	public static int translateIndex(int index, int[] indices) {
		for (int i = 0; i < indices.length; i++) {
			if(indices[i]==index) {
				return i;
			}
		}
		
		return -1;
	}
}
