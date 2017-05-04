/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package de.uni_mannheim.informatik.dws.winter.webtables.detectors.tabletypeclassifier;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.util.CoreMap;

/**
 * Creates the feature set per column, which is used to predict a column´s data
 * type.
 * 
 * @author Sanikumar Zope
 * @author Alexander Brinkmann
 * 
 */

public class FeatureSet {

	DecimalFormat df = new DecimalFormat("#.##");
	private MaxentTagger maxentTagger;

	public FeatureSet(MaxentTagger maxentTagger) {
		super();
		this.maxentTagger = maxentTagger;
	}

	// column features
	private double FractionofCellswithNumnericContent = -1;
	private double AverageNumberofDataTokensinEachCell = -1;
	private double AverageNumberofSpecialCharactersinEachCell = -1;
	private double AverageNumberofPunctuationsinEachCell = -1;
	private boolean IsAlphanumeric = false;
	private String CellContentPattern = null;

	private double PercentageofNumericCharacters = -1;
	private double PercentageofAlphabeticCharacters = -1;
	private double PercentageofSpecialCharacters = -1;
	private double PercentageofPunctuationCharacters = -1;

	private String POSPatternofColumn = null;
	private String POSPatternofHeaderCell = null;

	private int NumberofDistinctValuesinColumn = -1;

	// header cell features
	private boolean HasHeaderCell = false;
	private boolean ContainSpecialCharactersinHeaderCell = false;
	private boolean ContainPunctuationCharactersinHeaderCell = false;

	// features added after first successful run of model
	private boolean IsDateorTime = false;
	private int AverageCharacterLenghth = -1;
	private boolean IsBooleanValue = false;

	OtherOperations otherOperations = new OtherOperations();

	public double getFractionofCellswithNumnericContent() {
		return FractionofCellswithNumnericContent;
	}

	public void setFractionofCellswithNumnericContent(
			double fractionofCellswithNumnericContent) {
		FractionofCellswithNumnericContent = fractionofCellswithNumnericContent;
	}

	public double getAverageNumberofDataTokensinEachCell() {
		return AverageNumberofDataTokensinEachCell;
	}

	public void setAverageNumberofDataTokensinEachCell(
			double averageNumberofDataTokensinEachCell) {
		AverageNumberofDataTokensinEachCell = averageNumberofDataTokensinEachCell;
	}

	public double getAverageNumberofSpecialCharactersinEachCell() {
		return AverageNumberofSpecialCharactersinEachCell;
	}

	public void setAverageNumberofSpecialCharactersinEachCell(
			double averageNumberofSpecialCharactersinEachCell) {
		AverageNumberofSpecialCharactersinEachCell = averageNumberofSpecialCharactersinEachCell;
	}

	public double getAverageNumberofPunctuationsinEachCell() {
		return AverageNumberofPunctuationsinEachCell;
	}

	public void setAverageNumberofPunctuationsinEachCell(
			double averageNumberofPunctuationsinEachCell) {
		AverageNumberofPunctuationsinEachCell = averageNumberofPunctuationsinEachCell;
	}

	public boolean isIsAlphanumeric() {
		return IsAlphanumeric;
	}

	public void setIsAlphanumeric(boolean isAlphanumeric) {
		IsAlphanumeric = isAlphanumeric;
	}

	public String getCellContentPattern() {
		return CellContentPattern;
	}

	public void setCellContentPattern(String cellContentPattern) {
		CellContentPattern = cellContentPattern;
	}

	public double getPercentageofNumericCharacters() {
		return PercentageofNumericCharacters;
	}

	public void setPercentageofNumericCharacters(
			double percentageofNumericCharacters) {
		PercentageofNumericCharacters = percentageofNumericCharacters;
	}

	public double getPercentageofAlphabeticCharacters() {
		return PercentageofAlphabeticCharacters;
	}

	public void setPercentageofAlphabeticCharacters(
			double percentageofAlphabeticCharacters) {
		PercentageofAlphabeticCharacters = percentageofAlphabeticCharacters;
	}

	public double getPercentageofSpecialCharacters() {
		return PercentageofSpecialCharacters;
	}

	public void setPercentageofSpecialCharacters(
			double percentageofSpecialCharacters) {
		PercentageofSpecialCharacters = percentageofSpecialCharacters;
	}

	public double getPercentageofPunctuationCharacters() {
		return PercentageofPunctuationCharacters;
	}

	public void setPercentageofPunctuationCharacters(
			double percentageofPunctuationCharacters) {
		PercentageofPunctuationCharacters = percentageofPunctuationCharacters;
	}

	public boolean isHasHeaderCell() {
		return HasHeaderCell;
	}

	public void setHasHeaderCell(boolean hasHeader) {
		HasHeaderCell = hasHeader;
	}

	public boolean isContainSpecialCharactersinHeaderCell() {
		return ContainSpecialCharactersinHeaderCell;
	}

	public void setContainSpecialCharactersinHeaderCell(
			boolean containSpecialCharactersinHeaderRow) {
		ContainSpecialCharactersinHeaderCell = containSpecialCharactersinHeaderRow;
	}

	public boolean isContainPunctuationCharactersinHeaderCell() {
		return ContainPunctuationCharactersinHeaderCell;
	}

	public void setContainPunctuationCharactersinHeaderCell(
			boolean containPunctuationCharactersinHeaderRow) {
		ContainPunctuationCharactersinHeaderCell = containPunctuationCharactersinHeaderRow;
	}

	public String getPOSPatternofColumn() {
		return POSPatternofColumn;
	}

	public void setPOSPatternofColumn(String pOSPatternofCell) {
		POSPatternofColumn = pOSPatternofCell;
	}

	public String getPOSPatternofHeaderCell() {
		return POSPatternofHeaderCell;
	}

	public void setPOSPatternofHeaderCell(String pOSPatternofHeaderCell) {
		POSPatternofHeaderCell = pOSPatternofHeaderCell;
	}

	public int getNumberofDistinctValuesinColumn() {
		return NumberofDistinctValuesinColumn;
	}

	public void setNumberofDistinctValuesinColumn(
			int numberofDistinctValuesinColumn) {
		NumberofDistinctValuesinColumn = numberofDistinctValuesinColumn;
	}

	public int getAverageCharacterLenghth() {
		return AverageCharacterLenghth;
	}

	public void setAverageCharacterLenghth(int characterLenghthuptoFive) {
		this.AverageCharacterLenghth = characterLenghthuptoFive;
	}

	public boolean isIsDateorTime() {
		return IsDateorTime;
	}

	public void setIsDateorTime(boolean isDateorTime) {
		IsDateorTime = isDateorTime;
	}

	public boolean isIsBooleanValue() {
		return IsBooleanValue;
	}

	public void setIsBooleanValue(boolean isBooleanValue) {
		IsBooleanValue = isBooleanValue;
	}

	/**
	 * serves as a general entrance point and organizes the feature creation.
	 * 
	 * @param column
	 *            - Input column
	 * @param pipeline
	 *            - Pipeline for SUTParser
	 */

	public void createFeatures(String[] column, AnnotationPipeline pipeline) {

		int rowCounter = 0;
		String headerCelltemp = "";
		Map<String, Integer> ccpList = new TreeMap<String, Integer>();
		int length = 0;
		int resultSUTParser = 0;
		int resultBooleanValue = 0;

		// Loop once through one column
		for (String cell : column) {

			if (cell == null)
				continue;
			else {

				if (rowCounter < 2) {
					headerCelltemp = prepareHasHeaderCell(cell, headerCelltemp);
				}
				ccpList = prepareCellContentPattern(cell, ccpList);
				length = prepareAvgCharLength(cell, length);
				resultSUTParser = prepareSUTParser(cell, pipeline,
						resultSUTParser);
				resultBooleanValue = prepareBooleanValue(cell,
						resultBooleanValue);
				rowCounter++;
			}
		}

		// Validate loop results
		validateHasHeaderCell(headerCelltemp);
		validateCellContentPattern(ccpList);
		setAverageCharacterLenghth(length / column.length);
		validateSUTParser(resultSUTParser, column.length);
		validateBooleanValue(resultBooleanValue, column.length);

		if (isHasHeaderCell() && column[0] != null) {
			containPunctuationCharactersinHeaderCell(column[0]);
		}
		if (column[0] != null)
			posPatternofHeaderCell(column[0]);

		// check for whole content
		String content = otherOperations.getColumnContentWithoutSpaces(column);
		validatePercentageofAlphabeticCharacters(content);
		validatePercentageofPunctuationCharacters(content);

	}

	/**
	 * Header cell is detected by comparing the cell content pattern of the
	 * first two rows. Therefore the cell content pattern prepared for the
	 * comparison.
	 * 
	 * @param cell
	 *            Cell content
	 * @param headerCelltemp
	 *            temporary headerValues for the calculation.
	 * @return returns temporary headerValues for the calculation.
	 */
	private String prepareHasHeaderCell(String cell, String headerCelltemp) {
		String textPattern = null;

		if (!cell.trim().isEmpty() && !cell.trim().equals("-")
				&& !cell.trim().equals("--") && !cell.trim().equals("---")
				&& !cell.trim().equals("n/a") && !cell.trim().equals("N/A")
				&& !cell.trim().equals("(n/a)")
				&& !cell.trim().equals("Unknown")
				&& !cell.trim().equals("unknown") && !cell.trim().equals("?")
				&& !cell.trim().equals("??") && !cell.trim().equals(".")) {
			textPattern = cell //
					.replace("\\s", "").replaceAll("[a-zA-Z]+", "a") // alphabetical
					.replaceAll("[0-9]+", "d")// digits
					// http://www.enchantedlearning.com/grammar/punctuation/
					.replaceAll("[^a-zA-z\\d\\s.!;():?,\\-'\"]+", "s")// special
																		// (no
																		// alphabetical,
																		// digits
																		// or
																		// punctuation)
					.replaceAll("[\\s.!;():?,\\-'\"]+", "p"); // punctuation
			headerCelltemp = headerCelltemp + textPattern + " ";
		}
		return headerCelltemp;
	}

	/**
	 * Uses the prepared content from prepareHasHeaderCell to compare the first
	 * two rows of a column. If they differ in content pattern then it implies
	 * that a column has header cell else it does not. If a column has header
	 * cell then true is assigned, otherwise false is assigned.
	 * 
	 * @param headerCelltemp
	 *            temporary headerValues for the calculation.
	 */

	private void validateHasHeaderCell(String headerCelltemp) {
		if (headerCelltemp.trim().split("\\s").length >= 2) {
			if (headerCelltemp.split("\\s")[0]
					.equals(headerCelltemp.split("\\s")[1]))
				setHasHeaderCell(false);
			else
				setHasHeaderCell(true);
		} else
			setHasHeaderCell(false);
	}

	/**
	 * calculates the percentage of alphabetic characters in a string and sets
	 * the corresponding values as PercentageofAlphabeticCharacters.
	 * 
	 * @param content
	 *            String holding the whole column`s input.
	 */
	private void validatePercentageofAlphabeticCharacters(String content) {
		if (content.length() == 0)
			setPercentageofAlphabeticCharacters(-1);
		String alphabeticContent = content.replaceAll("[^a-zA-Z]", "");
		if ((double) alphabeticContent.length() != 0) {
			double result = ((double) alphabeticContent.length())
					/ content.length();
			if (result != 0.0)
//				setPercentageofAlphabeticCharacters(Double.valueOf(df.format(result))); // why?
				setPercentageofAlphabeticCharacters(result);
			else
				setPercentageofAlphabeticCharacters(-1);
		} else
			setPercentageofAlphabeticCharacters(-1);
	}

	/**
	 * calculates the percentage of punctuation characters in a string and sets
	 * the corresponding values as PercentageofPunctionCharacters.
	 * 
	 * @param content
	 *            String holding the whole column`s input.
	 */

	private void validatePercentageofPunctuationCharacters(String content) {
		if (content.length() == 0)
			setPercentageofPunctuationCharacters(-1);
		String punctuationContent = content.replaceAll("[^\\s.!;():?,\\-'\"]+",
				"");
		if ((double) punctuationContent.length() != 0) {
			double result = ((double) punctuationContent.length())
					/ content.length();
			if (result != 0)
//				setPercentageofPunctuationCharacters(Double.valueOf(df.format(result))); // why?
				setPercentageofPunctuationCharacters(result);
			else
				setPercentageofPunctuationCharacters(-1);
		} else
			setPercentageofPunctuationCharacters(-1);
	}

	/**
	 * prepares the predication of a CellContentPattern.
	 * 
	 * @param cell
	 * @param ccpList
	 *            stores needed text patterns.
	 * @return returns needed text patterns.
	 */

	private Map<String, Integer> prepareCellContentPattern(String cell,
			Map<String, Integer> ccpList) {
		String textPattern = null;
		if ((!cell.trim().isEmpty()) && (!cell.trim().equals("-")
				&& !cell.trim().equals("--") && !cell.trim().equals("---")
				&& !cell.trim().equals("n/a") && !cell.trim().equals("N/A")
				&& !cell.trim().equals("(n/a)")
				&& !cell.trim().equals("Unknown")
				&& !cell.trim().equals("unknown") && !cell.trim().equals("?")
				&& !cell.trim().equals("??") && !cell.trim().equals(".")
				&& !cell.trim().equals("null") && !cell.trim().equals("NULL")
				&& !cell.trim().equals("Null"))) {
			textPattern = cell //
					.replace("\\s", "").replaceAll("[a-zA-Z]+", "a") // alphabetical
					.replaceAll("[0-9]+", "d")// digits
					// http://www.enchantedlearning.com/grammar/punctuation/
					.replaceAll("[^a-zA-z\\d\\s.!;():?,\\-'\"]+", "s")// special
																		// (no
																		// alphabetical,
																		// digits
																		// or
																		// punctuation)
					.replaceAll("[\\s.!;():?,\\-'\"]+", "p"); // punctuation

			if (ccpList.containsKey(textPattern))
				ccpList.put(textPattern, ccpList.get(textPattern) + 1);
			else
				ccpList.put(textPattern, 1);
		}
		return ccpList;
	}

	private void validateCellContentPattern(Map<String, Integer> ccpList) {
		if (!ccpList.isEmpty()) {
			setCellContentPattern(
					(otherOperations.entriesSortedByValues(ccpList)).last()
							.getKey());
		} else
			setCellContentPattern(null);
		ccpList.clear();
	}

	/**
	 * Returns true if punctuations are present in header cell an otherwise
	 * false.
	 * 
	 * @param cell
	 *            Holds the column´s cell
	 */

	public void containPunctuationCharactersinHeaderCell(String cell) {
		if ((!cell.trim().isEmpty()) && (!cell.trim().equals("-")
				&& !cell.trim().equals("--") && !cell.trim().equals("---")
				&& !cell.trim().equals("n/a") && !cell.trim().equals("N/A")
				&& !cell.trim().equals("(n/a)")
				&& !cell.trim().equals("Unknown")
				&& !cell.trim().equals("unknown") && !cell.trim().equals("?")
				&& !cell.trim().equals("??") && !cell.trim().equals(".")
				&& !cell.trim().equals("null") && !cell.trim().equals("NULL")
				&& !cell.trim().equals("Null"))) {
			String temp = cell.replaceAll("[^\\s.!;():?,\\-'\"]+", "").trim();
			if (temp.length() < 1)
				setContainPunctuationCharactersinHeaderCell(false);
			else
				setContainPunctuationCharactersinHeaderCell(true);
		} else
			setContainPunctuationCharactersinHeaderCell(false);
	}

	/**
	 * Uses a MaxentTagger to determine the POS tag of a header cell.
	 * 
	 * @param cell
	 *            Holds the column´s cell
	 */

	public void posPatternofHeaderCell(String cell) {
		Map<String, Integer> posPatternHD = new TreeMap<String, Integer>();

		if ((!cell.trim().isEmpty()) && (!cell.trim().equals("-")
				&& !cell.trim().equals("--") && !cell.trim().equals("---")
				&& !cell.trim().equals("n/a") && !cell.trim().equals("N/A")
				&& !cell.trim().equals("(n/a)")
				&& !cell.trim().equals("Unknown")
				&& !cell.trim().equals("unknown") && !cell.trim().equals("?")
				&& !cell.trim().equals("??") && !cell.trim().equals(".")
				&& !cell.trim().equals("null") && !cell.trim().equals("NULL")
				&& !cell.trim().equals("Null"))) {
			// The tagged string
			String tagged = maxentTagger.tagString(cell);

			String[] temp = tagged.split("\\s");
			String POSPattern = "";
			for (String pattern : temp) {
				POSPattern += pattern.substring(pattern.indexOf("_") + 1) + "-";
			}

			String POSPatternofCell = POSPattern.trim()
					.substring(0, POSPattern.length() - 1)
					.replaceAll("--", "-");
			// if(POSPatternofCell.endsWith("-") && POSPatternofCell.length() >
			// 3)
			// POSPatternofCell = POSPatternofCell.substring(0,
			// POSPattern.length()-2);
			// else
			// POSPatternofCell = POSPatternofCell.substring(0,
			// POSPatternofCell.length()-1);

			if (posPatternHD.containsKey(POSPatternofCell))
				posPatternHD.put(POSPatternofCell,
						posPatternHD.get(POSPatternofCell) + 1);
			else
				posPatternHD.put(POSPatternofCell, 1);

			// posPatternHD = otherOperations.sortByValue(posPatternHD);
			setPOSPatternofHeaderCell(
					(otherOperations.entriesSortedByValues(posPatternHD)).last()
							.getKey());
		} else
			setPOSPatternofHeaderCell(null);

		posPatternHD.clear();
	}

	/**
	 * Sums up the total length of all strings in a column.
	 * 
	 * @param cell
	 *            Holds the column´s cell
	 * @param length
	 *            current length of cell before this operation.
	 * @return current length of cell after this operation.
	 */

	private int prepareAvgCharLength(String cell, int length) {
		if ((!cell.trim().isEmpty()) && (!cell.trim().equals("-")
				&& !cell.trim().equals("--") && !cell.trim().equals("---")
				&& !cell.trim().equals("n/a") && !cell.trim().equals("N/A")
				&& !cell.trim().equals("(n/a)")
				&& !cell.trim().equals("Unknown")
				&& !cell.trim().equals("unknown") && !cell.trim().equals("?")
				&& !cell.trim().equals("??") && !cell.trim().equals(".")
				&& !cell.trim().equals("null") && !cell.trim().equals("NULL")
				&& !cell.trim().equals("Null")))
			length = length + cell.trim().length();
		return length;
	}

	/**
	 * Prepares the check for a temporal expression.
	 * 
	 * @param cell
	 *            Holds the column´s cell
	 * @param pipeline
	 *            Used for temporal expressions.
	 * @param result
	 *            Holds the intermediate result before executing this operation.
	 * @return Holds the intermediate result after executing this operation.
	 */

	private int prepareSUTParser(String cell, AnnotationPipeline pipeline,
			int result) {
		if ((!cell.trim().isEmpty()) && (!cell.trim().equals("-")
				&& !cell.trim().equals("--") && !cell.trim().equals("---")
				&& !cell.trim().equals("n/a") && !cell.trim().equals("N/A")
				&& !cell.trim().equals("(n/a)")
				&& !cell.trim().equals("Unknown")
				&& !cell.trim().equals("unknown") && !cell.trim().equals("?")
				&& !cell.trim().equals("??") && !cell.trim().equals(".")
				&& !cell.trim().equals("null") && !cell.trim().equals("NULL")
				&& !cell.trim().equals("Null"))) {
			Annotation annotation = new Annotation(cell);
			annotation.set(CoreAnnotations.DocDateAnnotation.class,
					"2013-07-14");
			pipeline.annotate(annotation);

			List<CoreMap> timexAnnsAll = annotation
					.get(TimeAnnotations.TimexAnnotations.class);
			if (timexAnnsAll != null)
				if (!timexAnnsAll.isEmpty())
					result++;
		}
		return result;
	}

	/**
	 * Checks for a temporal expression and set the corresponding feature.
	 * 
	 * @param result
	 *            Holds the intermediate result before executing this operation.
	 * @param columnLength
	 *            total number of cells in a column.
	 */

	private void validateSUTParser(int result, int columnLength) {
		if (result > columnLength / 2)
			setIsDateorTime(true);
		else
			setIsDateorTime(false);
	}

	/**
	 * Counts all boolean values
	 * 
	 * @param cell
	 *            Holds the column´s cell
	 * @param resultBooleanValue
	 *            Holds the intermediate result before executing this operation.
	 * @return Holds the intermediate result after executing this operation.
	 */

	private int prepareBooleanValue(String cell, int resultBooleanValue) {
		if ((!cell.trim().isEmpty()) && (!cell.trim().equals("-")
				&& !cell.trim().equals("--") && !cell.trim().equals("---")
				&& !cell.trim().equals("n/a") && !cell.trim().equals("N/A")
				&& !cell.trim().equals("(n/a)")
				&& !cell.trim().equals("Unknown")
				&& !cell.trim().equals("unknown") && !cell.trim().equals("?")
				&& !cell.trim().equals("??") && !cell.trim().equals(".")
				&& !cell.trim().equals("null") && !cell.trim().equals("NULL")
				&& !cell.trim().equals("Null"))) {
			if (cell.trim().equals("yes") || cell.trim().equals("Yes")
					|| cell.trim().equals("YES") || cell.trim().equals("no")
					|| cell.trim().equals("No") || cell.trim().equals("NO")
					|| cell.trim().equals("1") || cell.trim().equals("0")
					|| cell.trim().equals("true") || cell.trim().equals("True")
					|| cell.trim().equals("TRUE") || cell.trim().equals("false")
					|| cell.trim().equals("False")
					|| cell.trim().equals("FALSE"))
				resultBooleanValue++;
		}
		return resultBooleanValue;
	}

	/**
	 * Validates whether the majority of cell´s represents a boolean value.
	 * 
	 * @param result
	 *            Holds the intermediate result before executing this operation.
	 * @param columnLength
	 *            Total number of cells in a column.
	 */

	private void validateBooleanValue(int result, int columnLength) {
		if (result > columnLength / 2)
			setIsBooleanValue(true);
		else
			setIsBooleanValue(false);
	}

}
