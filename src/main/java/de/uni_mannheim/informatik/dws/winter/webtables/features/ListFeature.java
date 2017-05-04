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
package de.uni_mannheim.informatik.dws.winter.webtables.features;

import java.util.ArrayList;
import java.util.Collections;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ListFeature implements Feature {

	public double calculate(Table t) {
		// check if all columns are of type string
		for(TableColumn tc : t.getColumns()) {
			if(tc.getDataType()!=DataType.string) {
				return 0.0;
			}
		}
		
		ArrayList<String> values = new ArrayList<>();
		ArrayList<String> valuesSorted1 = new ArrayList<>();
		ArrayList<String> valuesSorted2 = new ArrayList<>();
		
		// get all values from left to right, top to bottom
		for(TableRow r : t.getRows()) {
			for(TableColumn tc : t.getColumns()) {
				String value = (String)r.get(tc.getColumnIndex());
				if(value!=null && !value.trim().isEmpty()) {
					 valuesSorted1.add(value);
					 values.add(value);
				}
			}
		}
		
		// get all values from top to bottom, left to right
		for(TableColumn tc : t.getColumns()) {
			for(TableRow r : t.getRows()) {
				String value = (String)r.get(tc.getColumnIndex());
				if(value!=null && !value.trim().isEmpty()) {
					 valuesSorted2.add(value);
				}
			}
		}
		
		// sort all values alphabetically
		Collections.sort(values);
		
		// compare order of values in table to sorted values
		boolean isSorted = values.equals(valuesSorted1) || values.equals(valuesSorted2);
		
		return isSorted ? 1.0 : 0.0;
	}
	
}
