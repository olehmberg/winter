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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.text.ParseException;
import java.time.LocalDateTime;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ColumnType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DateJavaTime;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.GeoCoordinateParser;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.NumericParser;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.URLParser;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.Unit;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitParser;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;

/**
 * @author petar
 *
 */
public class TypeGuesser implements TypeDetector {

	private static Pattern listCharactersPattern = Pattern.compile("\\{|\\}");

	/**
	 * use for rough type guesssing
	 *
	 * @param columnValue
	 *            is the value of the column
	 * @param headerUnit
	 * @return the data type
	 */
	public ColumnType guessTypeForValue(String columnValue, Unit headerUnit) {
		if (checkIfList(columnValue)) {
			List<String> columnValues;
			// columnValue = columnValue.replace("{", "");
			// columnValue = columnValue.replace("}", "");
			columnValue = listCharactersPattern.matcher(columnValue).replaceAll("");
			columnValues = Arrays.asList(columnValue.split("\\|"));
			Map<DataType, Integer> countTypes = new HashMap<>();
			Map<Unit, Integer> countUnits = new HashMap<>();
			for (String singleValue : columnValues) {
				ColumnType guessedSingleType = guessTypeForSingleValue(singleValue, headerUnit);

				Integer cnt = countTypes.get(guessedSingleType.getType());
				if (cnt == null) {
					cnt = 0;
				}
				countTypes.put(guessedSingleType.getType(), cnt + 1);
				// if(countTypes.containsKey(guessedSingleType.getType())) {
				// countTypes.put(guessedSingleType.getType(),
				// countTypes.get(guessedSingleType.getType())+1);
				// }
				// else {
				// countTypes.put(guessedSingleType.getType(), 1);
				// }

				cnt = countUnits.get(guessedSingleType.getUnit());
				if (cnt == null) {
					cnt = 0;
				}
				countUnits.put(guessedSingleType.getUnit(), cnt + 1);
				// if(countUnits.containsKey(guessedSingleType.getUnit())) {
				// countUnits.put(guessedSingleType.getUnit(),
				// countUnits.get(guessedSingleType.getUnit())+1);
				// }
				// else {
				// countUnits.put(guessedSingleType.getUnit(), 1);
				// }
			}
			int max = 0;
			DataType finalType = null;
			for (DataType type : countTypes.keySet()) {
				if (countTypes.get(type) > max) {
					max = countTypes.get(type);
					finalType = type;
				}
			}
			max = 0;
			Unit finalUnit = null;
			for (Unit type : countUnits.keySet()) {
				if (countUnits.get(type) > max) {
					max = countUnits.get(type);
					finalUnit = type;
				}
			}
			return new ColumnType(finalType, finalUnit);
		} else {
			return guessTypeForSingleValue(columnValue, headerUnit);
		}
	}

	private static Pattern listPattern = Pattern.compile("^\\{.+\\|.+\\}$");

	private boolean checkIfList(String columnValue) {
		// if (columnValue.matches("^\\{.+\\|.+\\}$")) {
		if (columnValue != null && listPattern.matcher(columnValue).matches()) {
			return true;
		}
		return false;
	}

	private ColumnType guessTypeForSingleValue(String columnValue, Unit headerUnit) {
		if (columnValue != null) {
			// check the length
			boolean validLenght = true;
			if (columnValue.length() > 50) {
				validLenght = false;
			}
			if (validLenght && Boolean.parseBoolean(columnValue)) {
				return new ColumnType(DataType.bool, null);
			}
			if (URLParser.parseURL(columnValue)) {
				return new ColumnType(DataType.link, null);
			}
			if (validLenght && GeoCoordinateParser.parseGeoCoordinate(columnValue)) {
				return new ColumnType(DataType.coordinate, null);
			}
			if (validLenght) {
				try {
					LocalDateTime dateTime = DateJavaTime.parse(columnValue);
					if (dateTime != null) {
						return new ColumnType(DataType.date, null);
					}
				} catch (ParseException e1) {
				}

			}
			if (validLenght && NumericParser.parseNumeric(columnValue)) {
				Unit unit = headerUnit;
				if (headerUnit == null) {
					unit = UnitParser.checkUnit(columnValue);
				}
				return new ColumnType(DataType.unit, unit);
			}
		}
		return new ColumnType(DataType.string, null);
	}

	@Override
	public ColumnType detectTypeForColumn(Object[] attributeValues, String attributeLabel) {

		HashMap<Object, Integer> typeCount = new HashMap<>();
		HashMap<Object, Integer> unitCount = new HashMap<>();
		Unit unit = UnitParser.parseUnitFromHeader(attributeLabel);
		// detect types and units per value
		int rowCounter = 0; // Skip first line --> header
		for (Object attribute : attributeValues) {
			if (rowCounter != 0) {
				String value = (String) attribute;
				ColumnType cdt = null;

				if (value != null) {
					cdt = guessTypeForValue(value, unit);
				}

				if (cdt != null) {
					MapUtils.increment(typeCount, cdt.getType());
					MapUtils.increment(unitCount, cdt.getUnit());
				}
			}
			rowCounter++;
		}

		// majority vote for type
		Object type = MapUtils.max(typeCount);
		if (type == null) {
			type = DataType.string;
		}

		// majority vote for Unit - if header unit empty
		if (unit == null) {
			unit = (Unit) MapUtils.max(unitCount);
		}

		ColumnType resColumnType = new ColumnType((DataType) type, unit);
		return resColumnType;
	}
}
