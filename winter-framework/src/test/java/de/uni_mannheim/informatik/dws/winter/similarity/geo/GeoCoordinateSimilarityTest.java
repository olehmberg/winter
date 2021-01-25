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

package de.uni_mannheim.informatik.dws.winter.similarity.geo;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import junit.framework.TestCase;

public class GeoCoordinateSimilarityTest extends TestCase {


    public void testCalculateDateTimeDateTime() {

        // Initialise similarity measure
        GeoCoordinateSimilarity sim = new GeoCoordinateSimilarity(10);

        // Setup
        Pair<Double, Double> uni_mannheim_1 = new Pair<Double, Double>(49.4765830937, 8.45880983142);
        Pair<Double, Double> uni_mannheim_2 = new Pair<Double, Double>(49.4833125, 8.4626238);
        Pair<Double, Double> wasserturm_mannheim = new Pair<Double, Double>(49.483752, 8.476682);
        Pair<Double, Double> uni_heidelberg = new Pair<Double, Double>(49.406165042, 8.703830518);

        // Test
        assertEquals(1.0, sim.calculate(uni_mannheim_1, uni_mannheim_1));
        assertEquals(1.0, sim.calculate(uni_mannheim_2, uni_mannheim_2));
        assertTrue(0.9 < sim.calculate(uni_mannheim_1, uni_mannheim_2));
        assertTrue(0.8 < sim.calculate(uni_mannheim_1, wasserturm_mannheim));
        assertTrue(0.8 < sim.calculate(uni_mannheim_2, wasserturm_mannheim));
        assertEquals(0.0, sim.calculate(uni_mannheim_1, uni_heidelberg));
        assertEquals(0.0, sim.calculate(uni_mannheim_2, uni_heidelberg));
    }

}
