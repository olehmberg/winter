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

package de.uni_mannheim.informatik.dws.winter.webtables;

import java.util.List;
import java.util.regex.Pattern;

public class ListHandler {

	private static Pattern listPattern = Pattern.compile("^\\{.+\\}$");

	public static boolean checkIfList(String columnValue) {
		if (columnValue != null && listPattern.matcher(columnValue).matches()) {
			return true;
		}
		return false;
	}

	public static String[] splitList(String columnValue) {
		String data = columnValue.substring(1, columnValue.length() - 1);
		return data.split("\\|");
	}

	public static String formatList(List<String> values) {
		StringBuilder sb = new StringBuilder();

		sb.append("{");

		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) != null) {
				if (i != 0) {
					sb.append("|");
				}

				sb.append(values.get(i).replace("|", ""));
			}
		}

		sb.append("}");

		return sb.toString();
	}

	public static boolean isArray(final Object obj) {
		if (obj != null)
			return obj.getClass().isArray();
		return false;
	}
}
