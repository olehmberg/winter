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

/**
 * @author petar
 * 
 */
public class URLParser {

	public static final Pattern URLregex = Pattern.compile("^"
			+
			// protocol identifier
			"((?:(?:https?|ftp)://)|(www\\.))"
			+
			// user:pass authentication
			"(?:\\S+(?::\\S*)?@)?"
			+ "(?:"
			+
			// IP address exclusion
			// private & local networks
			"(?!10(?:\\.\\d{1,3}){3})"
			+ "(?!127(?:\\.\\d{1,3}){3})"
			+ "(?!169\\.254(?:\\.\\d{1,3}){2})"
			+ "(?!192\\.168(?:\\.\\d{1,3}){2})"
			+ "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})"
			+
			// IP address dotted notation octets
			// excludes loopback network 0.0.0.0
			// excludes reserved space >= 224.0.0.0
			// excludes network & broacast addresses
			// (first & last IP address of each class)
			"(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])"
			+ "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}"
			+ "(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" + "|" +
			// host name
			"(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)" +
			// domain name
			"(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*" +
			// TLD identifier
			"(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))" + ")" +
			// port number
			"(?::\\d{2,5})?" +
			// resource path
			"(?:/[^\\s]*)?" + "$", Pattern.CASE_INSENSITIVE);

	public static boolean parseURL(String text) {
		if (text.contains(" "))
			return false;
		if (text.startsWith("http://") || text.startsWith("www."))
			return true;
		if (URLregex.matcher(text).matches()) {
			return true;
		}
		return false;
	}

}
