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
package de.uni_mannheim.informatik.dws.winter.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Class representing a gold standard data.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class MatchingGoldStandard implements Serializable{

	private static final long serialVersionUID = 1L;
	private List<Pair<String, String>> positiveExamples;
	private List<Pair<String, String>> negativeExamples;
	private Set<String> canonicalPositiveExamples;
	private Set<String> canonicalNegativeExamples;
	private boolean isComplete = false;
	
	/**
	 * 
	 * @return Returns all positive examples in this gold standard
	 */
	public List<Pair<String, String>> getPositiveExamples() {
		return positiveExamples;
	}

	/**
	 * 
	 * @return Returns all negative examples in this gold standard
	 */
	public List<Pair<String, String>> getNegativeExamples() {
		return negativeExamples;
	}

	/**
	 * Adds a positive example to the gold standard
	 * 
	 * @param example
	 */
	public void addPositiveExample(Pair<String, String> example) {
		positiveExamples.add(example);
		canonicalPositiveExamples.add(getCanonicalExample(example.getFirst(),
				example.getSecond()));
	}

	/**
	 * Adds a negative example to the gold standard
	 * 
	 * @param example
	 */
	public void addNegativeExample(Pair<String, String> example) {
		negativeExamples.add(example);
		canonicalNegativeExamples.add(getCanonicalExample(example.getFirst(),
				example.getSecond()));
	}

	/**
	 * Checks if the gold standard contains the given combination of record ids as a positive example
	 * 
	 * @param id1
	 * @param id2
	 * @return true if the gold standard contains the combination as positive example
	 */
	public boolean containsPositive(String id1, String id2) {
		String c = getCanonicalExample(id1, id2);

		return canonicalPositiveExamples.contains(c);
	}

	/**
	 * Checks if the gold standard contains the given combination of records as
	 * a positive example
	 * 
	 * @param record1
	 *            the first record
	 * @param record2
	 *            the second record
	 * @return true if the gold standard contains the combination as positive example
	 */
	public boolean containsPositive(Matchable record1, Matchable record2) {
		String c = getCanonicalExample(record1, record2);

		return canonicalPositiveExamples.contains(c);
	}

	/**
	 * Checks if the gold standard contains the given combination of record ids
	 * as a negative example
	 * 
	 * @param id1
	 * @param id2
	 * @return true if the gold standard contains the combination as negative example
	 */
	public boolean containsNegative(String id1, String id2) {
		String c = getCanonicalExample(id1, id2);

		return canonicalNegativeExamples.contains(c);
	}

	/**
	 * Checks if the gold standard contains the given combination of records as
	 * a negative example
	 * 
	 * @param record1
	 *            the first record
	 * @param record2
	 *            the second record
	 * @return true if the gold standard contains the combination as negative example
	 */
	public boolean containsNegative(Matchable record1, Matchable record2) {
		String c = getCanonicalExample(record1, record2);

		return canonicalNegativeExamples.contains(c);
	}


	/***
	 * Sets whether the gold standard is complete. 
	 * In a complete gold standard, every example that is not in the list of correct examples is considered wrong.
	 * If a gold standard is not complete (i.e. partial), examples that are not in the gold standard are ignored.
	 * @param isComplete
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	/***
	 * 
	 * In a complete gold standard, every example that is not in the list of correct examples is considered wrong.
	 * If a gold standard is not complete (i.e. partial), examples that are not in the gold standard are ignored.
	 * @return Returns whether the gold standard is complete.
	 */
	public boolean isComplete() {
		return isComplete;
	}
	
	public MatchingGoldStandard() {
		positiveExamples = new LinkedList<>();
		negativeExamples = new LinkedList<>();
		canonicalPositiveExamples = new HashSet<>();
		canonicalNegativeExamples = new HashSet<>();
	}

	/**
	 * Loads a gold standard from a CSV file
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void loadFromCSVFile(File file) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(file));

		readAllLines(reader);

		reader.close();

		printGSReport();
	}

	/**
	 * Read all lines. Add positive and negative examples.
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void readAllLines(CSVReader reader) throws IOException {
		String[] values = null;

		while ((values = reader.readNext()) != null) {

			if (values.length == 3) {

				boolean isPositive = Boolean.parseBoolean(values[2]);

				Pair<String, String> example = new Pair<String, String>(
						values[0], values[1]);

				if (isPositive) {
					addPositiveExample(example);
				} else {
					addNegativeExample(example);
				}

			} else {
				System.err.println(String.format("Skipping malformed line: %s",
						StringUtils.join(values,",")));
			}
		}
	}

	/**
	 * Loads a gold standard from a TSV file
	 *
	 * @param file
	 * @throws IOException
	 */
	public void loadFromTSVFile(File file) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(file),  '\t');

		readAllLines(reader);

		reader.close();

		printGSReport();

	}

	private void printGSReport() {
		int numPositive = getPositiveExamples().size();
		int numNegative = getNegativeExamples().size();
		int ttl = numPositive + numNegative;
		double positivePerCent = (double) numPositive / (double) ttl * 100.0;
		double negativePerCent = (double) numNegative / (double) ttl * 100.0;

		System.out
				.println(String
						.format("The gold standard has %d examples\n\t%d positive examples (%.2f%%)\n\t%d negative examples (%.2f%%)",
								ttl, numPositive, positivePerCent, numNegative,
								negativePerCent));

		// check for duplicates
		if (getPositiveExamples().size() != canonicalPositiveExamples.size()) {
			System.err
					.println("The gold standard contains duplicate positive examples!");
		}
		if (getNegativeExamples().size() != canonicalNegativeExamples.size()) {
			System.err
					.println("The gold standard contains duplicate negative examples!");
		}

		// check if any example was labeled as positive and negative
		HashSet<String> allExamples = new HashSet<>();
		allExamples.addAll(canonicalPositiveExamples);
		allExamples.addAll(canonicalNegativeExamples);

		if (allExamples.size() != ttl) {
			System.err
					.println("The gold standard contains an example that is both labelled as positive and negative!");
		}

	}

	public void printBalanceReport() {
		int numPositive = getPositiveExamples().size();
		int numNegative = getNegativeExamples().size();
		int ttl = numPositive + numNegative;
		double positivePerCent = (double) numPositive / (double) ttl;
		double negativePerCent = (double) numNegative / (double) ttl;

		if (Math.abs(positivePerCent - negativePerCent) > 0.2) {
			System.err.println("The gold standard is imbalanced!");
		}
	}

	private String getCanonicalExample(String id1, String id2) {
		String first, second;

		if (id1.compareTo(id2) <= 0) {
			first = id1;
			second = id2;
		} else {
			first = id2;
			second = id1;
		}

		return first + "|" + second;
	}

	private String getCanonicalExample(Matchable record1, Matchable record2) {
		String first, second;

		if (record1.getIdentifier().compareTo(record2.getIdentifier()) <= 0) {
			first = record1.getIdentifier();
			second = record2.getIdentifier();
		} else {
			first = record2.getIdentifier();
			second = record1.getIdentifier();
		}

		return first + "|" + second;
	}
	
	/**
	 * 
	 * Removes all examples from the gold standard that are not contained in the given collection of records
	 * 
	 * @param records
	 */
	public <T extends Matchable>void removeNonexistingExamples(DataSet<T,?> records) {
		Iterator<Pair<String, String>> it = positiveExamples.iterator();
		while(it.hasNext()) {
			Pair<String, String> example = it.next();
			if(records.getRecord(example.getFirst())==null && records.getRecord(example.getSecond())==null) {
				it.remove();
				
				canonicalPositiveExamples.remove(getCanonicalExample(example.getFirst(), example.getSecond()));
			}
		}
		it = negativeExamples.iterator();
		while(it.hasNext()) {
			Pair<String, String> example = it.next();
			if(records.getRecord(example.getFirst())==null && records.getRecord(example.getSecond())==null) {
				it.remove();
				
				canonicalNegativeExamples.remove(getCanonicalExample(example.getFirst(), example.getSecond()));
			}
		}
	}
}
