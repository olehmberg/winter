package de.uni_mannheim.informatik.dws.winter.matching.rules;

import java.io.Serializable;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;

public class ComparatorLogger extends Record implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ComparatorLogger(String identifier) {
		super(identifier);
	}

	public String getComparatorName() {
		return this.getValue(COMPARATORNAME);
	}

	public void setComparatorName(String comparatorName) {
		this.setValue(COMPARATORNAME, comparatorName);
	}

	public String getRecord1Value() {
		return this.getValue(RECORD1VALUE);
	}

	public void setRecord1Value(String record1Value) {
		this.setValue(RECORD1VALUE, record1Value);
	}

	public String getRecord2Value() {
		return this.getValue(RECORD2VALUE);
	}

	public void setRecord2Value(String record2Value) {
		this.setValue(RECORD2VALUE, record2Value);
	}

	public String getRecord1PreprocessedValue() {
		String Record1PreprocessedValue = this.getValue(RECORD1PREPROCESSEDVALUE);
		if (Record1PreprocessedValue == null) {
			return getRecord1Value();
		}
		return Record1PreprocessedValue;
	}

	public void setRecord1PreprocessedValue(String record1PreprocessedValue) {
		this.setValue(RECORD1PREPROCESSEDVALUE, record1PreprocessedValue);
	}

	public String getRecord2PreprocessedValue() {
		String Record2PreprocessedValue = this.getValue(RECORD2PREPROCESSEDVALUE);
		if (Record2PreprocessedValue == null) {
			return getRecord2Value();
		}
		return Record2PreprocessedValue;
	}

	public void setRecord2PreprocessedValue(String record2PreprocessedValue) {
		this.setValue(RECORD2PREPROCESSEDVALUE, record2PreprocessedValue);
	}

	public String getSimilarity() {
		return this.getValue(SIMILARITY);
	}

	public void setSimilarity(String similarity) {
		this.setValue(SIMILARITY, similarity);
	}

	public String getPostprocessedSimilarity() {
		String postprocessedSimilarity = this.getValue(POSTPROCESSEDSIMILARITY);
		if (postprocessedSimilarity == null) {
			return getSimilarity();
		}
		return this.getValue(POSTPROCESSEDSIMILARITY);
	}

	public void setPostprocessedSimilarity(String postprocessedSimilarity) {
		this.setValue(POSTPROCESSEDSIMILARITY, postprocessedSimilarity);
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

	@Override
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
