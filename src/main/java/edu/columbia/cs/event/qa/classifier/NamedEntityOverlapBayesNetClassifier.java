package edu.columbia.cs.event.qa.classifier;

import edu.columbia.cs.event.qa.cotraining.QAPair;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/11/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class NamedEntityOverlapBayesNetClassifier implements WekaClassifierInterface {

    private BayesNet bayesNet;

    public String classifierName () {
        return "Bayes Net - Named Entity Overlap Features (BASELINE)";
    }

    public String nickname () { return "neo"; }

    public NamedEntityOverlapBayesNetClassifier () {}

    public Instances initializeInstances () {

        FastVector wekaAttributes = new FastVector(5);
        wekaAttributes.addElement(new Attribute("person-overlap"));
        wekaAttributes.addElement(new Attribute("location-overlap"));
        wekaAttributes.addElement(new Attribute("organization-overlap"));
        wekaAttributes.addElement(new Attribute("any-overlap"));

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
            System.exit(-1);;
        }
        return bayesNet;
    }

    public Instance buildWekaInstance (QAPair pair) {

        ArrayList<String> query = pair.getQueryList();
        ArrayList<String> answer = pair.getAnswerList();

        double[] overlap = computeOverlap(pair);

        FastVector attributes = new FastVector(5);
        attributes.addElement(new Attribute("person-overlap"));
        attributes.addElement(new Attribute("location-overlap"));
        attributes.addElement(new Attribute("organization-overlap"));
        attributes.addElement(new Attribute("any-overlap"));

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

    public double[] computeOverlap (QAPair pair) {
        Element query = pair.getQuery();
        Element answer = pair.getAnswer();
        HashSet[] queryNEs = identifyNamedEntities(query);
        HashSet[] answerNEs = identifyNamedEntities(answer);
        double persons = findMatches(queryNEs[0], answerNEs[0]);
        double locations = findMatches(queryNEs[1], answerNEs[1]);
        double orgs = findMatches(queryNEs[2], answerNEs[2]);
        double total = Math.min(queryNEs[0].size(), answerNEs[0].size())
                     + Math.min(queryNEs[1].size(), answerNEs[1].size())
                     + Math.min(queryNEs[2].size(), answerNEs[2].size());
        double[] overlap = { persons, locations, orgs, total, 0.0 };
        return overlap;
    }

    public HashSet<String>[] identifyNamedEntities (Element sentence) {
        HashSet<String> per = new HashSet<String>();
        HashSet<String> loc = new HashSet<String>();
        HashSet<String> org = new HashSet<String>();
        NodeList words = sentence.getElementsByTagName("Word");
        for (int i=0; i<words.getLength(); i++) {
            Element word = (Element) words.item(i);
            String lem = word.getElementsByTagName("Lemma").item(0).getFirstChild().getNodeValue();
            String ne = word.getElementsByTagName("Ne").item(0).getFirstChild().getNodeValue();
            if (ne.equals("PERSON")) { per.add(lem); }
            else if (ne.equals("LOCATION")) { loc.add(lem); }
            else if (ne.equals("ORGANIZATION")) { org.add(lem); }
        }
        return new HashSet[] { per, loc, org };
    }

    public double findMatches (HashSet<String> set1, HashSet<String> set2) {
        int count = 0;
        for (String word : set2) {
            if (set1.contains(word)) { count++; }
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
