package de.uni_mannheim.informatik.dws.winter.usecase.countries;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.CSVRecordReader;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.RecordCSVFormatter;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.preprocessing.DataSetNormalizer;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.ValueDetectionType;
import de.uni_mannheim.informatik.dws.winter.preprocessing.units.UnitCategoryParser;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.usecase.countries.model.Country;
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
		Map<String, Attribute> columnMappingCountry = new HashMap<>();
		columnMappingCountry.put("﻿Index", Country.ID);
		columnMappingCountry.put("Name", Country.NAME);
		columnMappingCountry.put("Population", Country.POPULATION);
		columnMappingCountry.put("Area", Country.AREA);
		columnMappingCountry.put("Speed Limit", Country.SPEEDLIMIT);
		columnMappingCountry.put("Date Latest Constitution", Country.LATESTCONSTITUTION);
		
		// load data
		DataSet<Record, Attribute> dataCountry = new HashedDataSet<>();
		new CSVRecordReader(0, columnMappingCountry).loadFromCSV(new File("usecase/country/input/countries.csv"), dataCountry);
		
		// Create column type mapping
		Map<Attribute, ValueDetectionType> columnTypeMapping = new HashMap<>();
		
		columnTypeMapping.put(Country.ID, new ValueDetectionType(DataType.string, null));
		columnTypeMapping.put(Country.NAME, new ValueDetectionType(DataType.string, null));
		columnTypeMapping.put(Country.POPULATION, new ValueDetectionType(DataType.numeric, null));
		columnTypeMapping.put(Country.AREA, new ValueDetectionType(DataType.numeric, UnitCategoryParser.getUnitCategory("Area")));
		columnTypeMapping.put(Country.SPEEDLIMIT, new ValueDetectionType(DataType.numeric, UnitCategoryParser.getUnitCategory("Speed")));
		columnTypeMapping.put(Country.LATESTCONSTITUTION, new ValueDetectionType(DataType.date, null));
		
		// normalize data set
		new DataSetNormalizer<Record>().normalizeDataset(dataCountry, columnTypeMapping);
		
		// export data
		 new RecordCSVFormatter().writeCSV(new File("usecase/country/output/countries.csv"), dataCountry, null);
		 logger.info("DataSet City written to file!");
	}

}
