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

import java.util.regex.Pattern;

public class BooleanParser {

	public static final Pattern booleanRegex = Pattern.compile("(yes|true|1|no|false|0)", Pattern.CASE_INSENSITIVE);

	public static boolean parseBoolean(String text) {

	    if(booleanRegex.matcher(text).matches()) {
			return true;
		}
		return false;
	}
}
