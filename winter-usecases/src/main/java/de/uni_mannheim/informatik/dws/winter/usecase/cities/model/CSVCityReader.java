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
package de.uni_mannheim.informatik.dws.winter.usecase.cities.model;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVMatchableReader;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ColumnType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.TypeConverter;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitParser;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * 
 * Reader for City records from CSV files applying data normalization.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */
public class CSVCityReader extends CSVMatchableReader<City, Attribute> {

	private int idIndex = -1;
	private Map<String, Attribute> attributeMapping;
	private Map<Attribute, ColumnType> columnTypeMapping;
	private Attribute[] attributes = null;
	private static final Logger logger = WinterLogManager.getLogger();
	private boolean normalizeValues = false;
	private TypeConverter tc;

	/**
	 * 
	 * @param idColumnIndex
	 *            The index of the column that contains the ID attribute.
	 *            Specify -1 if the file does not contain a unique ID attribute.
	 */
	public CSVCityReader(int idColumnIndex) {
		this.idIndex = idColumnIndex;
		
		initializeAttributeMapping();
	}
	
	/**
	 * 
	 * @param idColumnIndex
	 *            The index of the column that contains the ID attribute.
	 *            Specify -1 if the file does not contain a unique ID attribute.
	 * 
	 * @param normalizeValues
	 * 			  A flag that triggers the value normalization.
	 */
	public CSVCityReader(int idColumnIndex, boolean normalizeValues) {
		this.idIndex = idColumnIndex;
		this.normalizeValues = normalizeValues;
		
		initializeAttributeMapping();
	}
	
	/**
	 * Initialize attribute mapping and columnType mapping if the values shall be initialized.
	 */
	private void initializeAttributeMapping(){
		this.attributeMapping = new HashMap<>();
		
		this.attributeMapping.put("Index", City.ID);
		this.attributeMapping.put("label", City.NAME);
		this.attributeMapping.put("population", City.POPULATION);
		this.attributeMapping.put("country", City.COUNTRY);
		this.attributeMapping.put("countryCode", City.COUNTRYCODE);
		this.attributeMapping.put("lat", City.LATITUDE);
		this.attributeMapping.put("long", City.LONGITUDE);
		this.attributeMapping.put("officialName", City.OFFICIALNAME);
		
		if(this.normalizeValues){
			this.columnTypeMapping = new HashMap<>();
			
			this.columnTypeMapping.put(City.ID, new ColumnType(DataType.numeric, null));
			this.columnTypeMapping.put(City.NAME, new ColumnType(DataType.string, null));
			this.columnTypeMapping.put(City.POPULATION, new ColumnType(DataType.numeric, UnitParser.getUnit("thousand")));
			
			this.columnTypeMapping.put(City.COUNTRY, new ColumnType(DataType.string, null));
			this.columnTypeMapping.put(City.COUNTRYCODE, new ColumnType(DataType.string, null));
			this.columnTypeMapping.put(City.LATITUDE, new ColumnType(DataType.coordinate, null));
			this.columnTypeMapping.put(City.LONGITUDE, new ColumnType(DataType.coordinate, null));
			this.columnTypeMapping.put(City.OFFICIALNAME, new ColumnType(DataType.string, null));
			
			this.tc = new TypeConverter();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_mannheim.informatik.wdi.model.io.CSVMatchableReader#readLine(java.
	 * lang.String[], de.uni_mannheim.informatik.wdi.model.DataSet)
	 */

	@Override
	protected void readLine(File file, int rowNumber, String[] values, DataSet<City, Attribute> dataset) {

		Set<String> ids = new HashSet<>();

		if (rowNumber == 0) {

			attributes = new Attribute[values.length];

			for (int i = 0; i < values.length; i++) {
				String v = values[i];
				Attribute a = this.attributeMapping.get(v);

				attributes[i] = a;
				a.setName(v);
				dataset.addAttribute(a);
			}

		} else {

			String id = String.format("%s_%d", file.getName(), rowNumber);

			if (idIndex >= 0 && values[idIndex] != null) {
				id = values[idIndex];

				if (ids.contains(id)) {
					String replacementId = String.format("%s_%d", file.getName(), rowNumber);
					logger.error(String.format("Id '%s' (line %d) already exists, using '%s' instead!", id, rowNumber,
							replacementId));
					id = replacementId;
				}

				ids.add(id);
			}

			City r = new City(id, file.getAbsolutePath());

			for (int i = 0; i < values.length; i++) {
				Attribute a = attributes[i];
				String v = values[i];
				
				if (v.isEmpty()) {
					v = null;
				}
				
				if(v != null && this.normalizeValues){
					ColumnType columntype = this.columnTypeMapping.get(a);
					Object value = tc.typeValue(v, columntype.getType(), columntype.getUnit());
						if(value != null){
							v = value.toString(); 
						}
				}

				r.setValue(a, v);
			}

			dataset.add(r);

		}

	}
	
}
