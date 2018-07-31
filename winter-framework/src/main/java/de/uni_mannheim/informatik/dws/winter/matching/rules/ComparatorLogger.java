package de.uni_mannheim.informatik.dws.winter.matching.rules;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;

public class ComparatorLogger extends Record implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ComparatorLogger(String identifier) {
		super(identifier);
	}
	
	private String comparatorName;
	private String record1Value;
	private String record2Value;
	private String record1PreprocessedValue;
	private String record2PreprocessedValue;
	private String similarity;
	private String postprocessedSimilarity;

	public String getComparatorName() {
		return comparatorName;
	}
	public void setComparatorName(String comparatorName) {
		this.comparatorName = comparatorName;
	}
	public String getRecord1Value() {
		return record1Value;
	}
	public void setRecord1Value(String record1Value) {
		this.record1Value = record1Value;
	}
	public String getRecord2Value() {
		return record2Value;
	}
	public void setRecord2Value(String record2Value) {
		this.record2Value = record2Value;
	}
	public String getRecord1PreprocessedValue() {
		if(record1PreprocessedValue == null){
			return getRecord1Value();
		}
		return record1PreprocessedValue;
	}
	public void setRecord1PreprocessedValue(String record1PreprocessedValue) {
		this.record1PreprocessedValue = record1PreprocessedValue;
	}
	public String getRecord2PreprocessedValue() {
		if(record2PreprocessedValue == null){
			return getRecord2Value();
		}
		return record2PreprocessedValue;
	}
	public void setRecord2PreprocessedValue(String record2PreprocessedValue) {
		this.record2PreprocessedValue = record2PreprocessedValue;
	}
	public String getSimilarity() {
		return similarity;
	}
	public void setSimilarity(String similarity) {
		this.similarity = similarity;
	}
	public String getPostproccesedSimilarity() {
		if(postprocessedSimilarity == null){
			return getSimilarity();
		}
		return postprocessedSimilarity;
	}
	public void setPostprocessedSimilarity(String postprocessedSimilarity) {
		this.postprocessedSimilarity = postprocessedSimilarity;
	}
	
	private Map<Attribute, Collection<String>> provenance = new HashMap<>();
	private Collection<String> recordProvenance;

	public void setRecordProvenance(Collection<String> provenance) {
		recordProvenance = provenance;
	}

	public Collection<String> getRecordProvenance() {
		return recordProvenance;
	}

	public void setAttributeProvenance(Attribute attribute, Collection<String> provenance) {
		this.provenance.put(attribute, provenance);
	}

	public Collection<String> getAttributeProvenance(String attribute) {
		return provenance.get(attribute);
	}

	public String getMergedAttributeProvenance(Attribute attribute) {
		Collection<String> prov = provenance.get(attribute);

		if (prov != null) {
			return StringUtils.join(prov, "+");
		} else {
			return "";
		}
	}

	public static final Attribute COMPARATORNAME 			= new Attribute("comparatorName");
	public static final Attribute RECORD1VALUE 				= new Attribute("record1Value");
	public static final Attribute RECORD2VALUE 				= new Attribute("record2Value");
	public static final Attribute RECORD1PREPROCESSEDVALUE 	= new Attribute("record1PreprocessedValue");
	public static final Attribute RECORD2PREPROCESSEDVALUE 	= new Attribute("record2PreprocessedValue");
	public static final Attribute SIMILARITY 				= new Attribute("similarity");
	public static final Attribute POSTPROCESSEDSIMILARITY	= new Attribute("postproccesedSimilarity");
	
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
			return getPostproccesedSimilarity() != null && !getPostproccesedSimilarity().isEmpty();
		else
			return false;
	}

	@Override
	public String toString() {
		return String.format("[Comparison Log: %s / %s / %s  / %s / %s / %s / %s ]", 
				getComparatorName(), getRecord1Value(), getRecord1Value(), getRecord1PreprocessedValue(),
				getRecord2PreprocessedValue(), getSimilarity(), getPostproccesedSimilarity());
	}
}
