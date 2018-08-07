package de.uni_mannheim.informatik.dws.winter.datafusion;

import java.io.Serializable;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
/**
 * Logger for the attribute fusion process.
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 *
 * @param <RecordType>	the type that represents a record
 */
public class AttributeFusionLogger extends Record implements Serializable {

	private static final long serialVersionUID = 1L;

	public AttributeFusionLogger(String identifier) {
		super(identifier);
	}

	public String getValueIDS() {
		return this.getValue(VALUEIDS);
	}

	public void setValueIDS(String valueIDS) {
		this.setValue(VALUEIDS, valueIDS);
	}

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

	public final static Attribute VALUEIDS = new Attribute("ValueIDS");
	public final static Attribute VALUES = new Attribute("Values");
	public final static Attribute FUSEDVALUE = new Attribute("FusedValue");

	/**
	 * Check whether a specific attribute exists.
	 */
	@Override
	public boolean hasValue(Attribute attribute) {
		if (attribute == VALUEIDS)
			return getValueIDS() != null && !getValueIDS().isEmpty();
		else if (attribute == VALUES)
			return getValues() != null && !getValues().isEmpty();
		else if (attribute == FUSEDVALUE)
			return getFusedValue() != null && !getFusedValue().isEmpty();
		else
			return false;
	}

	@Override
	public String toString() {
		return String.format("[Fusion Log: %s / %s / %s ]", getValueIDS(),
				getValues(), getFusedValue());
	}
}
