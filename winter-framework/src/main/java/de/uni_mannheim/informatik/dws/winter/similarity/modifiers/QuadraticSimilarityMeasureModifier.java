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
 * 
 * Returns the square of the result of the inner similarity measure.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class QuadraticSimilarityMeasureModifier<DataType> extends SimilarityMeasureModifier<DataType> {

	private static final long serialVersionUID = 1L;

	public QuadraticSimilarityMeasureModifier() {
	}
	
	public QuadraticSimilarityMeasureModifier(SimilarityMeasure<DataType> decoratedObject) {
		super(decoratedObject);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.similarity.modifiers.SimilarityMeasureModifier#calculate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double calculate(DataType first, DataType second) {
		return Math.pow(super.calculate(first, second), 2);
	}
}
