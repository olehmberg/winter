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

import java.io.Serializable;

/**
 * A correspondence between two records. Does not track causal correspondences.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class SimpleCorrespondence<RecordType extends Matchable> implements Serializable {

	private static final long serialVersionUID = 1L;
	private RecordType firstRecord;
	private RecordType secondRecord;
	private double similarityScore;

	/**
	 * 
	 * @return returns the first record
	 */
	public RecordType getFirstRecord() {
		return firstRecord;
	}

	/**
	 * sets the first record
	 * 
	 * @param firstRecord
	 */
	public void setFirstRecord(RecordType firstRecord) {
		this.firstRecord = firstRecord;
	}

	/**
	 * 
	 * @return returns the second record
	 */
	public RecordType getSecondRecord() {
		return secondRecord;
	}

	/**
	 * sets the second record
	 * 
	 * @param secondRecord
	 */
	public void setSecondRecord(RecordType secondRecord) {
		this.secondRecord = secondRecord;
	}

	/**
	 * 
	 * @return returns the similarity score
	 */
	public double getSimilarityScore() {
		return similarityScore;
	}

	/**
	 * sets the similarity score
	 * 
	 * @param similarityScore
	 */
	public void setsimilarityScore(double similarityScore) {
		this.similarityScore = similarityScore;
	}

	public SimpleCorrespondence() {
		
	}
	
	public SimpleCorrespondence(RecordType first, RecordType second,
			double similarityScore) {
		firstRecord = first;
		secondRecord = second;
		this.similarityScore = similarityScore;
	}
	
	public void changeDirection() {
		RecordType tmp = getFirstRecord();
		setFirstRecord(getSecondRecord());
		setSecondRecord(tmp);
	}
	
	public String getIdentifiers() {
		return String.format("%s/%s", getFirstRecord().getIdentifier(), getSecondRecord().getIdentifier());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SimpleCorrespondence) {
			SimpleCorrespondence<?> cor2 = (SimpleCorrespondence<?>)obj;
			return getFirstRecord().equals(cor2.getFirstRecord()) && getSecondRecord().equals(cor2.getSecondRecord());
		} else {
			return super.equals(obj);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 997 * (getFirstRecord().hashCode()) ^ 991 * (getSecondRecord().hashCode());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String id1 = getFirstRecord()==null ? "" : getFirstRecord().getIdentifier();
		String id2 = getSecondRecord()==null ? "" : getSecondRecord().getIdentifier();
		
		return String.format("[%s]%s<->[%s]%s (%f)", id1, getFirstRecord(), id2, getSecondRecord(), getSimilarityScore());
	}
}
