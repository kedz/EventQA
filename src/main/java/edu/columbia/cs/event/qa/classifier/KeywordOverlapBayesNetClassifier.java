package edu.columbia.cs.event.qa.classifier;

import dk.ange.octave.OctaveEngine;
import edu.columbia.cs.event.qa.cotraining.QAPair;
import edu.columbia.cs.nlptk.util.OrderedWordList;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/11/13
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
 */

public class KeywordOverlapBayesNetClassifier implements WekaClassifierInterface {

    private BayesNet bayesNet;

    public String classifierName () {
        return "Bayes Net - Keyword Overlap Features (BASELINE)";
    }

    public String nickname () { return "kwo"; }

    public KeywordOverlapBayesNetClassifier () {}

    public Instances initializeInstances () {

        FastVector wekaAttributes = new FastVector(3);
        wekaAttributes.addElement(new Attribute("uni-overlap"));
        wekaAttributes.addElement(new Attribute("bi-overlap"));

        FastVector classVal = new FastVector();
        classVal.addElement("1");
        classVal.addElement("0");
        Attribute label = new Attribute("label", classVal);
        wekaAttributes.addElement(label);

        Instances data = new Instances("keyword-overlap",wekaAttributes,1000);
        data.setClass(label);

        return data;
    }

    public Classifier buildWekaClassifier (Instances wekaInstances) {
        bayesNet = new BayesNet();
        try {
            bayesNet.buildClassifier(wekaInstances);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        return bayesNet;
    }

    public Instance buildWekaInstance (QAPair pair) {

        ArrayList<String> query = pair.getQueryList();
        ArrayList<String> answer = pair.getAnswerList();

        double[] overlap = { computeUnigramOverlap(query, answer), computeBigramOverlap(query, answer), 0.0};

        FastVector attributes = new FastVector(3);
        attributes.addElement(new Attribute("uni-overlap"));
        attributes.addElement(new Attribute("bi-overlap"));

        FastVector classVal = new FastVector();
        classVal.addElement("1");
        classVal.addElement("0");
        Attribute label = new Attribute("label", classVal);
        attributes.addElement(label);

        Instances testInstances = new Instances("Test",attributes,1);
        testInstances.setClass(label);

        Instance example = new Instance(1, overlap);
        testInstances.add(example);
        example.setDataset(testInstances);

        if (!pair.getLabel().equals("-1")) {
            example.setClassValue(pair.getLabel());
        } else {
            example.setClassMissing();
        }
        return example;
    }

    public double computeUnigramOverlap (ArrayList<String> wordList1, ArrayList<String> wordList2) {
        int count = 0;
        for (String word : wordList1) {
            if (wordList2.contains(word)) { count++; }
        }
        return (double) count;
    }

    public double computeBigramOverlap (ArrayList<String> wordList1, ArrayList<String> wordList2) {
        int count = 0;
        if (wordList2.size() > 0) {
            String previousWord = wordList2.get(0);
            for (int i=1; i<wordList2.size(); i++) {
                if (wordList1.contains(previousWord)) {
                    int indexNextWord = wordList1.indexOf(previousWord)+1;
                    if (indexNextWord < wordList1.size()) {
                        if (wordList2.get(i).equals(wordList1.get(indexNextWord))) {
                            count++;
                        }
                    }
                }
                previousWord = wordList2.get(i);
            }
        }
        return (double) count;
    }

    public String classifyInstance (Instance wekaInstance) {
        String label = null;
        try {
            double labelIndex = bayesNet.classifyInstance(wekaInstance);
            wekaInstance.setClassValue(labelIndex);
            label = wekaInstance.toString(wekaInstance.classIndex());
        } catch(Exception e) {
            System.out.println("Classifier failed: "+e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        return label;
    }

    public Evaluation evaluateClassifier (Instances trainInstances, Instances testInstances) {
        try {
            Evaluation eval = new Evaluation(trainInstances);
            eval.evaluateModel(bayesNet, testInstances);
            return eval;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void shutdown () {}
}