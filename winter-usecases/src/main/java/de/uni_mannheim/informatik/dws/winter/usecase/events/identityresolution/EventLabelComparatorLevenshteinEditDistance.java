package de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution;


import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinEditDistance;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;
import de.uni_mannheim.informatik.dws.winter.usecase.events.utils.BestListSimilarity;

/**
 * {@link Comparator} for {@link Event}s based on the {@link Event#getLabels()}
 * value and their {@link LevenshteinEditDistance} value.
 * Based on the MovieTitleComparatorLevenshtein class
 *
 * @author Daniel Ringler
 *
 */
public class EventLabelComparatorLevenshteinEditDistance implements Comparator<Event, Attribute> {
    private static final long serialVersionUID = 1L;

    private BestListSimilarity bestListSimilarity = new BestListSimilarity();
    private LevenshteinEditDistance sim = new LevenshteinEditDistance();
    private double threshold;
    
    private ComparatorLogger comparisonLog;

    public EventLabelComparatorLevenshteinEditDistance(double t) {
        threshold = t;
    }

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {
    	
    	double similarity = bestListSimilarity.getBestEditDistance(sim, record1.getLabels(), record2.getLabels(), threshold);
    	
    	if(this.comparisonLog != null){
    		this.comparisonLog.setComparatorName(getClass().getName());
    	
    		this.comparisonLog.setRecord1Value(record1.getLabels().toString());
    		this.comparisonLog.setRecord2Value(record2.getLabels().toString());
		
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

