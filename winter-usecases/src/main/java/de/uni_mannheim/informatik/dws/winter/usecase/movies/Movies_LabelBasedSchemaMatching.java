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
package de.uni_mannheim.informatik.dws.winter.usecase.movies;

import java.io.File;

import org.slf4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.CSVRecordReader;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.LabelComparatorJaccard;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * 
 * Example of a basic setup for feature-based schema matching 
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Movies_LabelBasedSchemaMatching {
	
	/*
	 * Logging Options:
	 * 		default: 	level INFO	- console
	 * 		trace:		level TRACE     - console
	 * 		infoFile:	level INFO	- console/file
	 * 		traceFile:	level TRACE	- console/file
	 *  
	 * To set the log level to trace and write the log to winter.log and console, 
	 * activate the "traceFile" logger as follows:
	 *     private static final Logger logger = WinterLogManager.activateLogger("traceFile");
	 *
	 */

	private static final Logger logger = WinterLogManager.activateLogger("default");
	
	public static void main(String[] args) throws Exception {
		
		// load data
		DataSet<Record, Attribute> data1 = new HashedDataSet<>();
		new CSVRecordReader(0).loadFromCSV(new File("usecase/movie/input/scifi1.csv"), data1);
		DataSet<Record, Attribute> data2 = new HashedDataSet<>();
		new CSVRecordReader(0).loadFromCSV(new File("usecase/movie/input/scifi2.csv"), data2);
		
		// Initialize Matching Engine
		MatchingEngine<Record, Attribute> engine = new MatchingEngine<>();

		Processable<Correspondence<Attribute, Attribute>> correspondences = engine.runLabelBasedSchemaMatching(data1.getSchema(), data2.getSchema(), new LabelComparatorJaccard(), 0.5);
		
		// print results
		for(Correspondence<Attribute, Attribute> cor : correspondences.get()) {
			logger.info(String.format("[%s]'%s' <-> [%s]'%s' (%.4f)",
					cor.getFirstRecord().getIdentifier(),
					cor.getFirstRecord().getName(), 
					cor.getSecondRecord().getIdentifier(),
					cor.getSecondRecord().getName(), 
					cor.getSimilarityScore()));
		}
	}
	
}
