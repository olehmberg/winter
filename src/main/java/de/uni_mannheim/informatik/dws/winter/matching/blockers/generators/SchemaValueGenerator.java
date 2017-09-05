package de.uni_mannheim.informatik.dws.winter.matching.blockers.generators;


import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollector;

/**
 * 
 * A blocking key generator that generates values as blocking keys from the content of the schema elements.
 * 
 * For example, creates all values of an attribute in a dataset as blocking keys.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 * @param <RecordType>
 * @param <SchemaElementType>
 */
public abstract class SchemaValueGenerator<RecordType extends Matchable, SchemaElementType extends Matchable> extends BlockingKeyGenerator<RecordType, MatchableValue, SchemaElementType> {

	private static final long serialVersionUID = 1L;

	@Override
	public void mapRecordToKey(Pair<RecordType, Processable<Correspondence<MatchableValue, Matchable>>> pair,
			DataIterator<Pair<String, Pair<SchemaElementType, Processable<Correspondence<MatchableValue, Matchable>>>>> resultCollector) {

		
		ProcessableCollector<Pair<String, SchemaElementType>> collector = new ProcessableCollector<>();
		collector.setResult(new ProcessableCollection<>());
		collector.initialise();
		
		// execute the blocking funtion
		generateBlockingKeys(pair.getFirst(), pair.getSecond(), collector);
		
		collector.finalise();
		
		for(Pair<String, SchemaElementType> p : collector.getResult().get()) {
			
			// create causal correspondence from the blocking key
			Processable<Correspondence<MatchableValue, Matchable>> causes = new ProcessableCollection<>();
			MatchableValue matchableValue = new MatchableValue(p.getFirst(), pair.getFirst().getIdentifier(), p.getSecond().getIdentifier());
			Correspondence<MatchableValue, Matchable> causeCor = new Correspondence<>(matchableValue, matchableValue, 1.0);
			causes.add(causeCor);
			
			resultCollector.next(new Pair<>(matchableValue.getValue().toString(), new Pair<>(p.getSecond(), causes)));
			
		}
		
	}
	
}
