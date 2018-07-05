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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.StaticBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDateComparator10Years;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

/**
 * Class containing the standard setup to perform a duplicate detection task,
 * reading input data from the movie usecase.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * @author Robert Meusel (robert@dwslab.de)
 * 
 */
public class Movies_DuplicateDetection_Main {
	
	/*
	 * Trace Options:
	 * 		default: 	level INFO	- console
	 * 		trace:		level TRACE - console
	 * 		infoFile:	level INFO	- console/file
	 * 		traceFile:	level TRACE	- console/file
	 * 		
	 */
	private static final Logger logger = WinterLogManager.getLogger();
	//private static final Logger logger = WinterLogManager.getLogger("traceFile");
	
	public static void main(String[] args) throws Exception {

		// define the matching rule
		LinearCombinationMatchingRule<Movie, Attribute> rule = new LinearCombinationMatchingRule<>(
				0, 1.0);
		rule.addComparator(new MovieTitleComparatorLevenshtein(), 1);
		rule.addComparator(new MovieDateComparator10Years(), 0.822);

		// create the matching engine
		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<Movie, Attribute>(
				new StaticBlockingKeyGenerator<Movie, Attribute>());
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();

		// load the data sets
		HashedDataSet<Movie, Attribute> ds1 = new HashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/actors.xml"), "/movies/movie", ds1);

		// run the matching
		Processable<Correspondence<Movie, Attribute>> correspondences = engine
				.runDuplicateDetection(ds1, rule, blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("usecase/movie/output/actors_duplicates.csv"), correspondences);

		printCorrespondences(new ArrayList<>(correspondences.get()));
	}

	private static void printCorrespondences(
			List<Correspondence<Movie, Attribute>> correspondences) {
		// sort the correspondences
		Collections.sort(correspondences,
				new Comparator<Correspondence<Movie, Attribute>>() {

					@Override
					public int compare(Correspondence<Movie, Attribute> o1,
							Correspondence<Movie, Attribute> o2) {
						int score = Double.compare(o1.getSimilarityScore(),
								o2.getSimilarityScore());
						int title = o1.getFirstRecord().getTitle()
								.compareTo(o2.getFirstRecord().getTitle());

						if (score != 0) {
							return -score;
						} else {
							return title;
						}
					}

				});

		// print the correspondences
		for (Correspondence<Movie, Attribute> correspondence : correspondences) {
			logger.info(String
					.format("%s,%s |\t\t%.2f\t[%s] %s (%s) <--> [%s] %s (%s)",
							correspondence.getFirstRecord().getIdentifier(),
							correspondence.getSecondRecord().getIdentifier(),
							correspondence.getSimilarityScore(),
							correspondence.getFirstRecord().getIdentifier(),
							correspondence.getFirstRecord().getTitle(),
							correspondence.getFirstRecord().getDate()
									.toLocalDate().toString(), correspondence
									.getSecondRecord().getIdentifier(),
							correspondence.getSecondRecord().getTitle(),
							correspondence.getSecondRecord().getDate()
									.toLocalDate().toString()));
			// }
		}
	}

}
