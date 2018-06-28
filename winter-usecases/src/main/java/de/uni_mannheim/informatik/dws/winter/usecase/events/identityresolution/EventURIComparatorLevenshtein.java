package de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;

import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
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
    
    private HashMap<Integer, String> comparisonResult = new HashMap<Integer, String>();

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {
    	
    	this.comparisonResult.put(Comparator.comparatorName, EventURIComparatorLevenshtein.class.getName());
    	
    	this.comparisonResult.put(Comparator.record1Value, record1.getDates().toString());
    	this.comparisonResult.put(Comparator.record2Value, record2.getDates().toString());
    	
    	double similarity = bestListSimilarity.getBestStripedStringSimilarity(sim, record1.getUris(), record2.getUris());
    	
    	this.comparisonResult.put(Comparator.similarity, Double.toString(similarity));
        
    	return similarity;
    }

	@Override
	public Map<Integer, String> getComparisonResult() {
		// TODO Auto-generated method stub
		return this.comparisonResult;
	}

}



