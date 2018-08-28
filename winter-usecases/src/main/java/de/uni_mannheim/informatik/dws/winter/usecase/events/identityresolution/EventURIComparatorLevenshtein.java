package de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.usecase.events.utils.BestListSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;

/**
 * {@link Comparator} for {@link Event}s based on the {@link Event#getLabels()}
 * value and their {@link LevenshteinSimilarity} value.
 * Based on the MovieTitleComparatorLevenshtein class
 *
 * @author Daniel Ringler
 *
 */
public class EventURIComparatorLevenshtein implements Comparator<Event, Attribute> {
    private static final long serialVersionUID = 1L;

    private BestListSimilarity bestListSimilarity = new BestListSimilarity();
    private LevenshteinSimilarity sim = new LevenshteinSimilarity();
    
    private ComparatorLogger comparisonLog;

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {
    	
    	double similarity = bestListSimilarity.getBestStripedStringSimilarity(sim, record1.getUris(), record2.getUris());
    	
    	if(this.comparisonLog != null){
    		this.comparisonLog.setComparatorName(getClass().getName());
    	
    		this.comparisonLog.setRecord1Value(record1.getUris().toString());
    		this.comparisonLog.setRecord2Value(record2.getUris().toString());
		
    		this.comparisonLog.setSimilarity(Double.toString(similarity));
    	}
        
    	return similarity;
    }

	@Override
	public ComparatorLogger getComparisonLog() {
		return this.comparisonLog;
	}

	@Override
	public void setComparisonLog(ComparatorLogger comparatorLog) {
		this.comparisonLog = comparatorLog;
	}

}



