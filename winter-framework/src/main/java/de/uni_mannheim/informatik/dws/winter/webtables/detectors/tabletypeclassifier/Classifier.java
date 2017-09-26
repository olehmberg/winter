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

import java.util.List;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;

/**
 * Holds the decision tree logic to classify a column based on its corresponding
 * features. Therefore the datatypes from normalisation are used.
 * 
 * @author Sanikumar Zope
 * @author Alexander Brinkmann
 *
 */

public class Classifier {

	/**
	 * Classifies a column based on its features.
	 * 
	 * @param columnFeatures
	 *            Holds features, which were calculated for the corresponding
	 *            column.
	 * @return The predicted DataType.
	 */

	public DataType classify(List<String> columnFeatures) {
		// columnFeatures.get(0) -> PAC
		// columnFeatures.get(1) -> PPC
		// columnFeatures.get(2) -> CCP
		// columnFeatures.get(3) -> CPCHC
		// columnFeatures.get(4) -> POSHeaderCell
		// columnFeatures.get(5) -> ACL
		// columnFeatures.get(6) -> isDate/Time
		// columnFeatures.get(7) -> isBooleanVAlue

		DataType columnDatatype = null;

		if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) > 4.500
				&& Boolean.parseBoolean(columnFeatures.get(7)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > 0.685)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) > 4.500
				&& Boolean.parseBoolean(columnFeatures.get(7)) == false
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.685)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) > 4.500
				&& Boolean.parseBoolean(columnFeatures.get(7)) == true)
			columnDatatype = DataType.bool;
		else if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) <= 4.500
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > 0.700
				&& Double.parseDouble(columnFeatures.get(0)) > 0.805
				&& Boolean.parseBoolean(columnFeatures.get(7)) == false
				&& Double.parseDouble(columnFeatures.get(5)) > 0.500)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) <= 4.500
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > 0.700
				&& Double.parseDouble(columnFeatures.get(0)) > 0.805
				&& Boolean.parseBoolean(columnFeatures.get(7)) == false
				&& Double.parseDouble(columnFeatures.get(5)) <= 0.500)
			columnDatatype = DataType.bool;
		else if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) <= 4.500
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > 0.700
				&& Double.parseDouble(columnFeatures.get(0)) > 0.805
				&& Boolean.parseBoolean(columnFeatures.get(7)) == true)
			columnDatatype = DataType.bool;
		else if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) <= 4.500
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > 0.700
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.805)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) <= 4.500
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.700)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("a")
				&& Double.parseDouble(columnFeatures.get(5)) <= 4.500
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("a_a"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("ad")
				&& Double.parseDouble(columnFeatures.get(1)) > 0.020)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("ad")
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.020)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("ada"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("adsap"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apa")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(5)) > 6)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apa")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(5)) <= 6
				&& Double.parseDouble(columnFeatures.get(0)) > 0.900
				&& Double.parseDouble(columnFeatures.get(0)) > 0.995)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apa")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(5)) <= 6
				&& Double.parseDouble(columnFeatures.get(0)) > 0.900
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.995)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("apa")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(5)) <= 6
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.900)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apa")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apa")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apaapdad"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apap"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapad"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapap"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapapad"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapapap"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapapapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapapapadpdadpa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapapapapad"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapapapapapapapapapapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2)
				.equals("apapapapapapdpdpdpdpdpapapapapapapdpsa"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("apapapapapapdpsa"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("apapapapapapdspapapap"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapapapdpsa"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("apapd"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apapspd"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apd")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.430)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("apd")
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.430)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("apdapdasapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apdapdpdpdpa"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("apdp"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apdpd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apdpd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("apdpd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("apdpdpd"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("apsapapapapapapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apsapapapapapapapap"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("apspa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("as"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("asa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("asap"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("asapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("asapapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("asapapasa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("asapdpd"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("asds"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(5)) > 0.500)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(5)) <= 0.500
				&& Double.parseDouble(columnFeatures.get(0)) > 0.310)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(5)) <= 0.500
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.310)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) > 0.450
				&& Double.parseDouble(columnFeatures.get(0)) > 0.825)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) > 0.450
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.825)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.450
				&& Boolean.parseBoolean(columnFeatures.get(7)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) > 0.085
				&& Double.parseDouble(columnFeatures.get(0)) > 0.070)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.450
				&& Boolean.parseBoolean(columnFeatures.get(7)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) > 0.085
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.070)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.450
				&& Boolean.parseBoolean(columnFeatures.get(7)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.085)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.450
				&& Boolean.parseBoolean(columnFeatures.get(7)) == false
				&& Double.parseDouble(columnFeatures.get(0)) <= -0.500)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.450
				&& Boolean.parseBoolean(columnFeatures.get(7)) == true)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) <= -0.500)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) > 0.185
				&& Double.parseDouble(columnFeatures.get(0)) > 0.335
				&& Double.parseDouble(columnFeatures.get(0)) > 0.515)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) > 0.185
				&& Double.parseDouble(columnFeatures.get(0)) > 0.335
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.515)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) > 0.185
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.335)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.185
				&& Double.parseDouble(columnFeatures.get(5)) > 3.500)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("d")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.185
				&& Double.parseDouble(columnFeatures.get(5)) <= 3.500)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("da"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dp"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpa")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.515
				&& Double.parseDouble(columnFeatures.get(0)) > 0.610
				&& Double.parseDouble(columnFeatures.get(0)) > 0.740)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpa")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.515
				&& Double.parseDouble(columnFeatures.get(0)) > 0.610
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.740)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpa")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.515
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.610)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpa")
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.515)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpap"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpapa")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.745)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpapa")
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.745)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpapapapapd"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpapapd"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpapd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpapd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Double.parseDouble(columnFeatures.get(0)) > 0.420)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpapd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.420)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpapdpa")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.490)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpapdpa")
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.490)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpapdpd"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpasa"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) > 0.155)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.155
				&& Double.parseDouble(columnFeatures.get(0)) > 0.195
				&& Double.parseDouble(columnFeatures.get(1)) > 0.105)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.155
				&& Double.parseDouble(columnFeatures.get(0)) > 0.195
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.105)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.155
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.195
				&& Double.parseDouble(columnFeatures.get(1)) > 0.140)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.155
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.195
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.140
				&& Double.parseDouble(columnFeatures.get(5)) > 5.500)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > -0.500
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.155
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.195
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.140
				&& Double.parseDouble(columnFeatures.get(5)) <= 5.500)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(0)) <= -0.500
				&& Double.parseDouble(columnFeatures.get(5)) > 4.500)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(0)) <= -0.500
				&& Double.parseDouble(columnFeatures.get(5)) <= 4.500)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Double.parseDouble(columnFeatures.get(1)) > 0.125)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.125
				&& Double.parseDouble(columnFeatures.get(5)) > 9)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.125
				&& Double.parseDouble(columnFeatures.get(5)) <= 9)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(1)) > 0.325)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.325
				&& Double.parseDouble(columnFeatures.get(0)) > 0.300)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.325
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.300
				&& Double.parseDouble(columnFeatures.get(5)) > 2.500)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.325
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.300
				&& Double.parseDouble(columnFeatures.get(5)) <= 2.500)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpd")
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpda")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.320)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpda")
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.320)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpdpa")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.350)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpdpa")
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.350)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpdpapad"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpdpapap"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpdpapd"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpdpasapsap"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpdpd")
				&& Double.parseDouble(columnFeatures.get(1)) > 0.235)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpdpd")
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.235
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpdpd")
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.235
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpdpdapdpd"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpdpdpa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpdpdpd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpdpdpd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpdpdpdpd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dpdpdpdpd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dpdpsapsapsapsap"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpds"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dpdsap"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpdsapa"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dpsa"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("ds"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dsapd"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dsapdpa"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("dsd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("dsd")
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("dsdpapdp"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dsdpasdpsdpdsdpa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("dsdsd"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("pd"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("pdasap"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("pdpa"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("pdpd")
				&& Double.parseDouble(columnFeatures.get(1)) > 0.345)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("pdpd")
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.345)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("pdpdpd"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("pdpds"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sap")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.660
				&& Double.parseDouble(columnFeatures.get(5)) > 5.500)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("sap")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.660
				&& Double.parseDouble(columnFeatures.get(5)) <= 5.500
				&& Double.parseDouble(columnFeatures.get(0)) > 0.705)
			columnDatatype = DataType.bool;
		else if (columnFeatures.get(2).equals("sap")
				&& Double.parseDouble(columnFeatures.get(0)) > 0.660
				&& Double.parseDouble(columnFeatures.get(5)) <= 5.500
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.705)
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("sap")
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.660)
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("sapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("sapapasdpapapapapdsdpdpds"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("sapd"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sapdpapasap"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sapdpd"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sapdpds"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sapdpdspdpap"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sapdsap"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sapsapa"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("sapsapdpd"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sapsas"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("sapsd"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sasas"))
			columnDatatype = DataType.string;
		else if (columnFeatures.get(2).equals("sd")
				&& Double.parseDouble(columnFeatures.get(1)) > 0.055)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sd")
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.055
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) > 0.025)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sd")
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.055
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == false
				&& Double.parseDouble(columnFeatures.get(0)) <= 0.025)
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sd")
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.055
				&& Boolean.parseBoolean(columnFeatures.get(6)) == false
				&& Boolean.parseBoolean(columnFeatures.get(3)) == true)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sd")
				&& Double.parseDouble(columnFeatures.get(1)) <= 0.055
				&& Boolean.parseBoolean(columnFeatures.get(6)) == true)
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sda"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sdp"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("sdpa"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sdpas"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sdpd"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sdpda"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sdpdpa"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sdpdpd"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("sdpdpdpa"))
			columnDatatype = DataType.unit;
		else if (columnFeatures.get(2).equals("sdpdpdsapdpds"))
			columnDatatype = DataType.date;
		else if (columnFeatures.get(2).equals("sdpsdsds"))
			columnDatatype = DataType.numeric;
		else if (columnFeatures.get(2).equals("spdpda"))
			columnDatatype = DataType.unit;
		else
			columnDatatype = DataType.string; // default (if column
												// doesn`t fit into
												// above rules)

		return columnDatatype;
	}

	// public static void main(String[] args) {
	//
	// }
}
