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

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVMatchableReader;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.TypeConverter;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitParser;

/**
 * 
 * Reader for City records from CSV files applying data normalization.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */
public class CSVCityReader extends CSVMatchableReader<City, Attribute> {

	private boolean normalizeValues = false;
	private TypeConverter tc;

	/**
	 * 
	 * 
	 * @param normalizeValues
	 *            A flag that triggers the value normalization.
	 */
	public CSVCityReader(boolean normalizeValues) {
		this.normalizeValues = normalizeValues;
		if (this.normalizeValues) {
			this.tc = new TypeConverter();
		}
	}

	protected void initialiseDataset(DataSet<City, Attribute> dataset) {
		// the schema is defined in the City class and not interpreted from the
		// file, so we have to set the attributes manually
		dataset.addAttribute(City.COUNTRYCODE);
		dataset.addAttribute(City.NAME);
		dataset.addAttribute(City.LATITUDE);
		dataset.addAttribute(City.LONGITUDE);
		dataset.addAttribute(City.LONGITUDE);
		dataset.addAttribute(City.OFFICIALNAME);

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

		if (rowNumber == 0) {
			initialiseDataset(dataset);
		} else {
			// generate new record of type city
			City r = new City(values[0], file.getAbsolutePath());

			// set values of record
			r.setCountryCode(values[1]);
			r.setName(values[2]);

			if (this.normalizeValues) {
				Object value = tc.typeValue(values[3], DataType.coordinate, null);
				if (value != null) {
					r.setLatitude(Double.parseDouble(value.toString()));
				}
			} else {
				r.setLatitude(Double.parseDouble(values[3]));
			}

			if (this.normalizeValues) {
				Object value = tc.typeValue(values[4], DataType.coordinate, null);
				if (value != null) {
					r.setLongitude(Double.parseDouble(value.toString()));
				}
			} else {
				r.setLongitude(Double.parseDouble(values[4]));
			}

			r.setOfficialName(values[5]);
			r.setCountry(values[6]);

			if (this.normalizeValues) {
				// Set data type, parse unit and convert value
				Object value = tc.typeValue(values[7], DataType.numeric, UnitParser.checkUnit(values[7]));
				if (value != null) {
					r.setPopulation((Double) value);
				}
			} else {
				// Use regex to retrieve double
				String population = values[7].replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
				if (population != null && ! population.isEmpty()) {
					r.setPopulation(Double.parseDouble(population));
				}
			}
			
			dataset.add(r);

		}

	}

}
