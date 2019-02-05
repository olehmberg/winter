package de.uni_mannheim.informatik.dws.winter.usecase.countries;

import java.io.File;

import org.slf4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.countries.model.CSVCountryReader;
import de.uni_mannheim.informatik.dws.winter.usecase.countries.model.Country;
import de.uni_mannheim.informatik.dws.winter.usecase.countries.model.CountryCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;


public class DataNormalization_CountryModel {
	
	/*
	 * Trace Options:
	 * 		default: 	level INFO	- console
	 * 		trace:		level TRACE     - console
	 * 		infoFile:	level INFO	- console/file
	 * 		traceFile:	level TRACE	- console/file
	 *  
	 * To set the log level to trace and write the log to winter.log and console, 
	 * activate the "traceFile" logger as follows:
	 *     private static final Logger logger = WinterLogManager.activateLogger("traceFile");
	 *
	 */

	private static final Logger logger = WinterLogManager.activateLogger("default");
	
	public static void main(String[] args) throws Exception {
		
		// load data
		DataSet<Country, Attribute> dataCountry = new HashedDataSet<>();
		new CSVCountryReader().loadFromCSV(new File("usecase/country/input/countries.csv"), dataCountry);
		
		// export data
		 new CountryCSVFormatter().writeCSV(new File("usecase/country/output/countries.csv"), dataCountry, null);
		 logger.info("Data Set City written to file!");
	}

}
