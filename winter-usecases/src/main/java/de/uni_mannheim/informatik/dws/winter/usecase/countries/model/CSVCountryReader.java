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
package de.uni_mannheim.informatik.dws.winter.usecase.countries.model;

import java.io.File;
import java.time.LocalDateTime;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVMatchableReader;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ValueNormalizer;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategoryParser;

/**
 * 
 * Reader for City records from CSV files applying data normalization.
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */
public class CSVCountryReader extends CSVMatchableReader<Country, Attribute> {

	private ValueNormalizer valueNormalizer;

	/**
	 * 
	 * 
	 * @param normalizeValues
	 *            A flag that triggers the value normalization.
	 */
	public CSVCountryReader() {
		this.valueNormalizer = new ValueNormalizer();
	}

	protected void initialiseDataset(DataSet<Country, Attribute> dataset) {
		// the schema is defined in the City class and not interpreted from the
		// file, so we have to set the attributes manually
		dataset.addAttribute(Country.ID);
		dataset.addAttribute(Country.NAME);
		dataset.addAttribute(Country.POPULATION);
		dataset.addAttribute(Country.AREA);
		dataset.addAttribute(Country.SPEEDLIMIT);
		dataset.addAttribute(Country.LATESTCONSTITUTION);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_mannheim.informatik.wdi.model.io.CSVMatchableReader#readLine(java.
	 * lang.String[], de.uni_mannheim.informatik.wdi.model.DataSet)
	 */

	@Override
	protected void readLine(File file, int rowNumber, String[] values, DataSet<Country, Attribute> dataset) {

		if (rowNumber == 0) {
			initialiseDataset(dataset);
		} else {
			// generate new record of type city
			Country r = new Country(values[0], file.getAbsolutePath());

			// set values of record
			r.setName(values[1]);
			
			// Set data type and convert value
			Object population = valueNormalizer.normalize(values[2], DataType.numeric, null);
			if (population != null) {
				r.setPopulation((Double) population);
			}
			
			// Set data type and convert value
			Object area = valueNormalizer.normalize(values[3], DataType.numeric, UnitCategoryParser.getUnitCategory("Area"));
			if (area != null) {
				r.setArea((Double) area);
			}
			
			// Set data type and convert value
			Object speedLimit = valueNormalizer.normalize(values[4], DataType.numeric, UnitCategoryParser.getUnitCategory("Speed"));
			if (speedLimit != null) {
				r.setSpeedLimit((Double) speedLimit);
			}
			
			// Set data type and convert value
			Object date = valueNormalizer.normalize(values[5], DataType.date, null);
			if (date != null) {
				r.setDateLatestConstitution((LocalDateTime) date);
			}

			dataset.add(r);

		}

	}

}
