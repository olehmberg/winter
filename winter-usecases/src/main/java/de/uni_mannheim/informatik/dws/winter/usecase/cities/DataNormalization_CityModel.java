package de.uni_mannheim.informatik.dws.winter.usecase.cities;

import java.io.File;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.cities.model.CSVCityReader;
import de.uni_mannheim.informatik.dws.winter.usecase.cities.model.City;
import de.uni_mannheim.informatik.dws.winter.usecase.cities.model.CityCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;


public class DataNormalization_CityModel {
	
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
		DataSet<City, Attribute> dataCity = new HashedDataSet<>();
		new CSVCityReader(0, true).loadFromCSV(new File("usecase/city/input/city.csv"), dataCity);
		
		// export data
		 new CityCSVFormatter().writeCSV(new File("usecase/city/output/city.csv"), dataCity, null);
		 logger.info("DataSet City written to file!");
	}

}
