package de.metanome.algorithms.hyfd.utils;

public class ValueComparator {

	private boolean isNullEqualNull;
	
	public ValueComparator(boolean isNullEqualNull) {
		this.isNullEqualNull = isNullEqualNull;
	}
	
	public boolean isNullEqualNull() {
		return this.isNullEqualNull;
	}
	
	public boolean isEqual(Object val1, Object val2) {
		if ((val1 == null) && (val2 == null))
			return this.isNullEqualNull;
		
		return (val1 != null) && val1.equals(val2);
	}
	
	public boolean isEqual(int val1, int val2) {
		return (val1 >= 0) && (val2 >= 0) && (val1 == val2);
	}
	
	public boolean isDifferent(int val1, int val2) {
		return (val1 < 0) || (val2 < 0) || (val1 != val2);
	}
}
