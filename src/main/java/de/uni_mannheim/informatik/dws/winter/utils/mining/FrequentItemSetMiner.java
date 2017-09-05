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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.utils.Distribution;
import de.uni_mannheim.informatik.dws.winter.utils.MapUtils;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class FrequentItemSetMiner<TItem> {

	public Map<Set<TItem>, Integer> calculateFrequentItemSetsOfColumnPositions(Collection<Table> tables, Set<Collection<TItem>> transactions) {

		Map<Set<TItem>, Integer> itemSets = new HashMap<>();
		
		// index transactions by item
		Map<TItem, Set<Collection<TItem>>> transactionIndex = new HashMap<>();
		
		// create all 1-item sets
		for(Collection<TItem> transaction : transactions) {
			Distribution<TItem> itemDistribution = Distribution.fromCollection(transaction);
			
			for(TItem item : itemDistribution.getElements()) {

				// create the item set
				Set<TItem> itemSet = Q.toSet(item);
					
				MapUtils.add(itemSets, itemSet, itemDistribution.getFrequency(item));
				
				Set<Collection<TItem>> t = MapUtils.getFast(transactionIndex, item, (i) -> new HashSet<>());
				t.add(transaction);
			}
		}
			
		// create all i+1 item sets
		boolean hasChanges = false;
			
		// easy access to item sets generated in the last round
		Set<Set<TItem>> OneItemSets = new HashSet<>(itemSets.keySet());
		Set<Set<TItem>> lastItemSets = itemSets.keySet();
		Set<Set<TItem>> currentItemSets = new HashSet<>();
		
		// loop until no new item sets are discovered
		do {
			
			// iterate over all item sets created in the last round
			for(Set<TItem> itemset1 : lastItemSets) {
				
				// and combine them with the 1-item sets to create new item sets
				for(Set<TItem> itemset2 : OneItemSets) { 
					
					if(!itemset1.equals(itemset2) && !itemset1.containsAll(itemset2)) {
					
						Set<TItem> itemset = new HashSet<>();
						
						itemset.addAll(itemset1);
						itemset.addAll(itemset2);

						currentItemSets.add(itemset);							
					}
					
				}
			}

			// calculate frequency of new item sets
			Iterator<Set<TItem>> it = currentItemSets.iterator();
			while(it.hasNext()) {
				Set<TItem> itemSet = it.next();
				
				Set<Collection<TItem>> commonTransactions = null;
				
				for(TItem item : itemSet) {
					
					Set<Collection<TItem>> transactionsWithItem = transactionIndex.get(item); 
					
					if(commonTransactions==null) {
						commonTransactions = transactionsWithItem;
					} else {
						commonTransactions = Q.intersection(commonTransactions, transactionsWithItem);
					}
					
					if(commonTransactions.size()==0) {
						break;
					}
				}
				
				if(commonTransactions.size()==0) {
					it.remove();
				} else {
					itemSets.put(itemSet, commonTransactions.size());
				}
			}
			
			hasChanges = currentItemSets.size()>0;
			lastItemSets = currentItemSets;
			currentItemSets = new HashSet<>();
			
		} while(hasChanges);

		return itemSets;
	}
	
}
