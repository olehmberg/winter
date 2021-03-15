package de.uni_mannheim.informatik.dws.winter.datafusion.debug_report;

import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeFusionLogger;
import de.uni_mannheim.informatik.dws.winter.datafusion.CorrespondenceSet;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionEngine;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionStrategy;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.evaluation.ActorsEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.evaluation.DateEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.evaluation.DirectorEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.evaluation.TitleEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers.ActorsFuserUnion;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers.DateFuserVoting;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers.DirectorFuserLongestString;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.datafusion.fusers.TitleFuserShortestString;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.Movie;
import de.uni_mannheim.informatik.dws.winter.usecase.movies.model.MovieXMLReader;
import junit.framework.TestCase;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class DebugReportTest extends TestCase {

    @Test
    public void testDebugReport() throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        // Test Debug report Data Fusion

        // Setup

        // Load the Data into FusibleDataSet
        FusibleDataSet<Movie, Attribute> ds1 = new FusibleHashedDataSet<>();
        new MovieXMLReader().loadFromXML(new File("testdata/movie/input/academy_awards.xml"), "/movies/movie", ds1);
        ds1.printDataSetDensityReport();

        FusibleDataSet<Movie, Attribute> ds2 = new FusibleHashedDataSet<>();
        new MovieXMLReader().loadFromXML(new File("testdata/movie/input/actors.xml"), "/movies/movie", ds2);
        ds2.printDataSetDensityReport();

        FusibleDataSet<Movie, Attribute> ds3 = new FusibleHashedDataSet<>();
        new MovieXMLReader().loadFromXML(new File("testdata/movie/input/golden_globes.xml"), "/movies/movie", ds3);
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
        correspondences.loadCorrespondences(new File("testdata/movie/correspondences/academy_awards_2_actors_correspondences.csv"),ds1, ds2);
        correspondences.loadCorrespondences(new File("testdata/movie/correspondences/actors_2_golden_globes_correspondences.csv"),ds2, ds3);

        // load the gold standard
        DataSet<Movie, Attribute> gs = new FusibleHashedDataSet<>();
        new MovieXMLReader().loadFromXML(new File("testdata/movie/goldstandard/fused.xml"), "/movies/movie", gs);

        // define the fusion strategy
        DataFusionStrategy<Movie, Attribute> strategy = new DataFusionStrategy<>(new MovieXMLReader());

        // collect debug results
        strategy.activateDebugReport("testdata/movie/output/debugResultsDatafusion.csv", 100, gs);

        // add attribute fusers
        strategy.addAttributeFuser(Movie.TITLE, new TitleFuserShortestString(),new TitleEvaluationRule());
        strategy.addAttributeFuser(Movie.DIRECTOR,new DirectorFuserLongestString(), new DirectorEvaluationRule());
        strategy.addAttributeFuser(Movie.DATE, new DateFuserVoting(),new DateEvaluationRule());
        strategy.addAttributeFuser(Movie.ACTORS,new ActorsFuserUnion(),new ActorsEvaluationRule());

        // create fusion engine
        DataFusionEngine<Movie, Attribute> engine = new DataFusionEngine<>(strategy);

        // run fusion
        FusibleDataSet<Movie, Attribute> fusedDataSet = engine.run(correspondences, null);

        //Test content of debug report
        HashedDataSet<Record, Attribute> fusionDebugReport = strategy.getDebugFusionResults();

        // Tests
        assertNotNull(fusionDebugReport);
        assertEquals(16, fusionDebugReport.size());

        AttributeFusionLogger record1 = (AttributeFusionLogger) fusionDebugReport.getRecord("Director-{academy_awards_1880+golden_globes_1733}");
        assertNotNull(record1);
        assertEquals("Director", record1.getAttributeName());
        assertEquals("{Milos Forman|Milo Forman}", record1.getValues());
        assertEquals("Milos Forman", record1.getFusedValue());
        assertEquals("true", record1.getIsCorrect());
        assertEquals("Milos Forman", record1.getCorrectValue());


    }
}
