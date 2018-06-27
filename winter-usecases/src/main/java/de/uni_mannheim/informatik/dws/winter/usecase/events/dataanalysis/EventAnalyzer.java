package de.uni_mannheim.informatik.dws.winter.usecase.events.dataanalysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;


/**
 * Analyse the event XML data
 * Created on 23/01/17.
 * @author Daniel Ringler
 */
public class EventAnalyzer {
	
	private static final Logger logger = LogManager.getLogger();
	
    public EventAnalyzer() {}

    public void runAnalysis(FusibleDataSet<Event, Attribute> dataSet, String kg) {
        if (dataSet != null && dataSet.size() > 0) {

            logger.info("attribute densities of dataset " + kg);
            dataSet.printDataSetDensityReport();
            //dataSet.printDataSetDensityDistributionReport(true, "dataSetDensityDistributionReport_"+kg+".csv");

        } else {
            logger.info("Analysis not possible (no records or dataset is null).");
        }
    }
}
