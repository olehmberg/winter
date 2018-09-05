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
package de.uni_mannheim.informatik.dws.winter.webtables.detectors;


import org.junit.Test;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ValueDetectionType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategoryParser;
import junit.framework.TestCase;

/**
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */
public class PatternbaseTypeDetectorTest extends TestCase {
	
    @Test
    public void testTypeValue() {
    	
    	PatternbasedTypeDetector patternbasedTypeDetector = new PatternbasedTypeDetector();
    	
    	ValueDetectionType columnType = patternbasedTypeDetector.detectTypeForSingleValue("1.5");
    	
    	assertEquals(DataType.numeric, columnType.getType());
    	assertEquals(null, columnType.getUnit());
    	assertEquals(UnitCategoryParser.getQuantity("normalized number"), columnType.getQuantity());
    	
    	ValueDetectionType columnType2 = patternbasedTypeDetector.detectTypeForSingleValue("1.5 thousand");
    	
    	assertEquals(DataType.numeric, columnType2.getType());
    	assertEquals(null, columnType2.getUnit());
    	assertEquals(UnitCategoryParser.getQuantity("thousand"), columnType2.getQuantity());
    	
    	ValueDetectionType columnType3 = patternbasedTypeDetector.detectTypeForSingleValue("1.5 thousand km");
    	
    	assertEquals(DataType.numeric, columnType3.getType());
    	assertEquals("kilometre", columnType3.getUnit().getName());
    	assertEquals(UnitCategoryParser.getQuantity("thousand"), columnType3.getQuantity());
    	
    	ValueDetectionType columnType4 = patternbasedTypeDetector.detectTypeForSingleValue("thousand km");
    	
    	assertEquals(DataType.string, columnType4.getType());
    	assertEquals(null, columnType4.getUnit());
    	assertEquals(null, columnType4.getQuantity());
    }
	
}
