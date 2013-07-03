package edu.columbia.cs.event.qa.task;

import edu.columbia.cs.event.qa.util.FileLoader;
import edu.columbia.cs.event.qa.util.SimplePreprocessor;
import org.jblas.DoubleMatrix;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 6/27/13
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */

public class SSProjection {

    private ArrayList<String> terms;
    private DoubleMatrix semanticSpace;

    private static SSProjection SSProjection;

    public static SSProjection newInstance() {
        if(SSProjection == null)
            SSProjection = new SSProjection();
        return SSProjection;
    }

    public SSProjection() {
        try {
            terms = FileLoader.newInstance().loadTerms();
            semanticSpace = FileLoader.newInstance().loadSemanticSpace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public double[] transform (ArrayList<String> tokenList) {
        return project(vectorize(tokenList));
    }

    public double[] transform (String line) {
        return project(vectorize(preprocess(line)));
    }

    public ArrayList<String> preprocess (String line) {
        return SimplePreprocessor.newInstance().forge(line);
    }

    public double[] project (double[] termsVector) {
        return semanticSpace.mmul(new DoubleMatrix(termsVector)).toArray();
    }

    public double[] vectorize (ArrayList<String> tokenList) {
        double[] termsVector = new double[terms.size()];
        Map<String,Integer> wordCount = computeWordCount(tokenList);
        for (int i=0; i<terms.size(); i++) {
            if (wordCount.containsKey(terms.get(i))) {
                termsVector[i] = (double) wordCount.get(terms.get(i));
            }
        }
        return termsVector;
    }

    public Map<String,Integer> computeWordCount (ArrayList<String> tokenList) {
        Map<String,Integer> wordCount = new HashMap<String, Integer>();
        for (String token : tokenList) {
            int count = 0;
            if (wordCount.containsKey(token)) {
                count = wordCount.get(token);
            }
            wordCount.put(token, count+1);
        }
        return wordCount;
    }

    public double computeCosignSimilarity (double[] x, double[] y) {
        double dotProduct = 0.0, xMagnitude = 0.0, yMagnitude = 0.0;
        for (int i=0; i<x.length; i++) {
            dotProduct += x[i]*y[i];
            xMagnitude += x[i]*x[i];
            yMagnitude += y[i]*y[i];
        }
        return dotProduct / (Math.sqrt(xMagnitude)*Math.sqrt(yMagnitude));
    }
}
