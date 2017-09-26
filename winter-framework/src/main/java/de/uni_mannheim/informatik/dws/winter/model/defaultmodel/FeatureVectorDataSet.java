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
package de.uni_mannheim.informatik.dws.winter.model.defaultmodel;

import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;

/**
 * 
 * A data set for features created by a matching rule.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class FeatureVectorDataSet extends FusibleHashedDataSet<Record, Attribute> {

	private static final long serialVersionUID = 1L;
	public static final Attribute ATTRIBUTE_FINAL_VALUE = new Attribute("finalValue");
	public static final Attribute ATTRIBUTE_IS_MATCH = new Attribute("isMatch");
	public static final Attribute ATTRIBUTE_LABEL = new Attribute("label");

	public FeatureVectorDataSet() {
		super();
		addAttribute(ATTRIBUTE_FINAL_VALUE);
		addAttribute(ATTRIBUTE_IS_MATCH);
	}
	
}
