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
package de.uni_mannheim.informatik.dws.winter.usecase.products.model;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVMatchableReader;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ValueNormalizer;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategoryParser;
import de.uni_mannheim.informatik.dws.winter.usecase.countries.model.Country;

import java.io.File;
import java.time.LocalDateTime;

/**
 * 
 * Reader for Product records from CSV files applying data normalization.
 * 
 * @author Alexander Brinkmann (alex.brinkmann@informatik.uni-mannheim.de)
 *
 */
public class CSVProductReader extends CSVMatchableReader<Product, Attribute> {

	private ValueNormalizer valueNormalizer;

	/**
	 * 
	 * 
	 *
	 */
	public CSVProductReader() {
	}

	protected void initialiseDataset(DataSet<Product, Attribute> dataset) {
		// the schema is defined in the Product class and not interpreted from the
		// file, so we have to set the attributes manually
		dataset.addAttribute(Product.TITLE);
		dataset.addAttribute(Product.CATEGORY);
		dataset.addAttribute(Product.BRAND);
		dataset.addAttribute(Product.DESCRIPTION);
		dataset.addAttribute(Product.PRICE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_mannheim.informatik.wdi.model.io.CSVMatchableReader#readLine(java.
	 * lang.String[], de.uni_mannheim.informatik.wdi.model.DataSet)
	 */

	@Override
	protected void readLine(File file, int rowNumber, String[] values, DataSet<Product, Attribute> dataset) {

		if (rowNumber == 0) {
			initialiseDataset(dataset);
		} else {
			// generate new record of type city
			Product r = new Product(values[0], file.getAbsolutePath());

			// set values of record
			r.setCategory(values[1]);
			r.setBrand(values[2]);
			r.setDescription(values[3]);
			r.setPrice(values[4]);
			r.setTitle(values[5]);



			dataset.add(r);

		}

	}

}
