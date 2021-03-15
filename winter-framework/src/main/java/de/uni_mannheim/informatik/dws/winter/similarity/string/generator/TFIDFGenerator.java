package de.uni_mannheim.informatik.dws.winter.similarity.string.generator;

import de.uni_mannheim.informatik.dws.winter.matching.blockers.BlockingKeyIndexer;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.similarity.vectorspace.VectorSpaceCosineSimilarity;

import java.util.HashMap;

public class TFIDFGenerator<RecordType extends Matchable, SchemaElementType extends Matchable, CorrespondenceType extends Matchable> {

    private TokenGenerator<RecordType, CorrespondenceType> tokenizer;
    private HashMap<String, Double> inverseDocumentFrequencies = new HashMap<>();


    public TFIDFGenerator(TokenGenerator<RecordType, CorrespondenceType> tokenizer){
        this.tokenizer = tokenizer;

    }

    public HashMap<String, Double> getInverseDocumentFrequencies() {
        return this.inverseDocumentFrequencies;
    }

    public void initializeIDFScores(DataSet<RecordType, SchemaElementType> datasetLeft,
                           DataSet<RecordType, SchemaElementType> datasetRight){

        HashedDataSet<RecordType, SchemaElementType> dataSet = new HashedDataSet<>();
        datasetRight.getSchema().foreach(attribute -> dataSet.addAttribute(attribute));
        datasetRight.foreach(record -> dataSet.add(record));
        datasetLeft.foreach(record -> dataSet.add(record));

        BlockingKeyIndexer<RecordType, SchemaElementType, RecordType, CorrespondenceType> blockingKeyIndexer = new BlockingKeyIndexer<>(
                this.tokenizer,
                this.tokenizer,
                new VectorSpaceCosineSimilarity(),
                BlockingKeyIndexer.VectorCreationMethod.TFIDF, 0.0);

        Processable<Pair<String, Double>> inverseDocumentFrequencies =
                blockingKeyIndexer.calculateInverseDocumentFrequencies(dataSet, this.tokenizer);

        inverseDocumentFrequencies.foreach((c) -> this.inverseDocumentFrequencies.put(c.getFirst(), c.getSecond()));
    }

    public HashMap<String, Double> calculateTermFrequencies(String[] tokens){
        HashMap<String, Double> termFrequencies = new HashMap<>();
        for (String token: tokens){
            double count = 0;
            if(termFrequencies.containsKey(token)){
                count = termFrequencies.get(token);
            }
            count++;
            termFrequencies.put(token, count);
        }

        return termFrequencies;
    }

    public HashMap<String, Double> calculateTFIDFScores(HashMap<String, Double> termFrequencies){
        HashMap<String, Double> tfIDF = new HashMap<>();
        for (String token: termFrequencies.keySet()){
            double count = 0;
            double idfCount = 0;

            if(this.inverseDocumentFrequencies.containsKey(token)){
                count = termFrequencies.get(token);
                idfCount = this.inverseDocumentFrequencies.get(token);
            }

            double tfIdfValue = count * idfCount;
            tfIDF.put(token, tfIdfValue);

        }
        return tfIDF;
    }
}
