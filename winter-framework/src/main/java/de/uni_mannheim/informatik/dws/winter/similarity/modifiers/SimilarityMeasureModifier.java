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
package de.uni_mannheim.informatik.dws.winter.similarity.modifiers;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;

/**
 * A decorator that wraps a {@link SimilarityMeasure}
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class SimilarityMeasureModifier<DataType> extends SimilarityMeasure<DataType> {

	private static final long serialVersionUID = 1L;

	private SimilarityMeasure<DataType> decoratedObject;
	
	/**
	 * @param decoratedObject the decoratedObject to set
	 */
	public void setDecoratedObject(SimilarityMeasure<DataType> decoratedObject) {
		this.decoratedObject = decoratedObject;
	}
	/**
	 * @return the decoratedObject
	 */
	public SimilarityMeasure<DataType> getDecoratedObject() {
		return decoratedObject;
	}
	
	public SimilarityMeasureModifier() {
	}
	
	public SimilarityMeasureModifier(SimilarityMeasure<DataType> decoratedObject) {
		setDecoratedObject(decoratedObject);
	}
	
	@Override
	public double calculate(DataType first, DataType second) {
		return decoratedObject.calculate(first, second);
	}

}
