package de.uni_mannheim.informatik.dws.winter.usecase.events.utils;

import java.util.List;

import org.joda.time.DateTime;

import de.uni_mannheim.informatik.dws.winter.similarity.SimilarityMeasure;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinEditDistance;

/**
 * Created by Daniel Ringler on 21/12/16.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BestListSimilarity {

    private double compareDistanceWithThreshold(double lowestDistance, double threshold) {
        if (lowestDistance <= threshold) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    private double getLowestDistance(double editDistance, double lowestDistance) {
        if (lowestDistance == -1.0)
            return editDistance;
        if (editDistance < lowestDistance)
            return editDistance;
        return lowestDistance;
    }

    /**
     * Compare all strings and return best sim score
     */
	public double getBestStringSimilarity(SimilarityMeasure sim, List<String> strings1, List<String> strings2) {
        double bestSimilarity = 0.0;
        for (String s1 : strings1) {
            for (String s2 : strings2) {
                double similarity = sim.calculate(s1, s2);
                bestSimilarity = getHighestSimilarity(similarity, bestSimilarity);
            }
        }
        return bestSimilarity;
    }

    /**
     * Stripe prefix for URIs and return best sim score
     */
    public double getBestStripedStringSimilarity(SimilarityMeasure sim, List<String> strings1, List<String> strings2) {
        double bestSimilarity = 0.0;
        for (String s1 : strings1) {
            s1 = stripURIPrefix(s1);
            for (String s2 : strings2) {
                s2 = stripURIPrefix(s2);
                double similarity = sim.calculate(s1, s2);
                bestSimilarity = getHighestSimilarity(similarity, bestSimilarity);
            }
        }
        return bestSimilarity;
    }

    private String stripURIPrefix(String s) {
        if (s.contains("resource")) {
            s = s.substring(s.indexOf("resource")+9, s.length());
        }
        return s;
    }

    public double getBestDatesSimilarity(SimilarityMeasure sim, List<DateTime> dates1, List<DateTime> dates2) {
        double bestSimilarity = 0.0;
        if (dates1==null || dates2==null) {
            return -1.0;
        }
        for (DateTime d1 : dates1) {
            for (DateTime d2 : dates2) {
                double similarity = sim.calculate(d1.toString(), d2.toString());
                bestSimilarity = getHighestSimilarity(similarity, bestSimilarity);
            }
        }
        return bestSimilarity;
    }
    public double getBestDatesEditDistance(SimilarityMeasure sim, List<DateTime> dates1, List<DateTime> dates2, double threshold) {
        double lowestDistance = -1.0;
        if (dates1.size()==0 || dates2.size()==0) {
            return lowestDistance;
        }
        for (DateTime d1 : dates1) {
            for (DateTime d2 : dates2) {
                double editDistance = sim.calculate(d1.toString(), d2.toString());
                lowestDistance = getLowestDistance(editDistance, lowestDistance);
            }
        }
        return compareDistanceWithThreshold(lowestDistance, threshold);
    }


    private double getHighestSimilarity(double similarity, double bestSimilarity) {
        if (similarity >= bestSimilarity)
            return similarity;
        return bestSimilarity;
    }

    /**
     * Get similarity score for a edit distance measure on striped and lowercase attributes
     * @param sim
     * @param strings1
     * @param strings2
     * @param threshold
     * @return
     */
    public double getBestEditDistanceStripedLowercase(LevenshteinEditDistance sim, List<String> strings1, List<String> strings2, double threshold) {
        double lowestDistance = -1.0;
        for (String s1 : strings1) {
            s1 = stripURIPrefix(s1).toLowerCase();
            for (String s2 : strings2) {
                s2 = stripURIPrefix(s2).toLowerCase();
                double editDistance = sim.calculate(s1, s2);
                lowestDistance = getLowestDistance(editDistance, lowestDistance);
            }
        }
        return compareDistanceWithThreshold(lowestDistance, threshold);

    }

    /**
     * Get similarity score for a edit distance measure
     * @param sim
     * @param strings1
     * @param strings2
     * @param threshold
     * @return
     */
    public double getBestEditDistance(LevenshteinEditDistance sim, List<String> strings1, List<String> strings2, double threshold) {
        double lowestDistance = -1.0;
        for (String s1 : strings1) {
            for (String s2 : strings2) {
                double editDistance = sim.calculate(s1, s2);
                lowestDistance = getLowestDistance(editDistance, lowestDistance);
            }
        }
        return compareDistanceWithThreshold(lowestDistance, threshold);

    }


}
