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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import de.uni_mannheim.informatik.dws.winter.preprocessing.units.Unit;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitParser;

public class TypeConverter {

	private boolean verbose = false;
	public boolean isVerbose() {
		return verbose;
	}
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * Converts a String into the given type
	 * @param value
	 * @param type
	 * @param unit
	 * @return an object representing the value in the corresponding data type
	 */
    public Object typeValue(String value, DataType type, Unit unit) {
        Object typedValue = null;
        
        if(value!=null) {
	        try {
		        switch (type) {
		            case string:
		                typedValue = value;
		                break;
		            case date:
		                typedValue = DateJavaTime.parse(value);
		                break;
		            case numeric:
		                //TODO: how to handle numbers with commas (German style)
		                if (unit != null) {
		                    typedValue = UnitParser.transformUnit(value, unit);
		
		                } else {
		                    value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
		                    NumberFormat format = NumberFormat.getInstance(Locale.US);
		                    Number number = format.parse(value);
		                    typedValue = number.doubleValue();
		                }
		                break;
		            case bool:
		                typedValue = Boolean.parseBoolean(value);
		                break;
		            case coordinate:
		                typedValue = value;
		                break;
		            case link:
		                typedValue = value;
		            default:
		                break;
		        }
	        } catch(ParseException e) {
	        	if(isVerbose()) {
	        		e.printStackTrace();
	        	}
	        }
        }
        
        return typedValue;
    }
	
}
