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
package de.uni_mannheim.informatik.dws.winter.webtables.detectors.tabletypeclassifier;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Sanikumar Zope
 *
 */
public class TypeClassifierTest extends TestCase {

	public void testExecute() throws FileNotFoundException, IOException {
		String[][] table = new String[7][7];
		table[0][0] = "id";
		table[0][1] = "name";
		table[0][2] = "dob";
		table[0][3] = "male";
		table[0][4] = "female";
		table[0][5] = "weight";
		table[0][6] = "height(inch)";
		table[1][0] = "1";
		table[1][1] = "abc";
		table[1][2] = "12/12/2001";
		table[1][3] = "NO";
		table[1][4] = "true";
		table[1][5] = "20kg";
		table[1][6] = "5.5";
		table[2][0] = "1";
		table[2][1] = "abc";
		table[2][2] = "12/12/2001";
		table[2][3] = "YES";
		table[2][4] = "false";
		table[2][5] = "20kg";
		table[2][6] = "5.5";
		table[3][0] = "1";
		table[3][1] = "abc";
		table[3][2] = "12/12/2001";
		table[3][3] = "YES";
		table[3][4] = "false";
		table[3][5] = "20kg";
		table[3][6] = "5.5";
		table[4][0] = "1";
		table[4][1] = "abc";
		table[4][2] = "12/12/2001";
		table[4][3] = "NO";
		table[4][4] = "true";
		table[4][5] = "20kg";
		table[4][6] = "5.5";
		table[5][0] = "1";
		table[5][1] = "abc";
		table[5][2] = "12/12/2001";
		table[5][3] = "NO";
		table[5][4] = "true";
		table[5][5] = "20kg";
		table[5][6] = "5.5";
		table[6][0] = "1";
		table[6][1] = "abc";
		table[6][2] = "12/12/2001";
		table[6][3] = "NO";
		table[6][4] = "true";
		table[6][5] = "20kg";
		table[6][6] = "5.5";

		TypeClassifier mainClass = new TypeClassifier();
		mainClass.initialize();

		mainClass.execute(table);
	}
	
}
