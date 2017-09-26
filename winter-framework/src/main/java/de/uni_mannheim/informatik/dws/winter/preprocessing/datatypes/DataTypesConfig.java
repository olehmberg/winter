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

package de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author petar
 *
 */
public class DataTypesConfig {
	public static final Map<String, Integer> months = new HashMap<String, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("january", 1);
			put("february", 2);
			put("march", 3);
			put("april", 4);
			put("may", 5);
			put("june", 6);
			put("july", 7);
			put("august", 8);
			put("september", 9);
			put("october", 10);
			put("november", 11);
			put("december", 12);

		}
	};
	public static final Map<String, Integer> era = new HashMap<String, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("BCE", 1);
			put("BC", 1);
			put("CE", 1);
			put("AD", 1);
			put("AC", 1);
			put("CE", 1);

		}
	};

	public static String cardinalityRegex = "st|nd|rd|th";
}
