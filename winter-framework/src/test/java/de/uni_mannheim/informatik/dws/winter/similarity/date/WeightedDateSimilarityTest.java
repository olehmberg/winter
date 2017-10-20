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
package de.uni_mannheim.informatik.dws.winter.similarity.date;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class WeightedDateSimilarityTest extends TestCase {

	public void testCalculateDateTimeDateTime() {
		WeightedDateSimilarity sim = new WeightedDateSimilarity(0, 0, 1);
		sim.setYearRange(10);
		
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		        .appendPattern("yyyy-MM-dd")
		        .parseDefaulting(ChronoField.CLOCK_HOUR_OF_DAY, 0)
		        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		        .toFormatter(Locale.ENGLISH);
		
		LocalDateTime dt1 = LocalDateTime.parse("2015-01-01", formatter);
		LocalDateTime dt2 = LocalDateTime.parse("2014-01-01", formatter);
		LocalDateTime dt3 = LocalDateTime.parse("2010-01-01", formatter);
		LocalDateTime dt4 = LocalDateTime.parse("2005-01-01", formatter);
		LocalDateTime dt5 = LocalDateTime.parse("1905-01-01", formatter);
		
		assertEquals(1.0, sim.calculate(dt1, dt1));
		assertEquals(0.9, sim.calculate(dt1, dt2));
		assertEquals(0.9, sim.calculate(dt2, dt1));
		assertEquals(0.5, sim.calculate(dt1, dt3));
		assertEquals(0.0, sim.calculate(dt1, dt4));
		assertEquals(0.0, sim.calculate(dt1, dt5));
	}

}
