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
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import de.uni_mannheim.informatik.dws.winter.clustering.ConnectedComponentClusterer;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.utils.graph.Graph;

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
public class Correspondence<RecordType extends Matchable, CausalType extends Matchable> implements Serializable  {

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
	private Processable<Correspondence<CausalType, Matchable>> causalCorrespondences;
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
	 * @param firstRecord	the first record
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
	 * @param secondRecord	the second record
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
	 * @param similarityScore	the similarity score
	 */
	public void setsimilarityScore(double similarityScore) {
		this.similarityScore = similarityScore;
	}
	
	public Correspondence() {
		
	}
	
	public Correspondence(
			RecordType first, 
			RecordType second,
			double similarityScore) {
		firstRecord = first;
		secondRecord = second;
		this.similarityScore = similarityScore;
	}

	
	public Correspondence(
			RecordType first, 
			RecordType second,
			double similarityScore, 
			Processable<Correspondence<CausalType, Matchable>> correspondences) {
//		super(first, second, similarityScore);
		firstRecord = first;
		secondRecord = second;
		this.similarityScore = similarityScore;
		this.causalCorrespondences = correspondences;
	}

	public String getIdentifiers() {
		return String.format("%s/%s", getFirstRecord().getIdentifier(), getSecondRecord().getIdentifier());
	}
	
	/**
	 * @return the schema correspondences that were used to calculate this correspondence
	 */
	public Processable<Correspondence<CausalType, Matchable>> getCausalCorrespondences() {
		return causalCorrespondences;
	}
	
	/**
	 * 
	 * performs an *unchecked* cast of the causal correspondences and returns them.
	 * 
	 * 
	 * @return A {@link Processable} with changed type 
	 */
	public <T extends Matchable> Processable<Correspondence<CausalType, T>> castCausalCorrespondences() {
		Processable<Correspondence<CausalType, T>> result = new ProcessableCollection<>();
		
		for(Correspondence<CausalType, Matchable> cor : getCausalCorrespondences().get()) {
			
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
			Processable<Correspondence<CausalType, Matchable>> causalCorrespondences) {
		this.causalCorrespondences = causalCorrespondences;
	}
	
	/**
	 * Combines two correspondences which have the same target schema to a correspondence between the two source schemas, i.e. a-&gt;c; b-&gt;c will be combined to a-&gt;b
	 * @param first	the first correspondence
	 * @param second	the second corespondence
	 * @return The combined correspondence
	 */
	public static <RecordType extends Matchable, SchemaElementType extends Matchable> Correspondence<RecordType, SchemaElementType> combine(Correspondence<RecordType, SchemaElementType> first, Correspondence<RecordType, SchemaElementType> second) {
		Processable<Correspondence<SchemaElementType, Matchable>> cors = new ProcessableCollection<>(first.getCausalCorrespondences()).append(second.getCausalCorrespondences());
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
		
		for(Correspondence<CausalType, Matchable> cor : getCausalCorrespondences().get()) {
			cor.changeDirection();
		}
	}
	
	/**
	 * 
	 * Changes the direction of all correspondences such that the Matchable with the smaller data source identifier is on the left-hand side
	 * 
	 * @param correspondences
	 */
	public static <RecordType extends Matchable, CausalType extends Matchable> void setDirectionByDataSourceIdentifier(Processable<Correspondence<RecordType, CausalType>> correspondences) {
		if(correspondences==null) {
			return;
		} else {
			for(Correspondence<RecordType, CausalType> cor : correspondences.get()) {
				cor.setDirectionByDataSourceIdentifier();
			}
		}
	}
	
	public void setDirectionByDataSourceIdentifier() {
		
		if(getFirstRecord().getDataSourceIdentifier()>getSecondRecord().getDataSourceIdentifier()) {
			RecordType tmp = getFirstRecord();
			setFirstRecord(getSecondRecord());
			setSecondRecord(tmp);
		}
		
		if(getCausalCorrespondences()!=null) {
			for(Correspondence<CausalType, Matchable> cor : getCausalCorrespondences().get()) {
				cor.setDirectionByDataSourceIdentifier();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Correspondence) {
			Correspondence<?,?> cor2 = (Correspondence<?,?>)obj;
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
	
//	public static <RecordType extends Matchable, CorType extends Correspondence<RecordType, ? extends Matchable>> Processable<SimpleCorrespondence<RecordType>> simplify(Processable<CorType> correspondences) {
//		if(correspondences==null) {
//			return null;
//		} else {
//			Processable<SimpleCorrespondence<RecordType>> result = new ProcessableCollection<>();
//			for(CorType cor : correspondences.get()) {
//				
//				//TODO change simplification process, maybe instance method toSimple ... toMatchableCor...
//				
//				result.add(new SimpleCorrespondence<RecordType>(cor.getFirstRecord(), cor.getSecondRecord(), cor.getSimilarityScore()), Correspondence.simplify(cor.getCausalCorrespondences()));
//			} 
//			return result;
//		}
//	}

	public static <RecordType extends Matchable, CorType extends Correspondence<RecordType, ? extends Matchable>> Processable<Correspondence<RecordType, Matchable>> toMatchable(Processable<CorType> correspondences) {
		if(correspondences==null) {
			return null;
		} else {
			Processable<Correspondence<RecordType, Matchable>> result = new ProcessableCollection<>();
			for(CorType cor : correspondences.get()) {
				
				Correspondence<RecordType, Matchable> simple = new Correspondence<RecordType, Matchable>(cor.getFirstRecord(), cor.getSecondRecord(), cor.getSimilarityScore(), toMatchable2(cor.getCausalCorrespondences()));
				result.add(simple);
			} 
			return result;
		}
	}
	
	public static <RecordType extends Matchable, CorType extends Correspondence<RecordType, Matchable>> Processable<Correspondence<Matchable, Matchable>> toMatchable2(Processable<CorType> correspondences) {
		if(correspondences==null) {
			return null;
		} else {
			Processable<Correspondence<Matchable, Matchable>> result = new ProcessableCollection<>();
			for(CorType cor : correspondences.get()) {
				
				Correspondence<Matchable, Matchable> simpler = new Correspondence<Matchable, Matchable>(cor.getFirstRecord(), cor.getSecondRecord(), cor.getSimilarityScore(), cor.getCausalCorrespondences());

				result.add(simpler);
			} 
			return result;
		}
	}
	
	/**
	 * 
	 * creates a new correspondence for each causal correspondence
	 * 
	 * @param correspondences
	 * @param result
	 */
	public static <T extends Matchable, U extends Matchable> void flatten(Processable<Correspondence<T, U>> correspondences, Processable<Correspondence<T, U>> result) {
		for(Correspondence<T, U> cor : correspondences.get()) {
			
			for(Correspondence<U, Matchable> cause : cor.getCausalCorrespondences().get()) {

				Processable<Correspondence<U, Matchable>> newCauses = new ProcessableCollection<>();
				newCauses.add(cause);
				Correspondence<T, U> newCor = new Correspondence<T, U>(cor.getFirstRecord(), cor.getSecondRecord(), cor.getSimilarityScore(), newCauses);
				result.add(newCor);
			}
			
		}
	}
	
	public static <T extends Matchable, CorT extends Correspondence<T, Matchable>> Set<Collection<Integer>> getDataSourceClusters(Processable<CorT> correspondences) {
		ConnectedComponentClusterer<Integer> clusterer = new ConnectedComponentClusterer<>();
		
		for(CorT cor : correspondences.get()) {
			clusterer.addEdge(new Triple<Integer, Integer, Double>(cor.getFirstRecord().getDataSourceIdentifier(), cor.getSecondRecord().getDataSourceIdentifier(), cor.getSimilarityScore()));
		}
		
		Map<Collection<Integer>, Integer> clustering = clusterer.createResult();
		
		return clustering.keySet();
	}
	
	public static <T extends Matchable, U extends Matchable> Graph<T, Correspondence<T, U>> toGraph(Collection<Correspondence<T, U>> correspondences) {
		Graph<T, Correspondence<T, U>> graph = new Graph<>();
		
		for(Correspondence<T, U> cor : correspondences) {
			graph.addEdge(cor.getFirstRecord(), cor.getSecondRecord(), cor, cor.getSimilarityScore());
		}
		
		return graph;
	}
	
	public static <T extends Matchable, U extends Matchable> Set<Collection<T>> getConnectedComponents(Collection<Correspondence<T, U>> correspondences) {
		ConnectedComponentClusterer<T> clusterer = new ConnectedComponentClusterer<>();
		
		for(Correspondence<T, U> cor : correspondences) {
			clusterer.addEdge(new Triple<T, T, Double>(cor.getFirstRecord(), cor.getSecondRecord(), cor.getSimilarityScore()));
		}
		
		return clusterer.createResult().keySet();
	}
	
}
