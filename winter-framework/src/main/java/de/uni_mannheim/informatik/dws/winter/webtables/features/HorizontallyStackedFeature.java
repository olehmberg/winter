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

import java.util.HashMap;
import java.util.LinkedList;

import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.WebTablesStringNormalizer;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class HorizontallyStackedFeature implements Feature {

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.dws.t2k.webtables.features.Feature#calculate(de.uni_mannheim.informatik.dws.t2k.webtables.Table)
	 */
	@Override
	public double calculate(Table t) {
		
		LinkedList<String> columns = new LinkedList<>();
		HashMap<String, Integer> counts = new HashMap<>();
		
		for(TableColumn tc : t.getColumns()) {
			if(
					tc.getHeader()!=null 
					&& !tc.getHeader().trim().isEmpty() 
					&& !tc.getHeader().equalsIgnoreCase(WebTablesStringNormalizer.nullValue)) {
				String col = String.format("%s%s", tc.getHeader(), tc.getDataType()==DataType.string?"string":"nonstring");
				columns.add(col);
				Integer c = counts.get(col);
				if(c==null) {
					c = 0;
				}
				counts.put(col, c+1);
			}
		}
		
		// check if all counts are the same
		int count = -1;
		for(Integer c : counts.values()) {
			if(count==-1) {
				count = c;
			} else {
				if(count!=c) {
					return 0.0;
				}
			}
		}
		
		// count now tells us how often the table is stacked
		// so in the list of columns without empty columns, we know where the column must be repeated
		if(count<2) {
			return 0.0;
		}
		
		int numAttributes = counts.size();
		for(int index=0; index < numAttributes; index++) {
			String col = columns.get(index);
			
			for(int i = 1; i < count; i++) {
				String next = columns.get(index + i * numAttributes);
				if(!col.equals(next)) {
					return 0.0;
				}
			}
		}

		return 1.0;
	}

}
