package de.uni_mannheim.informatik.dws.winter.webtables.parsers;

import java.util.List;

public interface StringNormalizer {
	
	// determines which string/character corresponds to the null value
	public static final String nullValue = "NULL";
		
	String normaliseHeader(String columnName);
	
	String normaliseValue(String value, boolean removeContentInBrackets);
	
	String normalise(String s, boolean useStemmer);
	
	List<String> tokenise(String s, boolean useStemmer);
}
