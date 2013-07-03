package edu.columbia.cs.event.qa.cotraining;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/2/13
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */

public interface WekaInterface {

    public Instances initializeInstances ();

    public Classifier buildWekaClassifier (Instances wekaInstances);

    public Instance buildWekaInstance (QAPair pair);

    public int getLabel (Instance wekaInstance);

    public void shutdown ();

}
