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
package de.uni_mannheim.informatik.dws.winter.matching.blockers;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer.VectorCreationMethod;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchableValue;
import de.uni_mannheim.informatik.dws.winter.model.ParallelHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.blocking.DefaultAttributeValueGenerator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceCosineSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceJaccardSimilarity;
import de.uni_mannheim.informatik.dws.winter.utils.query.Q;
import junit.framework.TestCase;

/**
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class BlockingKeyIndexerTest extends TestCase {

	/**
	 * Test method for {@link de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer#runBlocking(de.uni_mannheim.informatik.dws.winter.model.DataSet, de.uni_mannheim.informatik.dws.winter.model.DataSet, de.uni_mannheim.informatik.dws.winter.processing.Processable)}.
	 */
	public void testRunBlocking() {
		Attribute a1 = new Attribute("a1");
		Record r1 = new Record("r1");
		r1.setValue(a1, "a");
		Record r2 = new Record("r2");
		r2.setValue(a1, "b");
		Record r3 = new Record("r3");
		r3.setValue(a1, "c");
		Record r4 = new Record("r4");
		r4.setValue(a1, "d");
		
		Attribute a2 = new Attribute("a2");
		Record r5 = new Record("r5");
		r5.setValue(a2, "a");
		Record r6 = new Record("r6");
		r6.setValue(a2, "b");
		Record r7 = new Record("r7");
		r7.setValue(a2, "e");
		Record r8 = new Record("r8");
		r8.setValue(a2, "f");
		
		DataSet<Record, Attribute> ds1 = new ParallelHashedDataSet<>();
		ds1.addAttribute(a1);
		ds1.add(r1);
		ds1.add(r2);
		ds1.add(r3);
		ds1.add(r4);
		
		DataSet<Record, Attribute> ds2 = new ParallelHashedDataSet<>();
		ds2.addAttribute(a2);
		ds2.add(r5);
		ds2.add(r6);
		ds2.add(r7);
		ds2.add(r8);
		
		// Test Jaccard with BinaryTermOccurrences
		BlockingKeyIndexer<Record, Attribute, Attribute, MatchableValue> blocker = new BlockingKeyIndexer<>(
				new DefaultAttributeValueGenerator(ds1.getSchema()), 
				new DefaultAttributeValueGenerator(ds2.getSchema()),
				new VectorSpaceJaccardSimilarity(),
				VectorCreationMethod.BinaryTermOccurrences,
				0.0);
		
		Processable<Correspondence<Attribute, MatchableValue>> correspondences = blocker.runBlocking(ds1, ds2, null);
		Correspondence<Attribute, MatchableValue> cor = Q.firstOrDefault(correspondences.get());
		assertNotNull(cor);
		assertEquals(33, (int)(cor.getSimilarityScore()*100));

		// Test Cosine with BinaryTermOccurrences
		blocker = new BlockingKeyIndexer<>(
				new DefaultAttributeValueGenerator(ds1.getSchema()), 
				new DefaultAttributeValueGenerator(ds2.getSchema()),
				new VectorSpaceCosineSimilarity(),
				VectorCreationMethod.BinaryTermOccurrences,
				0.0);
		
		correspondences = blocker.runBlocking(ds1, ds2, null);
		cor = Q.firstOrDefault(correspondences.get());
		assertNotNull(cor);
		assertEquals(50, (int)(cor.getSimilarityScore()*100));
		
		// Test Jaccard with TermFrequencies
		blocker = new BlockingKeyIndexer<>(
				new DefaultAttributeValueGenerator(ds1.getSchema()), 
				new DefaultAttributeValueGenerator(ds2.getSchema()),
				new VectorSpaceJaccardSimilarity(),
				VectorCreationMethod.TermFrequencies,
				0.0);
		
		correspondences = blocker.runBlocking(ds1, ds2, null);
		cor = Q.firstOrDefault(correspondences.get());
		assertNotNull(cor);
		assertEquals(33, (int)(cor.getSimilarityScore()*100));

		// Test Cosine with TermFrequencies
		blocker = new BlockingKeyIndexer<>(
				new DefaultAttributeValueGenerator(ds1.getSchema()), 
				new DefaultAttributeValueGenerator(ds2.getSchema()),
				new VectorSpaceCosineSimilarity(),
				VectorCreationMethod.TermFrequencies,
				0.0);
		
		correspondences = blocker.runBlocking(ds1, ds2, null);
		cor = Q.firstOrDefault(correspondences.get());
		assertNotNull(cor);
		assertEquals(50, (int)(cor.getSimilarityScore()*100));
		
		// Test Jaccard with TFIDF
		blocker = new BlockingKeyIndexer<>(
				new DefaultAttributeValueGenerator(ds1.getSchema()), 
				new DefaultAttributeValueGenerator(ds2.getSchema()),
				new VectorSpaceJaccardSimilarity(),
				VectorCreationMethod.TFIDF,
				0.0);
		
		correspondences = blocker.runBlocking(ds1, ds2, null);
		cor = Q.firstOrDefault(correspondences.get());
		assertNotNull(cor);
		assertEquals(0, (int)(cor.getSimilarityScore()*100));

		// Test Cosine with TFIDF
		blocker = new BlockingKeyIndexer<>(
				new DefaultAttributeValueGenerator(ds1.getSchema()), 
				new DefaultAttributeValueGenerator(ds2.getSchema()),
				new VectorSpaceCosineSimilarity(),
				VectorCreationMethod.TFIDF,
				0.0);
		
		correspondences = blocker.runBlocking(ds1, ds2, null);
		cor = Q.firstOrDefault(correspondences.get());
		assertNotNull(cor);
		assertEquals(0, (int)(cor.getSimilarityScore()*100));
	}

}
