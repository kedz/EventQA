package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.classifier.KeywordOverlapBayesNetClassifier;
import edu.columbia.cs.event.qa.classifier.NamedEntityOverlapBayesNetClassifier;

/**
 * User: wojo
 * Date: 7/16/13
 * Time: 10:42 AM
 */

public class BaselineRun {

    public static void main (String[] args) {
        System.out.println("********************** Loading Resources *********************");
        System.out.println("Bayes Net - Keyword Overlap Features:");
        KeywordOverlapBayesNetClassifier c1 = new KeywordOverlapBayesNetClassifier();
        System.out.println("Bayes Net - Named Entity Overlap Features:");
        NamedEntityOverlapBayesNetClassifier c2 = new NamedEntityOverlapBayesNetClassifier();
        System.out.println("************************* COTRAINING *************************");
        Cotraining droid = new Cotraining();
        droid.load(c1, c2);
        droid.run();
    }
}
