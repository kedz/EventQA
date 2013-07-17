package edu.columbia.cs.event.qa.classifier;

import edu.columbia.cs.event.qa.cotraining.QAPair;
import edu.columbia.cs.event.qa.task.SemanticSpaceProjector;
import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/3/13
 * Time: 4:51 PM
 * To change this template use File | Settings | File Templates.
 */

public class CosineSimilarityBayesNetClassifier implements WekaClassifierInterface {

    private Classifier bayesNet;

    private SemanticSpaceProjector projector;

    public CosineSimilarityBayesNetClassifier() {
        projector = SemanticSpaceProjector.newInstance();
    }

    public String classifierName () {
        return "SMO - Semantic Space Features (k="+ProjectConfiguration.newInstance().getProperty("number.of.eigenvectors")+")";
    }

    public String nickname () { return "cs"; }

    public Instances initializeInstances () {

        FastVector wekaAttributes = buildCosineAttributes();
        Attribute label = (Attribute) wekaAttributes.lastElement();

        Instances data = new Instances("semantic-space", wekaAttributes, 1000);
        data.setClass(label);

        return data;
    }

    public FastVector buildCosineAttributes () {
        FastVector attributes = new FastVector(2);
        attributes.addElement(new Attribute("cosine"));
        FastVector classVal = new FastVector();
        classVal.addElement("1");
        classVal.addElement("0");
        Attribute label = new Attribute("label", classVal);
        attributes.addElement(label);
        return attributes;
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

        double[] query = projector.transform(pair.getQueryList());
        double[] answer = projector.transform(pair.getAnswerList());
        double[] cosine = { projector.computeCosignSimilarity(query, answer), 0.0 };

        FastVector attributes = buildCosineAttributes();
        Attribute label = (Attribute) attributes.lastElement();

        Instances testInstances = new Instances("test",attributes,1);
        testInstances.setClass(label);

        Instance example = new Instance(1, cosine);
        testInstances.add(example);
        example.setDataset(testInstances);

        if (!pair.getLabel().equals("-1")) {
            example.setClassValue(pair.getLabel());
        } else {
            example.setClassMissing();
        }
        return example;
    }

    public String classifyInstance (Instance wekaInstance) {
        String label = null;
        try {
            double labelIndex = bayesNet.classifyInstance(wekaInstance);
            wekaInstance.setClassValue(labelIndex);
            label = wekaInstance.toString(wekaInstance.classIndex());
        } catch(Exception e) {
            System.err.println(e.getMessage());
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
