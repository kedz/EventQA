package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.classifier.WekaClassifierInterface;
import edu.columbia.cs.event.qa.task.WekaEvaluator;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/2/13
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */

public class LabeledData {

    private Instances i1;
    private Instances i2;

    private WekaClassifierInterface c1;
    private WekaClassifierInterface c2;

    public LabeledData(LabeledData seed) {
        this.i1 = new Instances(seed.getInstances1());
        this.i2 = new Instances(seed.getInstances2());
        this.c1 = seed.getClassifier1();
        this.c2 = seed.getClassifier2();
    }

    public LabeledData(WekaClassifierInterface c1, WekaClassifierInterface c2) {
        System.out.println("Loading Weka Classifiers:");
        this.c1 = c1;
        System.out.println("Classifier 1: "+c1.classifierName());
        this.c2 = c2;
        System.out.println("Classifier 2: "+c2.classifierName());
        System.out.print("Initializing seed data instances for C1: ");
        this.i1 = c1.initializeInstances();
        System.out.println((i1.numAttributes()-1)+" attributes");
        System.out.print("Initializing seed data instances for C2: ");
        this.i2 = c2.initializeInstances();
        System.out.println((i2.numAttributes()-1)+" attributes");
        System.out.println("Initializing Weka Evaluators");
    }

    public void buildClassifiers () {
        c1.buildWekaClassifier(i1);
        c2.buildWekaClassifier(i2);
    }

    public void addInstance (QAPair pair) {
        i1.add(c1.buildWekaInstance(pair));
        i2.add(c2.buildWekaInstance(pair));
    }

    public void addInstance (Instance instance, QAPair pair) {
        if (i1.checkInstance(instance)) {
            i1.add(instance); i2.add(c2.buildWekaInstance(pair));
        } else if (i2.checkInstance(instance)) {
            i2.add(instance); i1.add(c1.buildWekaInstance(pair));
        } else {
            System.err.println("Error: Instance does not match any data set.");
        }
    }

    public Evaluation evaluateClassifier1 (Instances testInstances) {
        return c1.evaluateClassifier(i1, testInstances);
    }

    public Evaluation evaluateClassifier2 (Instances testInstances) {
        return c2.evaluateClassifier(i2, testInstances);
    }

    public WekaClassifierInterface getClassifier1 () { return c1; }
    public WekaClassifierInterface getClassifier2 () { return c2; }

    public Instances getInstances1 () { return i1; }
    public Instances getInstances2 () { return i2; }

    public void shutdown () { c1.shutdown(); c2.shutdown(); }

}
