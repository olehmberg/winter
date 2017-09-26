package de.uni_mannheim.informatik.dws.winter.usecase.events.dataanalysis;

import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;


/**
 * Analyse the event XML data
 * Created on 23/01/17.
 * @author Daniel Ringler
 */
public class EventAnalyzer {

    public EventAnalyzer() {}

    public void runAnalysis(FusibleDataSet<Event, Attribute> dataSet, String kg) {
        if (dataSet != null && dataSet.size() > 0) {

            System.out.println("attribute densities of dataset " + kg);
            dataSet.printDataSetDensityReport();
            //dataSet.printDataSetDensityDistributionReport(true, "dataSetDensityDistributionReport_"+kg+".csv");

        } else {
            System.out.println("Analysis not possible (no records or dataset is null).");
        }
    }
}
