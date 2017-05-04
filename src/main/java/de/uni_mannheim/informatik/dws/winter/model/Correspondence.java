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
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;

/**
 * Represent a correspondence. Contains two Records and their similarity
 * score.
 * 
 * May also contain causal correspondences that explain how this correspondence was created (correspondence provenance)
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <RecordType>
 */
public class Correspondence<RecordType extends Matchable, CausalType extends Matchable> extends SimpleCorrespondence<RecordType> {

	public static class BySimilarityComparator<RecordType extends Matchable, CausalType extends Matchable> implements Comparator<Correspondence<RecordType, CausalType>> {

		private boolean descending = false;
		
		public BySimilarityComparator() {
			this.descending = false;
		}
		
		public BySimilarityComparator(boolean descending) {
			this.descending = descending;
		}
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Correspondence<RecordType, CausalType> o1, Correspondence<RecordType, CausalType> o2) {
			int comp = Double.compare(o1.getSimilarityScore(), o2.getSimilarityScore());
			
			if(descending) {
				return -comp;
			} else {
				return comp;
			}
		}
		
	}
	
	public static class ByIdentifiersComparator<RecordType extends Matchable, CausalType extends Matchable> implements Comparator<Correspondence<RecordType, CausalType>> {

		private boolean descending = false;
		
		public ByIdentifiersComparator() {
			this.descending = false;
		}
		
		public ByIdentifiersComparator(boolean descending) {
			this.descending = descending;
		}
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Correspondence<RecordType, CausalType> o1, Correspondence<RecordType, CausalType> o2) {
			int comp = o1.getIdentifiers().compareTo(o2.getIdentifiers());
			
			if(descending) {
				return -comp;
			} else {
				return comp;
			}
		}
		
	}
	
	private static final long serialVersionUID = 1L;
//	private RecordType firstRecord;
//	private RecordType secondRecord;
//	private double similarityScore;
	private Processable<SimpleCorrespondence<CausalType>> causalCorrespondences;
//
//	/**
//	 * returns the first record
//	 * 
//	 * @return
//	 */
//	public RecordType getFirstRecord() {
//		return firstRecord;
//	}
//
//	/**
//	 * sets the first record
//	 * 
//	 * @param firstRecord
//	 */
//	public void setFirstRecord(RecordType firstRecord) {
//		this.firstRecord = firstRecord;
//	}
//
//	/**
//	 * returns the second record
//	 * 
//	 * @return
//	 */
//	public RecordType getSecondRecord() {
//		return secondRecord;
//	}
//
//	/**
//	 * sets the second record
//	 * 
//	 * @param secondRecord
//	 */
//	public void setSecondRecord(RecordType secondRecord) {
//		this.secondRecord = secondRecord;
//	}
//
//	/**
//	 * returns the similarity score
//	 * 
//	 * @return
//	 */
//	public double getSimilarityScore() {
//		return similarityScore;
//	}
//
//	/**
//	 * sets the similarity score
//	 * 
//	 * @param similarityScore
//	 */
//	public void setsimilarityScore(double similarityScore) {
//		this.similarityScore = similarityScore;
//	}

	public Correspondence() {
		
	}
	
	public Correspondence(
			RecordType first, 
			RecordType second,
			double similarityScore, 
			Processable<SimpleCorrespondence<CausalType>> correspondences) {
		super(first, second, similarityScore);
//		firstRecord = first;
//		secondRecord = second;
//		this.similarityScore = similarityScore;
		this.causalCorrespondences = correspondences;
	}

	/**
	 * @return the schema correspondences that were used to calculate this correspondence
	 */
	public Processable<SimpleCorrespondence<CausalType>> getCausalCorrespondences() {
		return causalCorrespondences;
	}
	
	/**
	 * 
	 * performs an *unchecked* cast of the causal correspondences and returns them.
	 * 
	 * @return A {@link Processable} with changed type 
	 */
	public <T extends Matchable> Processable<Correspondence<CausalType, T>> castCausalCorrespondences() {
		Processable<Correspondence<CausalType, T>> result = new ProcessableCollection<>();
		
		for(SimpleCorrespondence<CausalType> cor : getCausalCorrespondences().get()) {
			
			@SuppressWarnings("unchecked")
			Correspondence<CausalType, T> cast = (Correspondence<CausalType, T>) cor;
			
			result.add(cast);
		}
		
		return result;
	}
	
	/**
	 * @param causalCorrespondences the causalCorrespondences to set
	 */
	public void setCausalCorrespondences(
			Processable<SimpleCorrespondence<CausalType>> causalCorrespondences) {
		this.causalCorrespondences = causalCorrespondences;
	}
	
	/**
	 * Combines two correspondences which have the same target schema to a correspondence between the two source schemas, i.e. a-&gt;c; b-&gt;c will be combined to a-&gt;b
	 * @param first
	 * @param second
	 * @return The combined correspondence
	 */
	public static <RecordType extends Matchable, SchemaElementType extends Matchable> Correspondence<RecordType, SchemaElementType> combine(Correspondence<RecordType, SchemaElementType> first, Correspondence<RecordType, SchemaElementType> second) {
		Processable<SimpleCorrespondence<SchemaElementType>> cors = new ProcessableCollection<>(first.getCausalCorrespondences()).append(second.getCausalCorrespondences());
		return new Correspondence<RecordType, SchemaElementType>(first.getFirstRecord(), second.getFirstRecord(), first.getSimilarityScore() * second.getSimilarityScore(), cors);
	}
	
	/**
	 * Inverts the direction of the correspondences in the given ResultSet
	 * @param correspondences
	 */
	public static <RecordType extends Matchable, CausalType extends Matchable> void changeDirection(Processable<Correspondence<RecordType, CausalType>> correspondences) {
		if(correspondences==null) {
			return;
		} else {
			for(Correspondence<RecordType, CausalType> cor : correspondences.get()) {
				cor.changeDirection();
			}
		}
	}
	
	public void changeDirection() {
		RecordType tmp = getFirstRecord();
		setFirstRecord(getSecondRecord());
		setSecondRecord(tmp);
		
		for(SimpleCorrespondence<CausalType> cor : getCausalCorrespondences().get()) {
			cor.changeDirection();
		}
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
	
	public static class RecordId implements Matchable {

		private String identifier;
		
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.wdi.model.Matchable#getIdentifier()
		 */
		@Override
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * @param identifier the identifier to set
		 */
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		
		/* (non-Javadoc)
		 * @see de.uni_mannheim.informatik.wdi.model.Matchable#getProvenance()
		 */
		@Override
		public String getProvenance() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public RecordId(String identifier) {
			this.identifier = identifier;
		}
	}
	
	public static Processable<Correspondence<RecordId, RecordId>> loadFromCsv(File location) throws IOException {
		CSVReader r = new CSVReader(new FileReader(location));
		
		Processable<Correspondence<RecordId, RecordId>> correspondences = new ProcessableCollection<>();
		
		String[] values = null;
		
		while((values = r.readNext())!=null) {
			if(values.length>=3) {
				String id1 = values[0];
				String id2 = values[1];
				String sim = values[2];
				Double similarityScore = 0.0;
				
				try {
					similarityScore = Double.parseDouble(sim);
				} catch(Exception ex) {
					System.err.println(ex.getMessage());
				}
				
				Correspondence<RecordId, RecordId> cor = new Correspondence<RecordId, RecordId>(new RecordId(id1), new RecordId(id2), similarityScore, null);
				correspondences.add(cor);
			} else {
				System.err.println(String.format("Invalid format: \"%s\"", StringUtils.join(values, "\",\"")));
			}
		}
		
		r.close();
		
		return correspondences;
	}
	
	public 
	static 
	<RecordType extends Matchable, SchemaElementType extends Matchable, CorrespondenceType extends Matchable> 
	Processable<Correspondence<RecordType, CorrespondenceType>> 
	loadFromCsv(File location, DataSet<RecordType, SchemaElementType> leftData, DataSet<RecordType, SchemaElementType> rightData) throws IOException {
		CSVReader r = new CSVReader(new FileReader(location));
		
		Processable<Correspondence<RecordType, CorrespondenceType>> correspondences = new ProcessableCollection<>();
		
		String[] values = null;
		
		while((values = r.readNext())!=null) {
			if(values.length>=3) {
				String id1 = values[0];
				String id2 = values[1];
				String sim = values[2];
				Double similarityScore = 0.0;
				
				try {
					similarityScore = Double.parseDouble(sim);
				} catch(Exception ex) {
					System.err.println(ex.getMessage());
				}
				
				Correspondence<RecordType, CorrespondenceType> cor = new Correspondence<RecordType, CorrespondenceType>(leftData.getRecord(id1), rightData.getRecord(id2), similarityScore, null);
				correspondences.add(cor);
			} else {
				System.err.println(String.format("Invalid format: \"%s\"", StringUtils.join(values, "\",\"")));
			}
		}
		
		r.close();
		
		return correspondences;
	}
	
	public static <RecordType extends Matchable, CorType extends Correspondence<RecordType, ?>> Processable<SimpleCorrespondence<RecordType>> simplify(Processable<CorType> correspondences) {
		if(correspondences==null) {
			return null;
		} else {
			Processable<SimpleCorrespondence<RecordType>> result = new ProcessableCollection<>();
			for(CorType cor : correspondences.get()) {
				result.add(cor);
			} 
			return result;
		}
	}
	

}
