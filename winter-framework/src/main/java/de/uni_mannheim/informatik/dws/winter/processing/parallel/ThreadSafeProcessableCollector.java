package de.uni_mannheim.informatik.dws.winter.processing.parallel;

import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollector;
import de.uni_mannheim.informatik.dws.winter.utils.parallel.ThreadBoundObject;

public class ThreadSafeProcessableCollector<RecordType> extends ProcessableCollector<RecordType> {

	private static final long serialVersionUID = 1L;

	private ThreadBoundObject<Processable<RecordType>> intermediateResults;
	
	@Override
	public void initialise() {
		super.initialise();
		
		intermediateResults = new ThreadBoundObject<>((t)->new ProcessableCollection<>());
	}
	
	@Override
	public void next(RecordType record) {		
		Processable<RecordType> localResult = intermediateResults.get();
		
		localResult.add(record);
	}
	
	@Override
	public void finalise() {
		Processable<RecordType> result = getResult();
		
		for(Processable<RecordType> partialResult : intermediateResults.getAll()) {
			result = result.append(partialResult);
		}
		
		setResult(result);
	}
}
