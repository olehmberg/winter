package de.uni_mannheim.informatik.dws.winter.datafusion;

import java.io.Serializable;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
/**
 * Logger for the attribute fusion process.
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 */
public class AttributeFusionLogger extends Record implements Serializable {

	private static final long serialVersionUID = 1L;

	public AttributeFusionLogger(String identifier) {
		super(identifier);
	}

	public String getValueIDS() { return this.getValue(VALUEIDS); }

	public void setValueIDS(String valueIDS) { this.setValue(VALUEIDS, valueIDS); }

	public String getRecordIDS() { return this.getValue(RECORDIDS); }

	public void setRecordIDS(String recordIDS) { this.setValue(RECORDIDS, recordIDS); }

	public String getValues() {
		return this.getValue(VALUES);
	}

	public void setValues(String values) {
		this.setValue(VALUES, values);
	}

	public String getFusedValue() {
		return this.getValue(FUSEDVALUE);
	}

	public void setFusedValue(String fusedValue) {
		this.setValue(FUSEDVALUE, fusedValue);
	}

	public String getAttributeName() {
		return this.getValue(ATTRIBUTE_NAME);
	}

	public void setAttributeName(String attributeName) {
		this.setValue(ATTRIBUTE_NAME, attributeName);
	}

	public String getConsistency() {
		return this.getValue(CONSISTENCY);
	}

	public void setConsistency(Double consistency) {
		this.setValue(CONSISTENCY, Double.toString(consistency));
	}

	public String getIsCorrect() {
		return this.getValue(IS_CORRECT);
	}

	public void setIsCorrect(boolean isCorrect) {
		this.setValue(IS_CORRECT, Boolean.toString(isCorrect));
	}

	public String getCorrectValue() {
		return this.getValue(CORRECT_VALUE);
	}

	public void setCorrectValue(Object value) {
		if(value!=null) {
			this.setValue(CORRECT_VALUE, value.toString());
		}
	}

	public final static Attribute VALUEIDS = new Attribute("ValueIDS");
	public final static Attribute RECORDIDS = new Attribute("RecordIDS");
	public final static Attribute ATTRIBUTE_NAME = new Attribute("AttributeName");
	public final static Attribute VALUES = new Attribute("Values");
	public final static Attribute FUSEDVALUE = new Attribute("FusedValue");
	public final static Attribute CONSISTENCY = new Attribute("Consistency");
	public final static Attribute IS_CORRECT = new Attribute("IsCorrect");
	public final static Attribute CORRECT_VALUE = new Attribute("CorrectValue");

	/**
	 * Check whether a specific attribute exists.
	 */
	@Override
	public boolean hasValue(Attribute attribute) {
		if (attribute == VALUEIDS)
			return getValueIDS() != null && !getValueIDS().isEmpty();
		else if (attribute == RECORDIDS)
			return getRecordIDS() != null && !getValues().isEmpty();
		else if (attribute == VALUES)
			return getValues() != null && !getValues().isEmpty();
		else if (attribute == FUSEDVALUE)
			return getFusedValue() != null && !getFusedValue().isEmpty();
		else 
			return super.hasValue(attribute);
	}

	@Override
	public String toString() {
		return String.format("[Fusion Log: %s / %s / %s ]", getValueIDS(),
				getValues(), getFusedValue());
	}
}
