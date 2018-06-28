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

package de.uni_mannheim.informatik.dws.winter.matching;

import java.io.File;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.Blocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.StaticBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.processing.SysOutDatasetIterator;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDateComparator10Years;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieDirectorComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.identityresolution.MovieTitleComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;
import junit.framework.TestCase;

public class MatchingEngineTest extends TestCase {

	public void testRunMatching() throws Exception {
		HashedDataSet<Movie, Attribute> ds = new HashedDataSet<>();
		File sourceFile1 = new File("usecase/movie/input/actors.xml");
		new MovieXMLReader().loadFromXML(sourceFile1, "/movies/movie", ds);

		HashedDataSet<Movie, Attribute> ds2 = new HashedDataSet<>();
		File sourceFile2 = new File("usecase/movie/input/academy_awards.xml");
		new MovieXMLReader().loadFromXML(sourceFile2, "/movies/movie", ds2);

		LinearCombinationMatchingRule<Movie, Attribute> rule = new LinearCombinationMatchingRule<>(
				0, 0);
		rule.addComparator(new MovieTitleComparatorLevenshtein(), 0.5);
		rule.addComparator(new MovieDirectorComparatorLevenshtein(), 0.25);
		rule.addComparator(new MovieDateComparator10Years(), 0.25);

		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<>(
				new StaticBlockingKeyGenerator<Movie, Attribute>());
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();

		engine.runIdentityResolution(ds, ds2, null, rule, blocker);
	}

	public void testRunDeduplication() throws Exception {
		HashedDataSet<Movie, Attribute> ds = new HashedDataSet<>();
		File sourceFile1 = new File("usecase/movie/input/actors.xml");
		new MovieXMLReader().loadFromXML(sourceFile1, "/movies/movie", ds);

		LinearCombinationMatchingRule<Movie, Attribute> rule = new LinearCombinationMatchingRule<>(
				0, 0);
		rule.addComparator(new MovieTitleComparatorLevenshtein(), 0.5);
		rule.addComparator(new MovieDirectorComparatorLevenshtein(), 0.25);
		rule.addComparator(new MovieDateComparator10Years(), 0.25);

		StandardRecordBlocker<Movie, Attribute> blocker = new StandardRecordBlocker<>(
				new StaticBlockingKeyGenerator<Movie, Attribute>());
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();

		engine.runDuplicateDetection(ds, rule, blocker);
	}

//	public void testGenerateFeaturesForOptimisation()
//			throws Exception {
//		HashedDataSet<Movie, Attribute> ds = new HashedDataSet<>();
//		File sourceFile1 = new File("usecase/movie/input/actors.xml");
//		new MovieXMLReader().loadFromXML(sourceFile1, "/movies/movie", ds);
//
//		HashedDataSet<Movie, Attribute> ds2 = new HashedDataSet<>();
//		File sourceFile2 = new File("usecase/movie/input/academy_awards.xml");
//		new MovieXMLReader().loadFromXML(sourceFile2, "/movies/movie", ds2);
//
//		LinearCombinationMatchingRule<Movie, Attribute> rule = new LinearCombinationMatchingRule<>(
//				0, 0);
//		rule.addComparator(new MovieTitleComparatorLevenshtein(), 0.5);
//		rule.addComparator(new MovieDirectorComparatorLevenshtein(), 0.25);
//		rule.addComparator(new MovieDateComparator10Years(), 0.25);
//
//		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();
//
//		MatchingGoldStandard gs = new MatchingGoldStandard();
//		gs.loadFromCSVFile(new File(
//				"usecase/movie/goldstandard/gs_academy_awards_2_actors.csv"));
//
//		engine.generateTrainingDataForLearning(ds, ds2, gs, rule, null);
//	}

	public void testRunSchemaMatching() throws Exception {
		HashedDataSet<Movie, Attribute> ds1 = new HashedDataSet<>();
		ds1.addAttribute(new Attribute("att1"));
		ds1.addAttribute(new Attribute("att2"));
		HashedDataSet<Movie, Attribute> ds2 = new HashedDataSet<>();
		ds2.addAttribute(new Attribute("att1"));
		ds2.addAttribute(new Attribute("att2"));
		
		LinearCombinationMatchingRule<Attribute, Movie> rule = new LinearCombinationMatchingRule<>(1.0);
		Comparator<Attribute, Movie> comp = new Comparator<Attribute, Movie>() {
			private static final long serialVersionUID = 1L;

			@Override
			public double compare(
					Attribute record1,
					Attribute record2,
					Correspondence<Movie, Matchable> schemaCorrespondences) {
				return record1.getIdentifier().equals(record2.getIdentifier()) ? 1.0 : 0.0;
			}

			@Override
			public Map<Integer, String> getComparisonResult() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		rule.addComparator(comp, 1.0);
		
		Blocker<Attribute, Attribute, Attribute, Movie> blocker = new Blocker<Attribute, Attribute, Attribute, Movie>()  {
			
			
			/* (non-Javadoc)
			 * @see de.uni_mannheim.informatik.wdi.matching.blocking.SchemaBlocker#runBlocking(de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.DataSet, de.uni_mannheim.informatik.wdi.model.ResultSet, de.uni_mannheim.informatik.wdi.processing.DataProcessingEngine)
			 */
			@Override
			public Processable<Correspondence<Attribute, Movie>> runBlocking(
					DataSet<Attribute, Attribute> schema1,
					DataSet<Attribute, Attribute> schema2,
					Processable<Correspondence<Movie, Matchable>> instanceCorrespondences) {
				Processable<Correspondence<Attribute, Movie>> result = new ProcessableCollection<>();
				
				for(Attribute s1 : schema1.get()) {
					for(Attribute s2 : schema2.get()) {
						result.add(new Correspondence<Attribute, Movie>(s1, s2, 0.0, instanceCorrespondences));
					}
				}
				
				return result;
			}

			@Override
			public double getReductionRatio() {
				return 0;
			}
		};
		
		MatchingEngine<Movie, Attribute> engine = new MatchingEngine<>();
		HashedDataSet<Attribute, Attribute> schema1 = new HashedDataSet<>();
		for(Attribute s : ds1.getSchema().get()) {
			schema1.add(s);
		}
		HashedDataSet<Attribute, Attribute> schema2 = new HashedDataSet<>();
		for(Attribute s : ds2.getSchema().get()) {
			schema2.add(s);
		}
		Processable<Correspondence<Attribute, Movie>> result = engine.runSchemaMatching(schema1, schema2, null, rule, blocker);
		
		for(Correspondence<Attribute, Movie> cor : result.get()) {
			assertEquals(cor.getFirstRecord().getIdentifier(), cor.getSecondRecord().getIdentifier());
		}
	}
	
	public void testGetTopKInstanceCorrespondences() {
		ProcessableCollection<Correspondence<Record, Attribute>> cors = new ProcessableCollection<>();
		Record r1 = new Record("r1");
		Record r2 = new Record("r2");
		Record r3 = new Record("r3");
		Record r4 = new Record("r4");
		
		cors.add(new Correspondence<Record, Attribute>(r1, r2, 1.0, null));
		cors.add(new Correspondence<Record, Attribute>(r1, r3, 0.8, null));
		cors.add(new Correspondence<Record, Attribute>(r1, r4, 0.5, null));
		cors.add(new Correspondence<>(r2, r1, 0.9, null));
		cors.add(new Correspondence<>(r2, r3, 0.4, null));
		cors.add(new Correspondence<>(r2, r4, 0.3, null));
		
		MatchingEngine<Record, Attribute> m = new MatchingEngine<>();
		
		Processable<Correspondence<Record, Attribute>> result; 
		
		assertEquals(6, m.getTopKInstanceCorrespondences(cors, 3, 0.0).size());
		assertEquals(3, m.getTopKInstanceCorrespondences(cors, 3, 0.7).size());
		result = m.getTopKInstanceCorrespondences(cors, 2, 0.0);
		result.foreach(new SysOutDatasetIterator<>());
		assertEquals(4, result.size());
		assertEquals(3, m.getTopKInstanceCorrespondences(cors, 2, 0.5).size());
		assertEquals(2, m.getTopKInstanceCorrespondences(cors, 1, 0.0).size());
		assertEquals(1, m.getTopKInstanceCorrespondences(cors, 1, 1.0).size());
	}
}
