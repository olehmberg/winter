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

package de.uni_mannheim.informatik.dws.winter.webtables.parsers;
import java.util.List;

import de.uni_mannheim.informatik.dws.winter.webtables.WebTablesStringNormalizer;


public class DynamicStringNormalizer implements StringNormalizer{
	
	/**
	 *
	 * @param columnName
	 * @return the normalised string
	 */
	public String normaliseHeader(String columnName) {
		return WebTablesStringNormalizer.normaliseHeader(columnName);
	}

	public String normaliseValue(String value, boolean removeContentInBrackets) {
		return WebTablesStringNormalizer.normaliseValue(value, removeContentInBrackets);
	}

	/**
	 * splits the string into tokens and concatenates it again, inserting white
	 * spaces between all tokens
	 *
	 * @param s
	 * @return the normalised string
	*/ 
	public String normalise(String s, boolean useStemmer) {

		return WebTablesStringNormalizer.normalise(s, useStemmer);

	}
	

	public List<String> tokenise(String s, boolean useStemmer) {
		return WebTablesStringNormalizer.tokenise(s, useStemmer);
	}

}
