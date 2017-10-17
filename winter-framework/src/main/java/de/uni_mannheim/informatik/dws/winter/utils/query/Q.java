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

package de.uni_mannheim.informatik.dws.winter.utils.query;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Helper class for data querying functions
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Q {

    public static <T> T firstOrDefault(Collection<T> data) {
        Iterator<T> it = data.iterator();
        
        if(!it.hasNext()) {
            return null;
        } else {
            return it.next();
        }
    }
    
    public static <T> Collection<T> where(Collection<T> data, Func<Boolean, ? super T> predicate) {
        Collection<T> result = new LinkedList<>();
        
        for(T instance : data) {
            if(predicate.invoke(instance)) {
                result.add(instance);
            }
        }
        
        return result;
    }
    
    public static <T> boolean any(Collection<T> data, Func<Boolean, ? super T> predicate) {
        for(T instance : data) {
            if(predicate.invoke(instance)) {
                return true;
            }
        }
        return false;
    }
    
    public static <T> boolean all(Collection<T> data, Func<Boolean, ? super T> predicate) {
        for(T instance : data) {
            if(!predicate.invoke(instance)) {
                return false;
            }
        }
        return true;
    }
    
    public static <T, U> Map<T, Collection<U>> group(Iterable<U> data, Func<T, U> keySelector) {
        Map<T, Collection<U>> result = new HashMap<T, Collection<U>>();
        
        for(U instance : data) {
            T key = keySelector.invoke(instance);
            
            Collection<U> collection = result.get(key);
            
            if(collection==null) {
                collection = new LinkedList<U>();
                result.put(key, collection);
            }
            
            collection.add(instance);
        }
        
        return result;
    }

    public static <T extends Comparable<T>> T max(Collection<T> data) {
        T max = null;
        
        for(T item : data) {
        	if(item!=null) {
	            if(max==null || item.compareTo(max)>0) {
	                max = item;
	            }
        	}
        }
        
        return max;
    }
    
    /**
     * @param data
     * @param valueSelector
     * @return Returns the object from the collection with the highest value according to the valueSelector function
     */
    public static <T, U extends Comparable<U>> T max(Collection<T> data, Func<U, T> valueSelector) {
        T maxObj = null;
        U maxValue = null;
        
        for(T item : data) {
            U value = valueSelector.invoke(item);
            if(maxValue==null || value.compareTo(maxValue)>0) {
                maxValue = value;
                maxObj = item;
            }
        }
        
        return maxObj;
    }
    
    /**
     * @param data
     * @param valueSelector
     * @return Returns the object from the collection with the lowest value according to the valueSelector function
     */
    public static <T, U extends Comparable<U>> T min(Collection<T> data, Func<U, T> valueSelector) {
        T minObj = null;
        U minValue = null;
        
        for(T item : data) {
            U value = valueSelector.invoke(item);
            if(minValue==null || value.compareTo(minValue)<0) {
                minValue = value;
                minObj = item;
            }
        }
        
        return minObj;
    }
    
    /**
     * @param first
     * @param second
     * @return returns all elements (de-duplicated) that appear in first or second
     */
    public static <T> Set<T> union(Collection<T> first, Collection<T> second) {
        Set<T> result = new HashSet<T>(first.size()+second.size());
        
        result.addAll(first);
        result.addAll(second);
        
        return result;
    }
    
    /**
     * @param first
     * @param second
     * @return returns all elements that appear both in first and second
     */
    public static <T> Set<T> intersection(Collection<T> first, Collection<T> second) {
        Set<T> result = new HashSet<>();
        
        for(T t : first) {
            if(second.contains(t)) {
                result.add(t);
            }
        }

        return result;
    }
    
    /**
     * @param first
     * @param second
     * @return returns all elements of first without the elements of second
     */
    public static <T> Collection<T> without(Collection<T> first, Collection<T> second) {
    	if(first==null || second==null) {
    		return first;
    	}
    	
        Collection<T> result = new LinkedList<T>(first);
        
        Iterator<T> it = result.iterator();
        
        while(it.hasNext()) {
            if(second.contains(it.next())) {
                it.remove();
            }
        }
        
        return result;
    }

    /**
     * @param first
     * @param second
     * @return returns all elements of first without the elements of second
     */
    public static <T, U> Collection<T> without(Collection<T> first, Collection<U> second, Func<U, T> firstToSecond) {
        Collection<T> result = new LinkedList<T>(first);
        
        Iterator<T> it = result.iterator();
        
        while(it.hasNext()) {
            if(second.contains(firstToSecond.invoke(it.next()))) {
                it.remove();
            }
        }
        
        return result;
    }
    
    public static <T extends Number> double average(Collection<T> data) {
    	double sum = 0.0;
    	int count = 0;
    	
    	for(T value : data) {
    		sum += value.doubleValue();
    		count++;
    	}
    	
    	return sum / (double)count;
    }
    
    public static <T extends Number> double sum(Collection<T> data) {
    	double sum = 0.0;
    	
    	for(T value : data) {
    		sum += value.doubleValue();
    	}
    	
    	return sum;
    }
    
    public static <T extends Comparable<? super T>> List<T> sort(Collection<T> data) {
    	ArrayList<T> sorted = new ArrayList<>(data);
    	Collections.sort(sorted);
    	return sorted;
    }
    
    public static <T> List<T> sort(Collection<T> data, Comparator<T> comparator) {
    	ArrayList<T> sorted = new ArrayList<>(data);
    	Collections.sort(sorted, comparator);
    	return sorted;
    }
    
    public static <TOut, TIn> Collection<TOut> project(Collection<TIn> data, Func<TOut, ? super TIn> projection) {
    	if(data==null) {
    		return new ArrayList<TOut>();
    	}
    	ArrayList<TOut> result = new ArrayList<>(data.size());
    	
    	for(TIn t : data) {
    		result.add(projection.invoke(t));
    	}
    	
    	return result;
    }
    
    public static <T> Collection<T> project(Collection<T> data, Collection<Integer> indices) {
    	if(data==null) {
    		return new ArrayList<T>();
    	}
    	ArrayList<T> result = new ArrayList<>(indices.size());
    	
    	int i = 0;
    	for(T t : data) {
    		if(indices.contains(i)) {
    			result.add(t);
    		}
    	}
    	
    	return result;
    }
    
    public static <T> Collection<T> project(Collection<T> data, int[] indices) {
    	List<Integer> indexList = new ArrayList<Integer>(indices.length);
    	for(int i : indices) {
    		indexList.add(i);
    	}
    	return project(data, indexList);
    }
    
    public static <T> T[] project(T[] data, int[] indices) {
    	if(data==null) {
    		return null;
    	}
    	
    	T[] result = Arrays.copyOf(data, indices.length);
    	
    	int j=0;
    	for(int i : indices) {
    		result[j++] = data[i];
    	}
    	
    	return result;
    }
    
    @SafeVarargs
	public static <T> List<T> toList(T... values) {
    	return new ArrayList<>(Arrays.asList(values));
    }
    
    @SafeVarargs
	public static <T> Set<T> toSet(T... values) {
    	return new HashSet<T>(Arrays.asList(values));
    }
    
    @SafeVarargs
	public static <T> T[] toArray(T... values) {
    	return values;
    }
    
    @SuppressWarnings("unchecked")
	public static <T> T[] toArrayFromCollection(Collection<T> values, Class<T> cls) {
    	T[] result = values.toArray((T[])Array.newInstance(cls, values.size()));
    	return result;
    }
    
    public static int[] toPrimitiveIntArray(Collection<Integer> values) {
    	int[] array = new int[values.size()];
    	int i=0;
    	for(Integer value : values) {
    		array[i++]=value;
    	}
    	return array;
    }
    
    public static <KeyType, T> Map<KeyType, T> map(Collection<T> data, Func<KeyType, T> mapByKey) {
    	HashMap<KeyType, T> result = new HashMap<>();
    	
    	for(T t : data) {
    		result.put(mapByKey.invoke(t), t);
    	}
    	
    	return result;
    }
    
    public static <KeyType, T, ValueType> Map<KeyType, ValueType> map(Collection<T> data, Func<KeyType, T> mapByKey, Func<ValueType, T> mapToValue) {
    	HashMap<KeyType, ValueType> result = new HashMap<>();
    	
    	for(T t : data) {
    		result.put(mapByKey.invoke(t), mapToValue.invoke(t));
    	}
    	
    	return result;
    }
    
    public static <T> Collection<String> toString(Collection<T> data) {
    	ArrayList<String> result = new ArrayList<>(data.size());
    	
    	for(T t : data) {
    		if(t==null) {
    			result.add("null");
    		} else {
    			result.add(t.toString());
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Creates all proper subsets of the given set (that is, excluding the set itself).
     * @param data
     * @return The subsets
     */
    public static <T> Set<Set<T>> getAllProperSubsets(Set<T> data) {
    	Set<Set<T>> result = new HashSet<>();

    	for(T element : data) {
    		
    		Set<T> subset = new HashSet<>(data);
    		subset.remove(element);
    		result.add(subset);
    		
    		if(subset.size()>1)
    		{
    			result.addAll(getAllSubsets(subset));
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Creates all subsets of the given set (including the set itself).
     * @param data
     * @return The subsets
     */
    public static <T> Set<Set<T>> getAllSubsets(Set<T> data) {
    	Set<Set<T>> result = new HashSet<>();

    	result.add(data);
    	
    	for(T element : data) {
    		
    		Set<T> subset = new HashSet<>(data);
    		subset.remove(element);
    		
    		if(subset.size()>0)
    		{
    			result.addAll(getAllSubsets(subset));
    		}
    	}
    	
    	return result;
    }

    public static boolean equals(Object value1, Object value2, boolean nullEqualsNull) {
    	
    	// if only one of the values if null, return false
    	if(value1==null && value2!=null || value1!=null && value2==null) {
    		return false;
    	} else {
    		
    		// if both values are null, return nullEqualsNull
    		if(value1==null && value2==null) {
    			return nullEqualsNull;
    		} else {
    			
    			// otherwise, compare the values
    			return value1.equals(value2);
    		}
    	}
    }
    
    public static <T> Collection<T> take(Collection<T> data, int n) {
    	ArrayList<T> result = new ArrayList<>(n);
    	
    	Iterator<T> it = data.iterator();
    	while(it.hasNext() && result.size()<n) {
    		result.add(it.next());
    	}
    	
    	return result;
    }
}
