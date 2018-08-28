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

	private TypeConverter tc;

	/**
	 * 
	 * 
	 * @param normalizeValues
	 *            A flag that triggers the value normalization.
	 */
	public CSVCityReader() {
		this.tc = new TypeConverter();
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
			
			// Set data type and convert value
			Object latitude = tc.typeValue(values[3], DataType.numeric, null);
			if (latitude != null) {
				r.setLatitude((Double) latitude);
			}
			
			// Set data type and convert value
			Object longitude = tc.typeValue(values[4], DataType.numeric, null);
			if (longitude != null) {
				r.setLongitude(Double.parseDouble(longitude.toString()));
			}

			r.setOfficialName(values[5]);
			r.setCountry(values[6]);

			// Set data type, parse unit and convert value
			Object population = tc.typeValue(values[7], DataType.numeric, UnitParser.checkUnit(values[7]));
			if (population != null) {
				r.setPopulation((Double) population);
			}

			dataset.add(r);

		}

	}

}
