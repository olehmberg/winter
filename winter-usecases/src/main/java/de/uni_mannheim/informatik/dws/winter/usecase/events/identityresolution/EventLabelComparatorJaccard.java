package de.uni_mannheim.informatik.dws.winter.usecase.events.identityresolution;

import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;
import de.uni_mannheim.informatik.dws.winter.usecase.events.utils.BestListSimilarity;

/**
 * {@link Comparator} for {@link Event}s based on the {@link Event#getLabels()}
 * value and their {@link TokenizingJaccardSimilarity} value.
 *
 * @author Daniel Ringler
 *
 */
public class EventLabelComparatorJaccard implements Comparator<Event, Attribute> {

    private static final long serialVersionUID = 1L;
    private BestListSimilarity bestListSimilarity = new BestListSimilarity();
    private TokenizingJaccardSimilarity sim = new TokenizingJaccardSimilarity();
    
    private HashMap<Integer, String> comparisonResult = new HashMap<Integer, String>();

    @Override
    public double compare(
            Event record1,
            Event record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {
    	
    	this.comparisonResult.put(Comparator.comparatorName, EventLabelComparatorJaccard.class.getName());
    	
    	this.comparisonResult.put(Comparator.record1Value, record1.getDates().toString());
    	this.comparisonResult.put(Comparator.record2Value, record2.getDates().toString());
    	
    	double similarity = bestListSimilarity.getBestStringSimilarity(sim, record1.getLabels(), record2.getLabels());
    	
    	this.comparisonResult.put(Comparator.similarity, Double.toString(similarity));
    	this.comparisonResult.put(Comparator.postproccesedSimilarity, Double.toString(similarity));
    	
        return similarity;
    }

	@Override
	public Map<Integer, String> getComparisonResult() {
		return this.comparisonResult;
	}
}
