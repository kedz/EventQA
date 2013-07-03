package edu.columbia.cs.event.qa.cotraining;

import com.sun.scenario.effect.impl.state.LinearConvolveKernel;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.OctaveDouble;
import edu.columbia.cs.nlptk.util.OrderedWordList;
import edu.stanford.nlp.ling.CoreAnnotations;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 7/3/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class BayesNetCcaKwEnrichedNeClassifier implements WekaInterface {

    private OctaveEngine octave;
    private int eigenFeatureSize;
    private Instances dataSet;
    private BayesNet bayesNet;
    private OrderedWordList keywordIndex;


    public BayesNetCcaKwEnrichedNeClassifier(File projectionFile, File keywordIndexFile) {

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
            wekaAttributes.addElement(new Attribute("feature"+(i+1)));
        }

        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement(1.0);
        fvClassVal.addElement(0.0);
        Attribute label = new Attribute("label", fvClassVal);
        wekaAttributes.addElement(label);
        dataSet = new Instances("keyword-enriched-w-ne",wekaAttributes,1000);

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


        octave.eval("x = sparse("+eigenFeatureSize+",1);");
        for(String keyword : activeFeatures) {

            int index = keywordIndex.getWordList().indexOf(keyword);
            if (index > -1) {

                index = index + 1;
                octave.eval("x("+index+",1) = 1;");

            }



        }

        octave.eval("xProj = A'*x;");
        OctaveDouble eigenFeatures = octave.get(OctaveDouble.class,"xProj");

        double[] featureValues = new double[eigenFeatureSize+1];
        for(int i = 0; i < eigenFeatureSize;i++) {
            int octIndex = i+1;
            featureValues[i] = eigenFeatures.get(octIndex,1);
        }

        Instance instance = new Instance(1,featureValues);
        instance.setDataset(dataSet);

        if (pair.getLabel()!=-1)
            instance.setClassValue(pair.getLabel());
        else {
            instance.setClassMissing();
        }


        return instance;

    }


    public void shutdown() {
        System.out.println("Closing octave engine.");
        octave.close();
    }

    public int getLabel (Instance wekaInstance) {

        int labelAsInt = -1;

        try {

          double label = bayesNet.classifyInstance(wekaInstance);
          wekaInstance.setClassValue(label);

          labelAsInt = (int) label;

        } catch(Exception e) {
            System.out.println("Classifier failed: "+e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        return labelAsInt;

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


}
