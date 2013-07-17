package edu.columbia.cs.event.qa.classifier;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveDouble;
import edu.columbia.cs.event.qa.cotraining.QAPair;
import edu.columbia.cs.nlptk.util.OrderedWordList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 7/3/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class CcaKwEnrichedNeBayesNetClassifier implements WekaClassifierInterface {

    private OctaveEngine octave;
    private int eigenFeatureSize;
    private Instances dataSet;
    private BayesNet bayesNet;
    private OrderedWordList keywordIndex;

    public String classifierName () {
        return "Bayes Net - CCA - Keyword Features Enriched with Named Entity Features (k="+eigenFeatureSize+")";
    }

    public String nickname () { return "kwene"; }

    public CcaKwEnrichedNeBayesNetClassifier(File projectionFile, File keywordIndexFile) {

        System.out.println("Loading keyword index: "+keywordIndexFile);
        this.keywordIndex = new OrderedWordList(keywordIndexFile);

        System.out.println("Starting Octave environment.");
        OctaveEngineFactory factory = new OctaveEngineFactory();
        octave = factory.getScriptEngine();

        System.out.println("Loading projection matrix from: "+projectionFile);
        octave.eval("A = csvread('"+projectionFile+"');");
        octave.eval("numFeatures = size(A,2)");
        OctaveDouble numFeatures = octave.get(OctaveDouble.class, "numFeatures");
        eigenFeatureSize = (int) numFeatures.get(1,1);


    }


    public Instances initializeInstances () {

        FastVector wekaAttributes = new FastVector(eigenFeatureSize+1);
        for(int i = 0; i < eigenFeatureSize; i++) {
            wekaAttributes.addElement(new Attribute(Integer.toString(i)));
        }

        FastVector classVal = new FastVector();
        classVal.addElement("1");
        classVal.addElement("0");
        Attribute label = new Attribute("label",classVal);
        wekaAttributes.addElement(label);

        dataSet = new Instances("keyword-enriched-w-ne",wekaAttributes,1000);
        dataSet.setClass(label);

        return dataSet;

    }

    public Classifier buildWekaClassifier (Instances wekaInstances) {

        bayesNet = new BayesNet();
        try {
            bayesNet.buildClassifier(wekaInstances);

        } catch (Exception e) {
            System.out.println("Weka BayesNet classifier threw exception: "+e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        return bayesNet;

    }


    public Instance buildWekaInstance (QAPair pair) {

        Set<String> questionKeywords = extractKeywordFeatures(pair.getQuery());
        Set<String> answerKeywords = extractKeywordFeatures(pair.getAnswer());
        Set<String> activeFeatures = new TreeSet<String>();

        for(String keyword : questionKeywords)
            if (answerKeywords.contains(keyword))
                activeFeatures.add(keyword);


        octave.eval("x = sparse("+keywordIndex.size()+",1);");
        for(String keyword : activeFeatures) {

            int index = keywordIndex.getWordList().indexOf(keyword);
            if (index > -1) {

                index = index + 1;
                octave.eval("x("+index+",1) = 1;");

            }



        }

        octave.eval("xProj = A'*x;");
        OctaveDouble eigenFeatures = octave.get(OctaveDouble.class,"xProj");

        FastVector attributes = new FastVector();
        double[] featureValues = new double[eigenFeatureSize+1];
        for(int i = 0; i < eigenFeatureSize;i++) {
            int octIndex = i+1;
            featureValues[i] = eigenFeatures.get(octIndex,1);
            Attribute attribute = new Attribute(Integer.toString(i));
            attributes.addElement(attribute);
        }

        FastVector classVal = new FastVector();
        classVal.addElement("1");
        classVal.addElement("0");
        Attribute labelAttribute = new Attribute("label",classVal);
        attributes.addElement(labelAttribute);

        Instances testInstances = new Instances("test",attributes,1);
        testInstances.setClass(labelAttribute);

        Instance instance = new Instance(1,featureValues);
        testInstances.add(instance);
        instance.setDataset(testInstances);



        //instance..setClassIndex(eigenFeatureSize);
        //instance.setDataset(dataSet);
        //instance.setClassMissing();

        if (!pair.getLabel().equals("-1")) {
            instance.setClassValue(pair.getLabel());
        } else {
            instance.setClassMissing();
        }
        return instance;
    }


    public void shutdown() {
        System.out.println("Closing octave engine.");
        octave.close();
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




    private Set<String> extractKeywordFeatures(Element root) {

        TreeSet<String> keywords = new TreeSet<String>();

        ArrayList<String> wordList = new ArrayList<String>();
        NodeList lemmaList = root.getElementsByTagName("Lemma");
        for(int i = 0; i < lemmaList.getLength(); i++) {

            if (i==0) {
                String lemma1 = lemmaList.item(i).getTextContent();
                keywords.add(("<B> "+lemma1).intern());
            }

            if (i+1 < lemmaList.getLength()) {

                String lemma1 = lemmaList.item(i).getTextContent();
                String lemma2 = lemmaList.item(i+1).getTextContent();
                keywords.add((lemma1 + " " + lemma2).intern());
                keywords.add(lemma1.intern());

            } else {

                String lemma1 = lemmaList.item(i).getTextContent();
                keywords.add((lemma1+" <E>").intern());
                keywords.add(lemma1.intern());

            }



        }


        return keywords;

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


}
