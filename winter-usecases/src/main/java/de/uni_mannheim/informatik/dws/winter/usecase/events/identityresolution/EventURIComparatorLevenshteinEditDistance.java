package de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution;

import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinEditDistance;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;
import de.uni_mannheim.informatik.dws.winter.usecase.events.utils.BestListSimilarity;

/**
 * {@link Comparator} for {@link Event}s based on the {@link Event#getUris()} ()}
 * value and their {@link LevenshteinEditDistance} value.
 * Based on the MovieTitleComparatorLevenshtein class
 *
 * @author Daniel Ringler
 *
 */
public class EventURIComparatorLevenshteinEditDistance implements Comparator<Event, Attribute> {
    private static final long serialVersionUID = 1L;

    private BestListSimilarity bestListSimilarity = new BestListSimilarity();
    private LevenshteinEditDistance sim = new LevenshteinEditDistance();
    private double threshold;
    
    private HashMap<ComparatorDetails, String> comparisonResult = new HashMap<ComparatorDetails, String>();

    public EventURIComparatorLevenshteinEditDistance(double t) {
        threshold = t;
    }

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {
    	
    	this.comparisonResult.put(ComparatorDetails.comparatorName, EventURIComparatorLevenshteinEditDistance.class.getName());
    	
    	this.comparisonResult.put(ComparatorDetails.record1Value, record1.getDates().toString());
    	this.comparisonResult.put(ComparatorDetails.record2Value, record2.getDates().toString());
    	
    	double similarity = bestListSimilarity.getBestEditDistanceStripedLowercase(sim, record1.getUris(), record2.getUris(), threshold);
    	
    	this.comparisonResult.put(ComparatorDetails.similarity, Double.toString(similarity));
    	this.comparisonResult.put(ComparatorDetails.postproccesedSimilarity, Double.toString(similarity));
        
    	return similarity;
    	
    }

	@Override
	public Map<ComparatorDetails, String> getComparisonResult() {
		return this.comparisonResult;
	}
}





