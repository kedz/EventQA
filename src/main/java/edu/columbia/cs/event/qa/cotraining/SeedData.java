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

    private Classifier c1;
    private Classifier c2;

    private static SeedData SeedData;

    public static SeedData newInstance() {
        if(SeedData == null)
            SeedData = new SeedData();
        return SeedData;
    }

    public SeedData () {
        // TODO
        // i1 = initializeInstances;
        // i2 = initializeInstances;
    }

    public void buildClassifiers () {
        // TODO
        // c1 = buildWekaClassifier(i1);
        // c2 = buildWekaClassifier(i2);
    }

    public void addInstanceToI1 (Instance i) { i1.add(i); }
    public void addInstanceToI2 (Instance i) { i2.add(i); }

    public Instances getC1 () { return i1; }
    public Instances getC2 () { return i2; }
}
