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

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ValueDetectionType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ColumnType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DateJavaTime;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.GeoCoordinateParser;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.NumericParser;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.URLParser;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.Quantity;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.Unit;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategory;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategoryParser;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * 
 * Type Detector to detect data type, unit and quantity of a data value based on different regex expressions.
 * 
 * @author Petar and Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */
public class PatternbasedTypeDetector implements TypeDetector {

	private static Pattern listCharactersPattern = Pattern.compile("\\{|\\}");
	private static final Logger logger = WinterLogManager.getLogger();
	
	/**
	 * use for rough type detection
	 *
	 * @param columnValue
	 *            is the value of the column
	 * @param headerUnit
	 * @return the data type
	 */
	public ValueDetectionType detectTypeForValue(String columnValue, Unit headerUnit) {
		if (checkIfList(columnValue)) {
			List<String> columnValues;
			// columnValue = columnValue.replace("{", "");
			// columnValue = columnValue.replace("}", "");
			columnValue = listCharactersPattern.matcher(columnValue).replaceAll("");
			columnValues = Arrays.asList(columnValue.split("\\|"));
			Map<DataType, Integer> countTypes = new HashMap<>();
			Map<Unit, Integer> countUnits = new HashMap<>();
			for (String singleValue : columnValues) {
				ColumnType detectedSingleType = detectTypeForSingleValue(singleValue, headerUnit);

				Integer cnt = countTypes.get(detectedSingleType.getType());
				if (cnt == null) {
					cnt = 0;
				}
				countTypes.put(detectedSingleType.getType(), cnt + 1);
				// if(countTypes.containsKey(detectedSingleType.getType())) {
				// countTypes.put(detectedSingleType.getType(),
				// countTypes.get(detectedSingleType.getType())+1);
				// }
				// else {
				// countTypes.put(detectedSingleType.getType(), 1);
				// }

				cnt = countUnits.get(detectedSingleType.getUnit());
				if (cnt == null) {
					cnt = 0;
				}
				countUnits.put(detectedSingleType.getUnit(), cnt + 1);
				// if(countUnits.containsKey(detectedSingleType.getUnit())) {
				// countUnits.put(detectedSingleType.getUnit(),
				// countUnits.get(detectedSingleType.getUnit())+1);
				// }
				// else {
				// countUnits.put(detectedSingleType.getUnit(), 1);
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
			UnitCategory unitCategory = null;
			for (Unit type : countUnits.keySet()) {
				if (countUnits.get(type) > max) {
					max = countUnits.get(type);
					finalUnit = type;
					unitCategory = finalUnit.getUnitCategory();
				}
			}
			return new ValueDetectionType(finalType,null, finalUnit, unitCategory);
		} else {
			return detectTypeForSingleValue(columnValue, headerUnit);
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
	
	public ValueDetectionType detectTypeForSingleValue(String columnValue) {
		return detectTypeForSingleValue(columnValue, null);
	}
	
	private ValueDetectionType detectTypeForSingleValue(String columnValue, Unit headerUnit) {
		if (columnValue != null) {
			// check the length
			boolean validLenght = true;
			if (columnValue.length() > 50) {
				validLenght = false;
			}
			if (validLenght && Boolean.parseBoolean(columnValue)) {
				return new ValueDetectionType(DataType.bool, null, null, null);
			}
			if (URLParser.parseURL(columnValue)) {
				return new ValueDetectionType(DataType.link, null, null, null);
			}
			if (validLenght && GeoCoordinateParser.parseGeoCoordinate(columnValue)) {
				return new ValueDetectionType(DataType.coordinate, null, null, null);
			}
			if (validLenght) {
				try {
					LocalDateTime dateTime = DateJavaTime.parse(columnValue);
					if (dateTime != null) {
						return new ValueDetectionType(DataType.date, null, null, null);
					}
				} catch (ParseException e1) {
				}

			}
			
			Unit unit = headerUnit;
			Quantity quantity = null;
			UnitCategory unitCategory = null;
			if (headerUnit == null && columnValue != null) {
				quantity = UnitCategoryParser.checkQuantity(columnValue);
				unit = UnitCategoryParser.checkUnit(columnValue, UnitCategoryParser.getUnitCategory("All"));
				
				if(unit != null){
					unitCategory = unit.getUnitCategory();
				}
				
				try {
					columnValue = UnitCategoryParser.transform(columnValue, unit, quantity).toString();
				} catch(ParseException e) {
					logger.trace("ParseException for value: " + columnValue);
	        		//e.printStackTrace();
				}
				

			}
			
			if (validLenght && NumericParser.parseNumeric(columnValue)) {
				return new ValueDetectionType(DataType.numeric, quantity, unit, unitCategory);
			}
		}
		return new ValueDetectionType(DataType.string, null, null, null);
	}

	@Override
	public ColumnType detectTypeForColumn(Object[] attributeValues, String attributeLabel) {

		HashMap<Object, Integer> typeCount = new HashMap<>();
		HashMap<Object, Integer> unitCount = new HashMap<>();
		HashMap<Object, Integer> quantityCount = new HashMap<>();
		Unit unit = UnitCategoryParser.parseUnitFromHeader(attributeLabel);
		Quantity quantity = null;
		UnitCategory unitCategory = null;
		// detect types and units per value 
		int rowCounter = 0; // Skip first line --> header
		for (Object attribute : attributeValues) {
			if (rowCounter != 0) {
				String value = (String) attribute;
				ValueDetectionType cdt = null;

				if (value != null) {
					cdt = detectTypeForValue(value, unit);
				}

				if (cdt != null) {
					MapUtils.increment(typeCount, cdt.getType());
					MapUtils.increment(unitCount, cdt.getUnit());
					MapUtils.increment(quantityCount, cdt.getQuantity());
				}
			}
			rowCounter++;
		}

		// create default order to guarantee that results are reproducible in case multiple types have the same number of votes
		List<Pair<Object, Integer>> typeVotes = new ArrayList<>(typeCount.size());
		for(Object type : DataType.values()) {
			Integer count = typeCount.get(type);
			if(count!=null) {
				typeVotes.add(new Pair<>(type, count));
			}
		}
		
		// majority vote for type
//		Object type = MapUtils.max(typeCount);
		Object type = Q.max(typeVotes, (p)->p.getSecond()).getFirst();
		if (type == null) {
			type = DataType.string;
		}
		
		// majority vote for quantity
		quantity = (Quantity) MapUtils.max(quantityCount);
		
		// majority vote for Unit - if header unit empty
		if (unit == null) {
			unit = (Unit) MapUtils.max(unitCount);
		}
		
		if(unit != null){
			unitCategory = unit.getUnitCategory();
		}
		

		ColumnType resColumnType = new ValueDetectionType((DataType) type, quantity, unit, unitCategory);
		return resColumnType;
	}
}
