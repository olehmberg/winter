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
package de.uni_mannheim.informatik.dws.winter.model.defaultmodel;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.io.RDFMatchableReader;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class RDFRecordReader extends RDFMatchableReader<Record, Attribute> {

	private int idIndex = -1;
	private Map<String, Attribute> attributeMapping;

	/**
	 * 
	 * @param idColumnIndex
	 * 		The index of the column that contains the ID attribute. Specify -1 if the file does not contain a unique ID attribute.
	 */
	public RDFRecordReader(int idColumnIndex) {
		this.idIndex = idColumnIndex;
	}

	/**
	 * 
	 * @param idColumnIndex
	 * 		The index of the column that contains the ID attribute. Specify -1 if the file does not contain a unique ID attribute.
	 * @param attributeMapping
	 * 		The position of a column and the corresponding attribute
	 */
	public RDFRecordReader(int idColumnIndex, Map<String, Attribute> attributeMapping) {
		this.idIndex = idColumnIndex;
		this.attributeMapping = attributeMapping;
	}

	/* (non-Javadoc)
	 * @see de.uni_mannheim.informatik.wdi.model.io.CSVMatchableReader#readLine(java.lang.String[], de.uni_mannheim.informatik.wdi.model.DataSet)
	 */
	@Override
	protected void readLine(File file, int rowNumber, String[] values, DataSet<Record, Attribute> dataset) {
		
		Set<String> ids = new HashSet<>();
		
		if(rowNumber==0) {
			
			for(int i = 0; i < values.length; i++) {
				String v = values[i];
				String attributeId = String.format("%s_Col%d", file.getName(), i);
				Attribute a = new Attribute(attributeId, file.getAbsolutePath());
				a.setName(v);
				dataset.addAttribute(a);
			}
			
		} else {
			
			String id = String.format("%s_%d", file.getName(), rowNumber);
			
			if(idIndex>=0 && values[idIndex]!=null) {
				id = values[idIndex];
				
				if(ids.contains(id)) {
					String replacementId = String.format("%s_%d", file.getName(), rowNumber);
					System.err.println(String.format("Id '%s' (line %d) already exists, using '%s' instead!", id, rowNumber, replacementId));
					id = replacementId;
				}
				
				ids.add(id);
			}
			
			Record r = new Record(id, file.getAbsolutePath());
			
			for(int i = 0; i < values.length; i++) {
				Attribute a;
				if(this.attributeMapping == null){
					String attributeId = String.format("%s_Col%d", file.getName(), i);
					a = dataset.getAttribute(attributeId);
				}
				else{
					a = this.attributeMapping.get(Integer.toString(i));
				}
				
				String v = values[i];
				
				if(v.isEmpty()) {
					v = null;
				}
				
				r.setValue(a, v);
			}
			
			dataset.add(r);
			
		}
		
	}

}
