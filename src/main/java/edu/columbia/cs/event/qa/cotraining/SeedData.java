package edu.columbia.cs.event.qa.cotraining;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/2/13
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */

public class SeedData {

    private Instances i1;
    private Instances i2;
    private WekaInterface c1;
    private WekaInterface c2;

    public SeedData (WekaInterface c1, WekaInterface c2) {
        this.c1 = c1;
        this.c2 = c2;
        this.i1 = c1.initializeInstances();
        this.i2 = c2.initializeInstances();
    }

    public void buildClassifiers () {
        c1.buildWekaClassifier(i1);
        c2.buildWekaClassifier(i2);
    }

    public void addInstanceToI1 (QAPair pair) { i1.add(c1.buildWekaInstance(pair)); }
    public void addInstanceToI2 (QAPair pair) { i2.add(c2.buildWekaInstance(pair)); }

    public void addInstanceToI1 (Instance i) { i1.add(i); }
    public void addInstanceToI2 (Instance i) { i2.add(i); }

    public WekaInterface getClassifier1 () { return c1; }
    public WekaInterface getClassifier2 () { return c2; }

}
