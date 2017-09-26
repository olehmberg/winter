/** 
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.dws.winter.utils.mining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class AssociationRuleMiner<TItem> {

	public Map<Set<TItem>, Set<TItem>> calculateAssociationRulesForColumnPositions(Map<Set<TItem>, Integer> itemSets) {

		// create association rules
		final Map<Set<TItem>, Set<TItem>> rules = new HashMap<>();
		
		// iterate over all frequent item sets
		for(Set<TItem> itemset : itemSets.keySet()) {
			if(itemset.size()>1) {
				
				// move each item from the condition to the consequent, step by step
				for(TItem item : itemset) {
					Set<TItem> condition = new HashSet<>(itemset);
					condition.remove(item);
					Set<TItem> consequent = new HashSet<>();
					consequent.add(item);
					
//					double confidence = (double)itemSets.get(itemset) / (double)itemSets.get(condition);
					
//					if(confidence==1.0) {
						rules.put(condition, consequent);
//					}
				}
				
			}
		}
		
		return rules;
	}
	
}
