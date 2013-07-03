package edu.columbia.cs.event.qa.classifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;

public class WekaSMOClassifier implements Serializable {

    private Classifier svm;
    private Instances train;

    public WekaSMOClassifier () throws Exception {
        this(ProjectConfiguration.getInstance().getProperty("weka.training.file"));
    }

    public WekaSMOClassifier (String trainFileName) throws Exception {
        this.train = getInstances(trainFileName);
        this.svm = new SMO ();
        this.svm.buildClassifier(train);
    }

    private Instances getInstances (String fileName) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        Instances instances = new Instances(reader);
        instances.setClassIndex(instances.numAttributes()-1);
        return instances;
    }

    public ArrayList<String> predict () throws Exception {
        return predict(ProjectConfiguration.getInstance().getProperty("weka.testing.file"));
    }

    public ArrayList<String> predict (String testFileName) throws Exception {
        Instances test = getInstances(testFileName);
        ArrayList<String> predictedClasses = new ArrayList<String>();
        for (int i=0; i<test.numInstances(); i++) {
            double score = svm.classifyInstance(test.instance(i));
            predictedClasses.add(test.classAttribute().value((int) score));
        }
        return predictedClasses;
    }

    public void evaluate () throws Exception {
        evaluate(ProjectConfiguration.getInstance().getProperty("weka.testing.file"));
    }

    public void evaluate (String testFileName) throws Exception {
        Instances test = getInstances(testFileName);
        Evaluation evaluation = new Evaluation(train);
        evaluation.crossValidateModel(svm, train, 10, new Random(1));
        System.out.println(evaluation.toSummaryString("\nResults\n*******\n", true));
        System.out.println(evaluation.fMeasure(1)+" "+evaluation.precision(1)+" "+evaluation.recall(1));

        for (int i=0;i<test.numInstances();i++) {
            double score = svm.classifyInstance(test.instance(i));
            System.out.print("ID: " + test.instance(i).value(0));
            System.out.print(", actual: " + test.classAttribute().value((int) test.instance(i).classValue()));
            System.out.println(", predicted: " + test.classAttribute().value((int) score));
        }
    }

    public Classifier getSvm () { return svm; }
    public Instances getTrain () { return train; }
}