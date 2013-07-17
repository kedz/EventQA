package edu.columbia.cs.event.qa.classifier;

import edu.columbia.cs.event.qa.cotraining.QAPair;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/2/13
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */

public interface WekaClassifierInterface {

    public String classifierName ();

    public String nickname();

    public Instances initializeInstances ();

    public Classifier buildWekaClassifier (Instances wekaInstances);

    public Instance buildWekaInstance (QAPair pair);

    public String classifyInstance (Instance wekaInstance);

    public Evaluation evaluateClassifier (Instances trainInstances, Instances testInstances);

    public void shutdown ();

}
