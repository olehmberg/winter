package de.uni_mannheim.informatik.dws.winter.matching.rules.comparators;

import java.util.concurrent.ConcurrentHashMap;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;

/**
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 *	Logs the comparison logs per comparator and per thread if logging is activated.
 *
 */
public class ComparatorLogger {

	private ConcurrentHashMap<Long,Record> comparatatorLogPerThread = new ConcurrentHashMap<Long, Record>();
	
	public void initialise(){
		 Thread currThread = Thread.currentThread();
		 comparatatorLogPerThread.put(currThread.getId(), new Record(Long.toString(currThread.getId())));
	}

	public String getComparatorName() {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		return currRecord.getValue(COMPARATORNAME);
	}

	public void setComparatorName(String comparatorName) {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		currRecord.setValue(COMPARATORNAME, comparatorName);
	}

	public String getRecord1Value() {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		return currRecord.getValue(RECORD1VALUE);
	}

	public void setRecord1Value(String record1Value) {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		currRecord.setValue(RECORD1VALUE, record1Value);
	}

	public String getRecord2Value() {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		return currRecord.getValue(RECORD2VALUE);
	}

	public void setRecord2Value(String record2Value) {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		currRecord.setValue(RECORD2VALUE, record2Value);
	}

	public String getRecord1PreprocessedValue() {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		String Record1PreprocessedValue = currRecord.getValue(RECORD1PREPROCESSEDVALUE);
		if (Record1PreprocessedValue == null) {
			return getRecord1Value();
		}
		return Record1PreprocessedValue;
	}

	public void setRecord1PreprocessedValue(String record1PreprocessedValue) {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		currRecord.setValue(RECORD1PREPROCESSEDVALUE, record1PreprocessedValue);
	}

	public String getRecord2PreprocessedValue() {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		String Record2PreprocessedValue = currRecord.getValue(RECORD2PREPROCESSEDVALUE);
		if (Record2PreprocessedValue == null) {
			return getRecord2Value();
		}
		return Record2PreprocessedValue;
	}

	public void setRecord2PreprocessedValue(String record2PreprocessedValue) {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		currRecord.setValue(RECORD2PREPROCESSEDVALUE, record2PreprocessedValue);
	}

	public String getSimilarity() {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		return currRecord.getValue(SIMILARITY);
	}

	public void setSimilarity(String similarity) {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		currRecord.setValue(SIMILARITY, similarity);
	}

	public String getPostprocessedSimilarity() {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		String postprocessedSimilarity = currRecord.getValue(POSTPROCESSEDSIMILARITY);
		if (postprocessedSimilarity == null) {
			return getSimilarity();
		}
		return postprocessedSimilarity;
	}

	public void setPostprocessedSimilarity(String postprocessedSimilarity) {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		currRecord.setValue(POSTPROCESSEDSIMILARITY, postprocessedSimilarity);
	}
	
	public String getValue(Attribute attribute) {
		Record currRecord = comparatatorLogPerThread.get(Thread.currentThread().getId());
		return currRecord.getValue(attribute);
	}

	public static final Attribute COMPARATORNAME = new Attribute("comparatorName");
	public static final Attribute RECORD1VALUE = new Attribute("record1Value");
	public static final Attribute RECORD2VALUE = new Attribute("record2Value");
	public static final Attribute RECORD1PREPROCESSEDVALUE = new Attribute("record1PreprocessedValue");
	public static final Attribute RECORD2PREPROCESSEDVALUE = new Attribute("record2PreprocessedValue");
	public static final Attribute SIMILARITY = new Attribute("similarity");
	public static final Attribute POSTPROCESSEDSIMILARITY = new Attribute("postproccesedSimilarity");

	public static final Attribute[] COMPARATORLOG = { COMPARATORNAME, RECORD1VALUE, RECORD2VALUE,
			RECORD1PREPROCESSEDVALUE, RECORD2PREPROCESSEDVALUE, SIMILARITY, POSTPROCESSEDSIMILARITY };


	public boolean hasValue(Attribute attribute) {
		if (attribute == COMPARATORNAME)
			return getComparatorName() != null && !getComparatorName().isEmpty();
		else if (attribute == RECORD1VALUE)
			return getRecord1Value() != null && !getRecord1Value().isEmpty();
		else if (attribute == RECORD2VALUE)
			return getRecord2Value() != null && !getRecord2Value().isEmpty();
		else if (attribute == RECORD1PREPROCESSEDVALUE)
			return getRecord1PreprocessedValue() != null && !getRecord1PreprocessedValue().isEmpty();
		else if (attribute == RECORD2PREPROCESSEDVALUE)
			return getRecord2PreprocessedValue() != null && !getRecord2PreprocessedValue().isEmpty();
		else if (attribute == SIMILARITY)
			return getSimilarity() != null && !getSimilarity().isEmpty();
		else if (attribute == POSTPROCESSEDSIMILARITY)
			return getPostprocessedSimilarity() != null && !getPostprocessedSimilarity().isEmpty();
		else
			return false;
	}

	@Override
	public String toString() {
		return String.format("[Comparison Log: %s / %s / %s  / %s / %s / %s / %s ]", getComparatorName(),
				getRecord1Value(), getRecord1Value(), getRecord1PreprocessedValue(), getRecord2PreprocessedValue(),
				getSimilarity(), getPostprocessedSimilarity());
	}
}
