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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class WebTablesStringNormalizer {

	// determines which string/character corresponds to the null value
	public static String nullValue = "NULL";
	// remove customized stopwords
	public static List<String> stopWords = new ArrayList<>();
	
	// detect null values
	private static final List<String> possibleNullValues = new ArrayList<String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{ 
		add(""); 
		add("__"); 
		add("-"); 
		add("_"); 
		add("?"); 
		add("unknown"); 
		add("- -"); 
		add("n/a"); 
		add("•"); 
		add("- - -"); 
		add("."); 
		add("??"); 
		add("(n/a)"); 
		}};

	/**
	 *
	 * @param columnName
	 * @return the normalised string
	 */
	public static String normaliseHeader(String columnName) {
		columnName = StringEscapeUtils.unescapeJava(columnName);
		columnName = columnName.replace("\"", "");
		columnName = columnName.replace("|", " ");
		columnName = columnName.replace(",", "");
		columnName = columnName.replace("{", "");
		columnName = columnName.replace("}", "");
		columnName = columnName.replaceAll("\n", "");

		columnName = columnName.replace("&nbsp;", " ");
		columnName = columnName.replace("&nbsp", " ");
		columnName = columnName.replace("nbsp", " ");
		columnName = columnName.replaceAll("<.*>", "");

		columnName = columnName.toLowerCase();
		columnName = columnName.trim();

		columnName = columnName.replaceAll("\\.", "");
		columnName = columnName.replaceAll("\\$", "");
		// clean the values from additional strings
		if (columnName.contains("/")) {
			columnName = columnName.substring(0, columnName.indexOf("/"));
		}

		if (columnName.contains("\\")) {
			columnName = columnName.substring(0, columnName.indexOf("\\"));
		}
		if (possibleNullValues.contains(columnName)) {
			columnName = nullValue;
		}
		
		return columnName;
	}

	private static final Pattern bracketsPattern = Pattern.compile("\\(.*\\)");

	public static String normaliseValue(String value, boolean removeContentInBrackets) {
		try {
			value = value.replaceAll("\n", "");
			value = value.replace("&nbsp;", " ");
			value = value.replace("&nbsp", " ");
			value = value.replaceAll("[&\\?]#[0-9]{1,3};", "");
			value = value.replace("nbsp", " ");
			value = value.replaceAll("<.*>", "");
			value = value.toLowerCase();
			value = value.trim();
			if (possibleNullValues.contains(value)) {
				value = nullValue;
			}
			if (removeContentInBrackets) {
				value = bracketsPattern.matcher(value).replaceAll("");
			}
		} catch (Exception e) {
		}
		return value;
	}

	/**
	 * splits the string into tokens and concatenates it again, inserting white
	 * spaces between all tokens
	 *
	 * @param s
	 * @return the normalised string
	 */
	public static String normalise(String s, boolean useStemmer) {

		return StringUtils.join(tokenise(s, useStemmer), " ");

	}

	public static List<String> tokenise(String s, boolean useStemmer) {
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
		List<String> result = new ArrayList<String>();

		try {

			Map<String, String> args = new HashMap<String, String>();
			args.put("generateWordParts", "1");
			args.put("generateNumberParts", "1");
			args.put("catenateNumbers", "0");
			args.put("splitOnCaseChange", "1");
			WordDelimiterFilterFactory fact = new WordDelimiterFilterFactory(args);

			// resolve non unicode chars
			s = StringEscapeUtils.unescapeJava(s);

			// remove brackets (but keep content)
			s = s.replaceAll("[\\(\\)]", "");

			// tokenise
			TokenStream stream = fact.create(new WhitespaceTokenizer(Version.LUCENE_46, new StringReader(s)));
			stream.reset();

			if (useStemmer) {
				// use stemmer if requested
				stream = new PorterStemFilter(stream);
			}

			// lower case all tokens
			stream = new LowerCaseFilter(Version.LUCENE_46, stream);

			// remove stop words
			stream = new StopFilter(Version.LUCENE_46, stream, ((StopwordAnalyzerBase) analyzer).getStopwordSet());

			// enumerate tokens
			while (stream.incrementToken()) {
				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
			stream.close();
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
		}

		analyzer.close();

		return result;
	}
}
