package de.uni_mannheim.informatik.dws.winter.webtables.detectors;

public interface RowContentDetector {
	
	int[] detectEmptyHeaderRows(String[][] attributeValues, boolean columBased);
	
	int[] detectSumRow(String[][] attributeValues);

}
