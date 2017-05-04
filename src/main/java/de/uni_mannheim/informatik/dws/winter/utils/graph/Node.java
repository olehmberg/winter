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
package de.uni_mannheim.informatik.dws.winter.utils.graph;

import java.util.Comparator;

import de.uni_mannheim.informatik.dws.winter.utils.query.Func;


/**
 * Model of a node in a graph.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Node<T> {

	public static class NodeIdComparator<T> implements Comparator<Node<T>> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Node<T> o1, Node<T> o2) {
			return Integer.compare(o1.id, o2.id);
		}
		
	}
	
	protected static class NodeIdProjection<T> implements Func<Integer, Node<T>> {

		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public Integer invoke(Node<T> in) {
			return in.getId();
		}
		
	}
	
	protected static class NodeDataProjection<T> implements Func<T, Node<T>> {

		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.dws.t2k.utils.query.Func#invoke(java.lang.Object)
		 */
		@Override
		public T invoke(Node<T> in) {
			return in.getData();
		}
		
	}
	
	private T data;
	private int id;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the data
	 */
	public T getData() {
		return data;
	}
	
	
	public Node(T data, int id) {
		this.data = data;
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Node) {
			@SuppressWarnings("rawtypes")
			Node n = (Node)obj;
			return data.equals(n.data);
		} else {
			return super.equals(obj);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return data.toString();
	}
}
