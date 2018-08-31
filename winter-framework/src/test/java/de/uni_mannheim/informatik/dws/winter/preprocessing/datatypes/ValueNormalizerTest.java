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

import org.junit.Test;

import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategoryParser;
import junit.framework.TestCase;

public class ValueNormalizerTest extends TestCase {
	
    @Test
    public void testTypeValue() {
    	
    	

    	ValueNormalizer valueNormalizer = new ValueNormalizer();

        assertEquals(1.0,valueNormalizer.normalize("1", DataType.numeric, null));

        String value = "1.5 million";
        assertEquals(1500000.0,valueNormalizer.normalize(value, DataType.numeric, UnitCategoryParser.checkUnitCategory(value)));
        assertEquals(1500000.0,valueNormalizer.normalize(value, DataType.numeric, null));
        
        String value2 = "1.5 km";
        assertEquals(1500.0,valueNormalizer.normalize(value2, DataType.numeric, UnitCategoryParser.checkUnitCategory(value2)));
        assertEquals(1500.0,valueNormalizer.normalize(value2, DataType.numeric, UnitCategoryParser.getUnitCategory("Distance")));
        assertNotSame(1500.0,valueNormalizer.normalize(value2, DataType.numeric, null));
        assertEquals(1.5,valueNormalizer.normalize(value2, DataType.numeric, null));
        
        String value3 = "1.5 thousand km";
        assertEquals(1500000.0,valueNormalizer.normalize(value3, DataType.numeric, UnitCategoryParser.checkUnitCategory(value3)));
        assertEquals(1500000.0,valueNormalizer.normalize(value3, DataType.numeric, UnitCategoryParser.getUnitCategory("Distance")));
        
        String value4 = "asd thousand km";
        assertEquals(null,valueNormalizer.normalize(value4, DataType.numeric, UnitCategoryParser.checkUnitCategory(value4)));
        
        String value5 = "85 mph";
        assertEquals(136.7939,valueNormalizer.normalize(value5, DataType.numeric, UnitCategoryParser.checkUnitCategory(value5)));
        assertEquals(136.7939,valueNormalizer.normalize(value5, DataType.numeric, UnitCategoryParser.getUnitCategory("Speed")));
        
        String value6 = "357386 km2";
        assertEquals(357386000000.0,valueNormalizer.normalize(value6, DataType.numeric, UnitCategoryParser.checkUnitCategory(value6)));
        assertEquals(357386000000.0,valueNormalizer.normalize(value6, DataType.numeric, UnitCategoryParser.getUnitCategory("Area")));
        
        
    }

}