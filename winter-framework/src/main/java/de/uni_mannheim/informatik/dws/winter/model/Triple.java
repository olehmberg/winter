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

/**
 * A class for wrapping three objects of arbitrary type
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <TFirst>
 * @param <TSecond>
 * @param <TThird>
 */
public class Triple<TFirst, TSecond, TThird> {

	private TFirst first;
	private TSecond second;
	private TThird third;

	/**
	 * Creates a new triple with the specified objects
	 * 
	 * @param first
	 * @param second
	 * @param third
	 */
	public Triple(TFirst first, TSecond second, TThird third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	/**
	 * 
	 * @return Returns the first object
	 */
	public TFirst getFirst() {
		return first;
	}

	/**
	 * Sets the first object
	 * 
	 * @param first
	 */
	public void setFirst(TFirst first) {
		this.first = first;
	}

	/**
	 * 
	 * @return Returns the second object
	 */
	public TSecond getSecond() {
		return second;
	}

	/**
	 * Sets the second object
	 * 
	 * @param second
	 */
	public void setSecond(TSecond second) {
		this.second = second;
	}

	/**
	 * 
	 * @return Returns the third object
	 */
	public TThird getThird() {
		return third;
	}

	/**
	 * Sets the third object
	 * 
	 * @param third
	 */
	public void setThird(TThird third) {
		this.third = third;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		return result;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Triple other = (Triple) obj;
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
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}
}
