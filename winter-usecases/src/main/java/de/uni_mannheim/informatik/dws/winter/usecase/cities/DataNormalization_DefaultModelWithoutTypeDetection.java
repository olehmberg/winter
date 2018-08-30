package de.uni_mannheim.informatik.dws.winter.usecase.cities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.CSVRecordReader;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.RecordCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.preprocessing.DataSetNormalizer;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ValueDetectionType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategoryParser;
import de.uni_mannheim.informatik.dws.winter.usecase.cities.model.City;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;


public class DataNormalization_DefaultModelWithoutTypeDetection {
	
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
		

		// Create column mapping
		Map<String, Attribute> columnMappingCity = new HashMap<>();
		columnMappingCity.put("Index", City.ID);
		columnMappingCity.put("label", City.NAME);
		columnMappingCity.put("population", City.POPULATION);
		columnMappingCity.put("country", City.COUNTRY);
		columnMappingCity.put("countryCode", City.COUNTRYCODE);
		columnMappingCity.put("lat", City.LATITUDE);
		columnMappingCity.put("long", City.LONGITUDE);
		columnMappingCity.put("officialName", City.OFFICIALNAME);
		
		// load data
		DataSet<Record, Attribute> dataCity = new HashedDataSet<>();
		new CSVRecordReader(0, columnMappingCity).loadFromCSV(new File("usecase/city/input/city.csv"), dataCity);
		
		// Create column type mapping
		Map<Attribute, ValueDetectionType> columnTypeMapping = new HashMap<>();
		
		columnTypeMapping.put(City.ID, new ValueDetectionType(DataType.string, null));
		columnTypeMapping.put(City.NAME, new ValueDetectionType(DataType.string, null));
		columnTypeMapping.put(City.POPULATION, new ValueDetectionType(DataType.numeric, null));
		columnTypeMapping.put(City.COUNTRY, new ValueDetectionType(DataType.string, null));
		columnTypeMapping.put(City.COUNTRYCODE, new ValueDetectionType(DataType.string, null));
		columnTypeMapping.put(City.LATITUDE, new ValueDetectionType(DataType.numeric, null));
		columnTypeMapping.put(City.LONGITUDE, new ValueDetectionType(DataType.numeric, null));
		columnTypeMapping.put(City.OFFICIALNAME, new ValueDetectionType(DataType.string, null));
		
		// normalize dataset
		new DataSetNormalizer<Record>().normalizeDataset(dataCity, columnTypeMapping);
		
		// export data
		 new RecordCSVFormatter().writeCSV(new File("usecase/city/output/city.csv"), dataCity, null);
		 logger.info("DataSet City written to file!");
	}

}
