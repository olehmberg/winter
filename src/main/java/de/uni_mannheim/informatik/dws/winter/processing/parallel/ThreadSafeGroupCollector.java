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
package de.uni_mannheim.informatik.dws.winter.processing.parallel;

import java.util.concurrent.ConcurrentHashMap;

import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Group;
import de.uni_mannheim.informatik.dws.winter.processing.GroupCollector;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * Thread-Safe implementation of {@link GroupCollector}.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class ThreadSafeGroupCollector<KeyType, RecordType> extends GroupCollector<KeyType, RecordType> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConcurrentHashMap<KeyType, Processable<RecordType>> groups;
	private Processable<Group<KeyType, RecordType>> result; 
	
	/**
	 * @return the result
	 */
	public Processable<Group<KeyType, RecordType>> getResult() {
		return result;
	}
	
	@Override
	public void initialise() {
		groups = new ConcurrentHashMap<>();
		result = new ParallelProcessableCollection<>();
	}

	@Override
	public void next(Pair<KeyType, RecordType> record) {
		Processable<RecordType> list = groups.get(record.getFirst());

		if(list==null) {
			list = groups.putIfAbsent(record.getFirst(), new ParallelProcessableCollection<RecordType>());
			list = groups.get(record.getFirst());
		}
		
		list.add(record.getSecond());
	}

	@Override
	public void finalise() {
		for(KeyType key : groups.keySet()) {
			Group<KeyType, RecordType> g = new Group<>(key, groups.get(key));
			result.add(g);
		}
	}

}
