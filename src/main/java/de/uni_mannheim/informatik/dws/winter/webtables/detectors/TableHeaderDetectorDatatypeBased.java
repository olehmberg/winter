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

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Alexander Brinkmann
 *	Detect Header based on Datatype
 */

public class TableHeaderDetectorDatatypeBased implements TableHeaderDetector {

	@Override
	public int[] detectTableHeader(String[][] attributeValues, int[] skipRows) {
		int analysedRows = 5;
		if(skipRows != null){
			analysedRows = analysedRows + skipRows.length - 1;
		}
		Map<Integer, String[]> myRowMap = new HashMap<Integer, String[]>(analysedRows);
		for (int i=0; i<analysedRows; i++)
		{
		    myRowMap.put(i, (String[]) attributeValues[i]);
		}
		
		boolean[] isString = new boolean[myRowMap.size()];
		for (int i = 0;i < myRowMap.size(); i++) {
			isString[i] = isStringOnly(myRowMap.get(i)); 
		}	
		
		for(int i = 1; i < isString.length; i++){
			if(isString[0] && isString[i]){
				return null;
			}
		}
		int[] result = new int[]{0};
		return result;
	}
	
	public boolean isStringOnly(String[] columnORrow)  
	{  
		int alphaCount = 0, anyCount = 0;
		if (columnORrow.length > 0) {
			
			// count occurrences of alphabetical and numerical
			// characters within the content string
			for(String str : columnORrow){
				for (char c : str.toCharArray()) {
					if(Character.isAlphabetic(c) || Character.isSpaceChar(c)) {
						alphaCount++;
					} 
					else{
					anyCount++;
					}
				}
			}
		}
		
		if(alphaCount > 0 && anyCount == 0)
			return true;
		else
			return false;

	}

}
