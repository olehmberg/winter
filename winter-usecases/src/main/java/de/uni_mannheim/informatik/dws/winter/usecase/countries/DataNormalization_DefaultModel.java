package de.uni_mannheim.informatik.dws.winter.usecase.countries;

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
import de.uni_mannheim.informatik.dws.winter.usecase.countries.model.Country;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.PatternbasedTypeDetector;


public class DataNormalization_DefaultModel {
	
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
		DataSet<Record, Attribute> dataCountry = new HashedDataSet<>();
		new CSVRecordReader(0).loadFromCSV(new File("usecase/country/input/countries.csv"), dataCountry);
		
		// normalize dataset
		new DataSetNormalizer<Record>().normalizeDataset(dataCountry, new PatternbasedTypeDetector());
		
		// export data
		 new RecordCSVFormatter().writeCSV(new File("usecase/country/output/countries.csv"), dataCountry, null);
		 logger.info("DataSet City written to file!");
	}

}
