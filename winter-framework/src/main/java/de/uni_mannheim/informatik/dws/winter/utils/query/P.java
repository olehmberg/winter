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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Helper class containing various predicates which can be used with {@link Q}
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class P {

	public static <T> boolean CollectionContainsValue(Collection<T> collection, T value) {
		return new Contains<T>(value).invoke(collection);
	}
	
	public static class Contains<T> implements Func<Boolean, Collection<T>> {

		private T value;
		
		public Contains(T value) {
			this.value = value;
		}
		
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public Boolean invoke(Collection<T> in) {
			return in.contains(value);
		}
		
	}

	public static <T> boolean CollectionContainsAllValues(Collection<T> collection, Collection<T> values) {
		return new ContainsAll<>(values).invoke(collection);
	}
	
	public static class ContainsAll<T> implements Func<Boolean, Collection<T>> {

		private Collection<T> value;
		
		public ContainsAll(Collection<T> value) {
			this.value = value;
		}
		
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public Boolean invoke(Collection<T> in) {
			return in.containsAll(value);
		}
		
	}
	
	public static <T> boolean SetEqualsValues(Collection<T> collection, Set<T> values) {
		return new SetEquals<>(values).invoke(collection);
	}
	
	public static class SetEquals<T> implements Func<Boolean, Collection<T>> {
		
		private Set<T> value;
		
		/**
		 * 
		 */
		public SetEquals(Set<T> value) {
			this.value = value;
		}
		
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public Boolean invoke(Collection<T> in) {
			return new HashSet<T>(in).equals(value);
		}
	}
	
	public static <T> boolean ValueIsContainedInCollection(Collection<T> collection, T value) {
		return new IsContainedIn<>(collection).invoke(value);
	}
	
	public static class IsContainedIn<T> implements Func<Boolean, T> {

		private Collection<T> value;
		
		public IsContainedIn(Collection<T> value) {
			this.value = value;
		}
		
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public Boolean invoke(T in) {
			return value.contains(in);
		}
		
	}
	
	public static <T> boolean CollectionIsContainedInValues(Collection<T> collection, Collection<T> values) {
		return new AreAllContainedIn<>(values).invoke(collection);
	}
	
	public static class AreAllContainedIn<T> implements Func<Boolean, Collection<T>> {

		private Collection<T> value;
		
		public AreAllContainedIn(Collection<T> value) {
			this.value = value;
		}
		
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public Boolean invoke(Collection<T> in) {
			return value.containsAll(in);
		}
		
	}
}
