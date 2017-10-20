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
package de.uni_mannheim.informatik.dws.winter.datafusion;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import de.uni_mannheim.informatik.dws.winter.clustering.ConnectedComponentClusterer;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroupFactory;
import de.uni_mannheim.informatik.dws.winter.model.Triple;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;

/**
 * Represents a set of correspondences (from the identity resolution)
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * @author Robert Meusel (robert@dwslab.de)
 * 
 * @param <RecordType>	the type that represents a record
 */
public class CorrespondenceSet<RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> {

	private Collection<RecordGroup<RecordType, SchemaElementType>> groups;
	private Map<String, RecordGroup<RecordType, SchemaElementType>> recordIndex;

	private RecordGroupFactory<RecordType, SchemaElementType> groupFactory;
	
	public CorrespondenceSet() {
		groups = new LinkedList<>();
		recordIndex = new HashMap<>();
		groupFactory = new RecordGroupFactory<>();
	}
	
	/**
	 * @param groupFactory the groupFactory to set
	 */
	public void setGroupFactory(
			RecordGroupFactory<RecordType, SchemaElementType> groupFactory) {
		this.groupFactory = groupFactory;
	}

	/**
	 * Loads correspondences from a file and adds them to this correspondence
	 * set. Can be called multiple times.
	 * 
	 * @param correspondenceFile	the to load from
	 * @param first					the dataset that contains the records on the left-hand side of the correspondences
	 * @param second				the dataset that contains the records on the right-hand side of the correspondences
	 * @throws IOException			thrown if there is a problem loading the file
	 */
	public void loadCorrespondences(File correspondenceFile,
                                    FusibleDataSet<RecordType, SchemaElementType> first, FusibleDataSet<RecordType, SchemaElementType> second)
			throws IOException {
		CSVReader reader = new CSVReader(new FileReader(correspondenceFile));

		String[] values = null;

		while ((values = reader.readNext()) != null) {
			// check if the ids exist in the provided datasets
			if (first.getRecord(values[0]) == null) {
				System.err.println(String.format(
						"Record %s not found in first dataset", values[0]));
				continue;
			}
			if (second.getRecord(values[1]) == null) {
				System.err.println(String.format(
						"Record %s not found in second dataset", values[0]));
				continue;
			}

			// check if the ids already belong to any groups
			RecordGroup<RecordType, SchemaElementType> grp1 = recordIndex.get(values[0]);
			RecordGroup<RecordType, SchemaElementType> grp2 = recordIndex.get(values[1]);

			if (grp1 == null && grp2 == null) {
				// no existing groups, create a new one
				RecordGroup<RecordType, SchemaElementType> grp = groupFactory.createRecordGroup();
				grp.addRecord(values[0], first);
				grp.addRecord(values[1], second);
				recordIndex.put(values[0], grp);
				recordIndex.put(values[1], grp);
				groups.add(grp);
			} else if (grp1 != null && grp2 == null) {
				// one existing group, add to this group
				grp1.addRecord(values[1], second);
				recordIndex.put(values[1], grp1);
			} else if (grp1 == null && grp2 != null) {
				// one existing group, add to this group
				grp2.addRecord(values[0], first);
				recordIndex.put(values[0], grp2);
			} else {
				// two existing groups, merge
				grp1.mergeWith(grp2);

				for (String id : grp2.getRecordIds()) {
					recordIndex.put(id, grp1);
				}
			}
		}

		reader.close();
	}

	/**
	 * Loads correspondences from a file and adds them to this correspondence
	 * set. Can be called multiple times.
	 * 
	 * @param correspondenceFile	the file to load from
	 * @param first					the dataset that contains the records
	 * @throws IOException			thrown if there is a problem loading the file
	 */
	public void loadCorrespondences(File correspondenceFile,
			FusibleDataSet<RecordType, SchemaElementType> first)
			throws IOException {
		CSVReader reader = new CSVReader(new FileReader(correspondenceFile));

		String[] values = null;
		int skipped = 0;

		while ((values = reader.readNext()) != null) {
			// check if the ids exist in the provided data sets
			if (first.getRecord(values[0]) == null) {
				skipped++;
				continue;
			}
			
			// we only have the records from the source data sets, so we group by the id in the target data set
			RecordGroup<RecordType, SchemaElementType> grp2 = recordIndex.get(values[1]);

			if (grp2 == null) {
				// no existing groups, create a new one
				RecordGroup<RecordType, SchemaElementType> grp = groupFactory.createRecordGroup();
				grp.addRecord(values[0], first);
				recordIndex.put(values[1], grp);
				groups.add(grp);
			} else {
				// one existing group, add to this group
				grp2.addRecord(values[0], first);
				recordIndex.put(values[0], grp2);
			}
		}

		reader.close();
		
		if (skipped>0) {
			System.err.println(String.format("Skipped %,d records (not found in provided dataset)", skipped));
		}
	}
	
	/**
	 * Creates the {@link CorrespondenceSet} from correspondences. Cannot be called multiple times.
	 * @param correspondences	the correspondences to load
	 * @param first					the dataset that contains the records on the left-hand side of the correspondences
	 * @param second				the dataset that contains the records on the right-hand side of the correspondences
	 */
	public void createFromCorrespondences(Processable<Correspondence<RecordType, Matchable>> correspondences,
            FusibleDataSet<RecordType, SchemaElementType> first, FusibleDataSet<RecordType, SchemaElementType> second) {
		
		Map<String, FusibleDataSet<RecordType, SchemaElementType>> idToDataSet = new HashMap<>();
		
		ConnectedComponentClusterer<RecordType> clu = new ConnectedComponentClusterer<>();
		for(Correspondence<RecordType, Matchable> cor : correspondences.get()) {
			clu.addEdge(new Triple<RecordType, RecordType, Double>(cor.getFirstRecord(), cor.getSecondRecord(), cor.getSimilarityScore()));
			idToDataSet.put(cor.getFirstRecord().getIdentifier(), first);
			idToDataSet.put(cor.getSecondRecord().getIdentifier(), second);
		}
		Map<Collection<RecordType>, RecordType> clusters = clu.createResult();
		
		for(Collection<RecordType> cluster : clusters.keySet()) {
			RecordGroup<RecordType, SchemaElementType> grp = groupFactory.createRecordGroup();
			
			for(RecordType r : cluster) {
				grp.addRecord(r.getIdentifier(), idToDataSet.get(r.getIdentifier()));
				recordIndex.put(r.getIdentifier(), grp);
			}
			
			groups.add(grp);
		}
	}
	
	/**
	 * 
	 * 
	 * @return returns the groups of records which are the same according to the correspondences
	 */
	public Collection<RecordGroup<RecordType, SchemaElementType>> getRecordGroups() {
		return groups;
	}

	/**
	 * writes the distribution of the sizes of the groups of records to the
	 * specified file
	 * 
	 * @param outputFile	the file to write to
	 * @throws IOException	Thrown if there is a problem writing to the file
	 */
	public void writeGroupSizeDistribution(File outputFile) throws IOException {
		Map<Integer, Integer> sizeDist = new HashMap<>();

		for (RecordGroup<RecordType, SchemaElementType> grp : groups) {
			int size = grp.getSize();

			Integer count = sizeDist.get(size);

			if (count == null) {
				count = 0;
			}

			sizeDist.put(size, ++count);
		}

		CSVWriter writer = new CSVWriter(new FileWriter(outputFile));

		writer.writeNext(new String[] { "Group Size", "Frequency" });
		for (int size : sizeDist.keySet()) {
			writer.writeNext(new String[] { Integer.toString(size),
					Integer.toString(sizeDist.get(size)) });
		}

		writer.close();
	}

	/**
	 * prints the distribution of the sizes of the groups of records to the
	 * specified file
	 * 
	 */
	public void printGroupSizeDistribution()  {
		Map<Integer, Integer> sizeDist = new HashMap<>();

		for (RecordGroup<RecordType, SchemaElementType> grp : groups) {
			int size = grp.getSize();

			Integer count = sizeDist.get(size);

			if (count == null) {
				count = 0;
			}

			sizeDist.put(size, ++count);
		}
		System.out.println("Group Size Distribtion of " + groups.size() + " groups:");
		System.out.println("	Group Size | Frequency ");
		System.out.println("	———————————————————————");

		for (int size : Q.sort(sizeDist.keySet())) {
			String sizeStr = Integer.toString(size);
			System.out.print("	");
			for (int i = 0; i < 10 - sizeStr.length(); i++) {
				System.out.print(" ");
			}
			System.out.print(sizeStr);
			System.out.print(" |");
			String countStr = Integer.toString(sizeDist.get(size));
			for (int i = 0; i < 10 - countStr.length(); i++) {
				System.out.print(" ");
			}
			System.out.println(countStr);
		}

	}

}
