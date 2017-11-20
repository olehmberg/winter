package de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes;

import java.text.ParseException;

import junit.framework.TestCase;

public class DateJavaTimeTest extends TestCase {

	public void testParseString() throws ParseException {
		
		DateJavaTime.parse("09-02-1901", "dd-MM-yyyy");
		
		DateJavaTime.parse("9-feb-1901", "d-MMM-yyyy");
		
	}

	public void testDetermineDateFormat() {
		
		assertEquals("dd-MM-yyyy", DateJavaTime.determineDateFormat("09-02-1901"));
		
		assertEquals("d-MMM-yyyy", DateJavaTime.determineDateFormat("9-feb-1901"));
		
	}

}
