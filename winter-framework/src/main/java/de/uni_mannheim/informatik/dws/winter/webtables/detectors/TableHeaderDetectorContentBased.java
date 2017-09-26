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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Alexander Brinkmann
 *	Detect Header based on cell Content
 */

public class TableHeaderDetectorContentBased implements TableHeaderDetector {

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
		int flag = 0;
		String[] firstRow = myRowMap.get(0);
		String[] secondRow = myRowMap.get(1);
		
		for (int i = 0; i < firstRow.length; i++) {
//			String p1 = myTableStats.extractPatternFromCell(firstRow[i]);
//			String p2 = myTableStats.extractPatternFromCell(secondRow[i]);
			if(firstRow[i].length() <= 10){
				if(extractPatternFromCell(firstRow[i]).equals(extractPatternFromCell(secondRow[i]))) {
					flag ++;
				}
			}
		}
		
		if(flag < myRowMap.get(1).length - 1){
			flag = 0;
			Pattern emailpattern = Pattern.compile("^.+@.+\\..+$");
			Matcher emailMatcher;
			for(int i = 1; i < myRowMap.size() - 1; i++){
				String[] currentRow = myRowMap.get(i);
				String[] nextRow = myRowMap.get(i + 1);
				for (int j = 0; j < myRowMap.get(1).length; j++) {
					emailMatcher = emailpattern.matcher(currentRow[j]);
					if(emailMatcher.matches()){
						break;
					}
					else if(!extractPatternFromCell(currentRow[j]).equals(extractPatternFromCell(nextRow[j]))) {
//						return new HeaderDetection(-1, false);
						flag++;
					}
				}	
			}
		
			if(flag > myRowMap.size()*(2)){
				return null;
			}
			else{
				int[] result = new int[]{0};
				return result;
			}
		}
		else{
			return null;
		}
			
	}
	
	/**
	 * Define the extraction pattern.
	 * @param cell
	 * @return a cleaned cell value
	 */
	public String extractPatternFromCell(String cell) {
		String cellPattern = cell //
				.replace("\\s", "")
				.replaceAll("[a-zA-Z]+", "a") // alphabetical
				.replaceAll("[0-9]+", "d")// digits
				// http://www.enchantedlearning.com/grammar/punctuation/
				.replaceAll("[^ad\\s.!;():?,\\-'\"]+", "s")// special (no alphabetical, digits or punctuation)
				.replaceAll("[\\s.!;():?,\\-'\"]+", "p"); // punctuation
		
		return cellPattern;
	}

}
