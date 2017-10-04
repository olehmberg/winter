package de.uni_mannheim.informatik.dws.winter.webtables.detectors;

import java.util.ArrayList;
import java.util.List;

public class WebTablesRowContentDetector implements RowContentDetector {

	// detect null values
	private static final List<String> totalRowIndicators = new ArrayList<String>() {
		/**
		* 
		*/
		private static final long serialVersionUID = 1L;

		{
			add("total");
			add("sum");
		}
	};

	/**
	 * Empty rows at the beginning of a table.
	 * 
	 * @param attributeValues:
	 *            Holds the table content
	 * @return the to be skipped row when populating the table.
	 */
	
	@Override
	public int[] detectEmptyHeaderRows(String[][] attributeValues, boolean columnBased) {
		int emptyRows = -1;
		if (columnBased) {
			for (int rowIdx = 0; rowIdx < attributeValues[0].length; rowIdx++) {
				boolean empty = true;
				for (int columnIdx = 0; columnIdx < attributeValues.length; columnIdx++) {
					String value = attributeValues[columnIdx][rowIdx]; 
					if (value!=null && !value.equals("")) {
						empty = false;
						break;
					}
				}
				if (empty) {
					emptyRows = rowIdx;
					break;
				}
			}
		} else {
			for (int rowIdx = 0; rowIdx < attributeValues.length; rowIdx++) {
				String[] rowData = attributeValues[rowIdx];
				boolean empty = true;
				for (String value : rowData) {
					if (value!=null && !value.equals("")) {
						empty = false;
						break;
					}
				}
				if (empty) {
					emptyRows = rowIdx;
					break;
				}
			}

		}
		if (emptyRows > -1) {
			int[] result = new int[emptyRows + 1];
			for (int emptyRow = 0; emptyRow < result.length; emptyRow++) {
				result[emptyRow] = emptyRow;
			}
			return result;
		}
		return null;
	}

	/**
	 * Detectors a sum Row based on the value of the last row´s first value by
	 * using the totalRowIndicators
	 * 
	 * @param attributeValues:
	 *            Holds the table content
	 * @return the to be skipped row when populating the table.
	 */
	
	@Override
	public int[] detectSumRow(String[][] attributeValues) {
		String value = attributeValues[attributeValues.length - 1][0];
		if(value!=null) {
			value = value.toLowerCase();
			if (totalRowIndicators.contains(value)) {
				int[] result = { attributeValues.length - 1 };
				return result;
			}
		}
		return null;
	}

}
