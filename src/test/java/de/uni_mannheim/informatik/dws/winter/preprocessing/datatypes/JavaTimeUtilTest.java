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
import java.time.LocalDateTime;

import junit.framework.TestCase;

public class JavaTimeUtilTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DateUtil#parse(java.lang.String)}.
	 * @throws ParseException 
	 */
	public void testParseString() throws ParseException {
		LocalDateTime d1 = DateJavaTime.parse("1976-01-02T00:00:00+02:00");
        assertEquals(1976, d1.getYear());
        
        LocalDateTime d2 = DateJavaTime.parse("2017-07-14T13:12:53.167");
        assertEquals(14, d2.getDayOfMonth());
        
	}

}
