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
package de.uni_mannheim.informatik.dws.winter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;

/**
 * An implementation of a Data set backed by a HashMap
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 */
public class HashedDataSet<RecordType extends Matchable, SchemaElementType extends Matchable> extends ProcessableCollection<RecordType> implements DataSet<RecordType, SchemaElementType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * HashMap of an identifier and the actual {@link AbstractRecord}.
	 */
	protected Map<String, RecordType> records;

	public HashedDataSet() {
		records = new HashMap<>();
	}
	
	public HashedDataSet(Collection<RecordType> records) {
		this.records = new HashMap<>();
		for(RecordType r : records) {
			add(r);
		}
	}

	/**
	 * 
	 * @param identifier
	 *            The identifier of the entry that should be returned
	 * @return Returns the entry with the specified identifier or null, if it is not found.
	 */
	@Override
	public RecordType getRecord(String identifier) {
		return records.get(identifier);
	}

	/** 
	 * 
	 * @return Returns the number of entries in this data set
	 */
	@Override
	public int size() {
		return records.size();
	}

	/**
	 * Adds an entry to this data set. Any existing entry with the same identifier will be replaced.
	 * 
	 * @param record
	 */
	@Override
	public void add(RecordType record) {
		records.put(record.getIdentifier(), record);
	}

	/**
	 * 
	 * @return Returns a random record from the data set
	 */
	@Override
	public RecordType getRandomRecord() {
		Random r = new Random();

		List<RecordType> allRecords = new ArrayList<>(records.values());

		int index = r.nextInt(allRecords.size());

		return allRecords.get(index);
	}

	/***
	 * Removes all records from this dataset
	 */
	@Override
	public void ClearRecords() {
		records.clear();
	}
	
	private HashedDataSet<SchemaElementType, SchemaElementType> attributes;
	
	public void addAttribute(SchemaElementType attribute) {
		if(attributes==null) {
			// important: only create the schema data set if actual attributes are added, otherwise we would create an infinite amount of meta-schemas
			attributes = new HashedDataSet<>();
		}
		attributes.add(attribute);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.DataSet#getAttribute(java.lang.String)
	 */
	@Override
	public SchemaElementType getAttribute(String identifier) {
		return attributes.getRecord(identifier);
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.BasicCollection#get()
	 */
	@Override
	public Collection<RecordType> get() {
		return records.values();
	}
	
	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.DataSet#removeRecord(java.lang.String)
	 */
	@Override
	public void removeRecord(String identifier) {
		records.remove(identifier);
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.DataSet#getSchema()
	 */
	@Override
	public DataSet<SchemaElementType, SchemaElementType> getSchema() {
		return attributes;
	}
}
