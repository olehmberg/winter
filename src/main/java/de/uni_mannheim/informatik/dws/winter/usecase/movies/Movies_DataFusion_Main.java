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
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import de.uni_mannheim.informatik.dws.winter.model.*;
import org.xml.sax.SAXException;

import de.uni_mannheim.informatik.dws.winter.datafusion.CorrespondenceSet;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionEngine;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionEvaluator;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionStrategy;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.evaluation.ActorsEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.evaluation.DateEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.evaluation.DirectorEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.evaluation.TitleEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers.ActorsFuserUnion;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers.DateFuserVoting;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers.DirectorFuserLongestString;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers.TitleFuserShortestString;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLFormatter;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;

/**
 * Class containing the standard setup to perform a data fusion task, reading
 * input data from the movie usecase.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class Movies_DataFusion_Main {

	public static void main(String[] args) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException,
			TransformerException {

		// Load the Data into FusibleDataSet
		FusibleDataSet<Movie, Attribute> ds1 = new FusibleHashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/academy_awards.xml"), "/movies/movie", ds1);
		ds1.printDataSetDensityReport();

		FusibleDataSet<Movie, Attribute> ds2 = new FusibleHashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/actors.xml"), "/movies/movie", ds2);
		ds2.printDataSetDensityReport();

		FusibleDataSet<Movie, Attribute> ds3 = new FusibleHashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/input/golden_globes.xml"), "/movies/movie", ds3);
		ds3.printDataSetDensityReport();

		// Maintain Provenance
		// Scores (e.g. from rating)
		ds1.setScore(3.0);
		ds2.setScore(1.0);
		ds3.setScore(2.0);

		// Date (e.g. last update)
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		        .appendPattern("yyyy-MM-dd")
		        .parseDefaulting(ChronoField.CLOCK_HOUR_OF_DAY, 0)
		        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		        .toFormatter(Locale.ENGLISH);
		
		ds1.setDate(LocalDateTime.parse("2012-01-01", formatter));
		ds2.setDate(LocalDateTime.parse("2010-01-01", formatter));
		ds3.setDate(LocalDateTime.parse("2008-01-01", formatter));

		// load correspondences
		CorrespondenceSet<Movie, Attribute> correspondences = new CorrespondenceSet<>();
		correspondences.loadCorrespondences(new File("usecase/movie/correspondences/academy_awards_2_actors_correspondences.csv"),ds1, ds2);
		correspondences.loadCorrespondences(new File("usecase/movie/correspondences/actors_2_golden_globes_correspondences.csv"),ds2, ds3);

		// write group size distribution
		correspondences.printGroupSizeDistribution();

		// define the fusion strategy
		DataFusionStrategy<Movie, Attribute> strategy = new DataFusionStrategy<>(new MovieXMLReader());
		// add attribute fusers
		strategy.addAttributeFuser(Movie.TITLE, new TitleFuserShortestString(),new TitleEvaluationRule());
		strategy.addAttributeFuser(Movie.DIRECTOR,new DirectorFuserLongestString(), new DirectorEvaluationRule());
		strategy.addAttributeFuser(Movie.DATE, new DateFuserVoting(),new DateEvaluationRule());
		strategy.addAttributeFuser(Movie.ACTORS,new ActorsFuserUnion(),new ActorsEvaluationRule());
		
		// create the fusion engine
		DataFusionEngine<Movie, Attribute> engine = new DataFusionEngine<>(strategy);

		// run the fusion
		FusibleDataSet<Movie, Attribute> fusedDataSet = engine.run(correspondences, null);

		// write the result
		new MovieXMLFormatter().writeXML(new File("usecase/movie/output/fused.xml"), fusedDataSet);

		// load the gold standard
		DataSet<Movie, Attribute> gs = new FusibleHashedDataSet<>();
		new MovieXMLReader().loadFromXML(new File("usecase/movie/goldstandard/fused.xml"), "/movies/movie", gs);

		// evaluate
		DataFusionEvaluator<Movie, Attribute> evaluator = new DataFusionEvaluator<>(
				strategy, new RecordGroupFactory<Movie, Attribute>());
		evaluator.setVerbose(true);
		double accuracy = evaluator.evaluate(fusedDataSet, gs, null);

		System.out.println(String.format("Accuracy: %.2f", accuracy));

	}

}
