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
package de.uni_mannheim.informatik.dws.winter.preprocessing.units;


import org.junit.Test;
import de.uni_mannheim.informatik.dws.winter.utils.StringUtils;
import junit.framework.TestCase;

public class UnitParserTest extends TestCase {

    @Test
    public void testCheckUnit() {

        // for(Unit u : UnitParser.getUnits()) {
        //     System.out.println(String.format("%s: {%s}",
        //         u.getName(),
        //         StringUtils.join(u.getAbbreviations(), ",")
        //     ));
        // }

        assertNotNull(UnitParser.checkUnit("100 km"));
        assertNotNull(UnitParser.checkUnit("100 million"));

    }

}