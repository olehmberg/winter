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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 *         Based on
 *         de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DateUtil,
 *         but uses java time to construct dates.
 * 
 *         Helps to parse a date based on all listed regex expressions.
 *
 */
public class DateJavaTime {

	// Init
	// ---------------------------------------------------------------------------------------
	private static final Map<Pattern, String> DATE_FORMAT_REGEXPS = new HashMap<Pattern, String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			put(Pattern.compile("^\\d{4}-##-##$", Pattern.CASE_INSENSITIVE), "yyyy");
			put(Pattern.compile("^\\d{4}-\\d{2}-##$", Pattern.CASE_INSENSITIVE), "yyyy-MM");

			put(Pattern.compile("^\\d{4}$", Pattern.CASE_INSENSITIVE), "yyyy");

			put(Pattern.compile("^\\d{8}$", Pattern.CASE_INSENSITIVE), "yyyyMMdd");
			put(Pattern.compile("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", Pattern.CASE_INSENSITIVE), "dd.MM.yyyy");
			put(Pattern.compile("^\\d{1,2}-\\d{1,2}-\\d{4}$", Pattern.CASE_INSENSITIVE), "dd-MM-yyyy");
			put(Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{4}$", Pattern.CASE_INSENSITIVE), "dd/MM/yyyy");
			put(Pattern.compile("^\\d{1,2}\\.\\d{1,2}\\.\\d{2}$", Pattern.CASE_INSENSITIVE), "dd.MM.yy");
			put(Pattern.compile("^\\d{1,2}-\\d{1,2}-\\d{2}$", Pattern.CASE_INSENSITIVE), "dd-MM-yy");
			put(Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{2}$", Pattern.CASE_INSENSITIVE), "dd/MM/yy");
			put(Pattern.compile("^\\d{1,2}\\.\\d{4}$", Pattern.CASE_INSENSITIVE), "MM.yyyy");
			put(Pattern.compile("^\\d{1,2}-\\d{4}$", Pattern.CASE_INSENSITIVE), "MM-yyyy");
			put(Pattern.compile("^\\d{1,2}/\\d{4}$", Pattern.CASE_INSENSITIVE), "MM/yyyy");
			put(Pattern.compile("^\\d{1,2}\\.\\d{2}$", Pattern.CASE_INSENSITIVE), "MM.yy");
			put(Pattern.compile("^\\d{1,2}-\\d{2}$", Pattern.CASE_INSENSITIVE), "MM-yy");
			put(Pattern.compile("^\\d{1,2}/\\d{2}$", Pattern.CASE_INSENSITIVE), "MM/yy");
			put(Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$", Pattern.CASE_INSENSITIVE), "yyyy-MM-dd");
			put(Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{4}$", Pattern.CASE_INSENSITIVE), "MM/dd/yyyy");
			put(Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2}$", Pattern.CASE_INSENSITIVE), "yyyy/MM/dd");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", Pattern.CASE_INSENSITIVE), "dd MMM yyyy");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", Pattern.CASE_INSENSITIVE), "dd MMMM yyyy");
			put(Pattern.compile("^[a-z]{4,}\\s\\d{1,2}\\s\\d{4}$", Pattern.CASE_INSENSITIVE), "MMMM dd yyyy");
			put(Pattern.compile("^\\d{1,2}-[a-z]{4,}-\\d{4}$", Pattern.CASE_INSENSITIVE), "dd-MMMM-yyyy");
			put(Pattern.compile("^\\d{1,2}\\.[a-z]{4,}\\.\\d{4}$", Pattern.CASE_INSENSITIVE), "dd.MMMM.yyyy");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{4,}$", Pattern.CASE_INSENSITIVE), "dd MMMM");
			put(Pattern.compile("^[a-z]{4,}\\s\\d{1,2}$", Pattern.CASE_INSENSITIVE), "MMMM dd");

			put(Pattern.compile("^\\d{1,2}\\s[a-z]{2,}$", Pattern.CASE_INSENSITIVE), "dd MMMM");
			put(Pattern.compile("^\\d{1,2}-[a-z]{2,}$", Pattern.CASE_INSENSITIVE), "dd-MMMM");

			put(Pattern.compile("^\\d{1,2}\\s[a-z]{2,}\\s\\d{4}$", Pattern.CASE_INSENSITIVE), "dd MMMM yyyy");
			put(Pattern.compile("^\\d{1,2}/[a-z]{2,}/\\d{4}$", Pattern.CASE_INSENSITIVE), "dd/MMMM/yyyy");
			put(Pattern.compile("^\\d{1,2}-[a-z]{2,}-\\d{4}$", Pattern.CASE_INSENSITIVE), "dd-MMMM-yyyy");
			put(Pattern.compile("^\\d{1,2}\\.[a-z]{2,}\\.\\d{4}$", Pattern.CASE_INSENSITIVE), "dd.MMMM.yyyy");

			put(Pattern.compile("^\\d{1,2}\\s[a-z]{2,}\\s\\d{2}$", Pattern.CASE_INSENSITIVE), "dd MMMM yy");
			put(Pattern.compile("^\\d{1,2}/[a-z]{2,}/\\d{2}$", Pattern.CASE_INSENSITIVE), "dd/MMMM/yy");
			put(Pattern.compile("^\\d{1,2}-[a-z]{2,}-\\d{2}$", Pattern.CASE_INSENSITIVE), "dd-MMMM-yy");
			put(Pattern.compile("^\\d{1,2}\\.[a-z]{2,}\\.\\d{2}$", Pattern.CASE_INSENSITIVE), "dd.MMMM.yy");

			put(Pattern.compile("^\\d{12}$", Pattern.CASE_INSENSITIVE), "yyyyMMddHHmm");
			put(Pattern.compile("^\\d{8}\\s\\d{4}$", Pattern.CASE_INSENSITIVE), "yyyyMMdd HHmm");
			put(Pattern.compile("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"dd-MM-yyyy HH:mm");
			put(Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"yyyy-MM-dd HH:mm");
			put(Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"MM/dd/yyyy HH:mm");
			put(Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"yyyy/MM/dd HH:mm");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"dd MMM yyyy HH:mm");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"dd MMMM yyyy HH:mm");
			put(Pattern.compile("^\\d{14}$", Pattern.CASE_INSENSITIVE), "yyyyMMddHHmmss");
			put(Pattern.compile("^\\d{8}\\s\\d{6}$", Pattern.CASE_INSENSITIVE), "yyyyMMdd HHmmss");
			put(Pattern.compile("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"dd-MM-yyyy HH:mm:ss");
			put(Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"yyyy-MM-dd HH:mm:ss");
			put(Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"MM/dd/yyyy HH:mm:ss");
			put(Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"yyyy/MM/dd HH:mm:ss");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"dd MMM yyyy HH:mm:ss");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", Pattern.CASE_INSENSITIVE),
					"dd MMMM yyyy HH:mm:ss");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{6}$",
					Pattern.CASE_INSENSITIVE), "dd MMMM yyyy HH:mm:ss.SSSSSS");
			put(Pattern.compile("^\\d{1,2}\\s\\d{2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{6}$",
					Pattern.CASE_INSENSITIVE), "dd MM yyyy HH:mm:ss.SSSSSS");
			put(Pattern.compile("^\\d{4}\\s\\d{2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{6}$",
					Pattern.CASE_INSENSITIVE), "yyyy MM dd HH:mm:ss.SSSSSS");
			put(Pattern.compile("^\\d{4}-\\d{2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{6}$", Pattern.CASE_INSENSITIVE),
					"yyyy-MM-dd HH:mm:ss.SSSSSS");
			put(Pattern.compile("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{2}$",
					Pattern.CASE_INSENSITIVE), "dd MMMM yyyy HH:mm:ss.SS");
			// put(Pattern.compile("^\\d{4}-\\d{2}-\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}\\+\\d{2}:\\d{2}$",
			// Pattern.CASE_INSENSITIVE),
			// "yyyy-MM-dd'T'HH:mm:ssZZZ");
			// this seems to work for gYear...
			put(Pattern.compile("^\\d{4}-\\d{2}-\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}\\+\\d{2}:\\d{2}$",
					Pattern.CASE_INSENSITIVE), "yyyy-MM-dd'T'HH:mm:ssXXX");
			put(Pattern.compile("^\\d{4}-\\d{2}-\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}Z$", Pattern.CASE_INSENSITIVE),
					"yyyy-MM-dd'T'HH:mm:ssXXX");

			// put("^\\d{2}$", "yy");
		}
	};

	/**
	 * Parse the given date string to date object and return a localDateTime
	 * instance based on the given date string. This makes use of the
	 * {@link DateJavaTime#determineDateFormat(String)} to determine the
	 * DateTimeFormat pattern to be used for parsing.
	 *
	 * @param dateString
	 *            The date string to be parsed to date object.
	 * @return The parsed localDateTime object.
	 * @throws ParseException
	 *             If the date format pattern of the given date string is
	 *             unknown, or if the given date string or its actual date is
	 *             invalid based on the date format pattern.
	 */
	public static LocalDateTime parse(String dateString) throws ParseException {
		// check not empty
		if (dateString == null) {
			return null;
		}
		// check Double
		try {
			double possibleHeight = Double.parseDouble(dateString);
			if (possibleHeight > 1.5 && possibleHeight < 2.5) {
				return null;
			}
		} catch (Exception e) {
		}
		// simple parse
		try {
			return LocalDateTime.parse(dateString);

		} catch (DateTimeParseException e) {

			// detect pattern and parse
			String dateFormat = determineDateFormat(dateString);
			if (dateFormat == null) {
				throw new ParseException("Unknown date format.", 0);
				// return null;
			}
			if (dateString.contains("-##")) {
				dateString = dateString.replace("-##", "");
			}
			LocalDateTime d = null;
			// if (dateFormat.equals("MM/dd/yyyy"))
			if (dateFormat.contains("MM") && dateFormat.contains("dd")) {
				try {
					d = parse(dateString, dateFormat);
				} catch (Exception e1) {
					String util = dateFormat.replace("MM", "XX");
					util = util.replace("dd", "MM");
					util = util.replace("XX", "dd");
					try {
						d = parse(dateString, util);
					} catch (Exception e2) {
					}
				}
				return d;
			}
			try {
				d = parse(dateString, dateFormat);
			} catch (Exception e3) {
			}

			if (d != null && (d.getYear() < 0 || d.getYear() > 2100)) {
				return null;
			}
			return d;
		}
	}

	/**
	 * Validate the actual date of the given date string based on the given date
	 * format pattern and return a date instance based on the given date string.
	 *
	 * @param dateString
	 *            The date string.
	 * @param dateFormat
	 *            The date format pattern which should respect the
	 *            DateTimeFormatter rules.
	 * @return The parsed date object.
	 * @throws ParseException
	 *             If the given date string or its actual date is invalid based
	 *             on the given date format pattern.
	 * @see SimpleDateFormat
	 */
	public static LocalDateTime parse(String dateString, String dateFormat) throws ParseException {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(dateFormat)
				.parseDefaulting(ChronoField.YEAR_OF_ERA, 1).parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
				.parseDefaulting(ChronoField.DAY_OF_MONTH, 1).parseDefaulting(ChronoField.CLOCK_HOUR_OF_DAY, 0)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
				.toFormatter(Locale.ENGLISH);

		return LocalDateTime.parse(dateString, formatter);
	}

	// Checkers
	// -----------------------------------------------------------------------------------
	/**
	 * Determine DateTimeFormatter pattern matching with the given date string.
	 * Returns null if format is unknown. You can simply extend DateJavaTime
	 * with more formats if needed.
	 *
	 * @param dateString
	 *            The date string to determine the DateTimeFormat pattern for.
	 * @return The matching SimpleDateFormat pattern, or null if format is
	 *         unknown.
	 * @see SimpleDateFormat
	 */
	public static String determineDateFormat(String dateString) {
		for (Pattern regexp : DATE_FORMAT_REGEXPS.keySet()) {
			if (regexp.matcher(dateString).matches()) {
				return DATE_FORMAT_REGEXPS.get(regexp);
			}
		}
		return null; // Unknown format.
	}

}
