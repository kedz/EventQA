package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.classifier.CcaKwEnrichedNeBayesNetClassifier;
import edu.columbia.cs.event.qa.classifier.CosineSimilarityBayesNetClassifier;
import edu.columbia.cs.event.qa.classifier.KeywordOverlapBayesNetClassifier;
import edu.columbia.cs.event.qa.classifier.NamedEntityOverlapBayesNetClassifier;

import java.io.File;

/**
 * User: wojo
 * Date: 7/16/13
 * Time: 3:26 PM
 */

public class CosineSimKweneRun {

    public static void main (String[] args) {
        System.out.println("********************** Loading Resources *********************");
        System.out.println("Bayes Net - CCA - Keywords Features Enriched with Named Entity Features:");
        File projectionFile = new File("/Users/wojo/Documents/eventQA/resources/kw.enriched.ne.proj");
        File keywordIndexFile = new File("/Users/wojo/Documents/eventQA/resources/keyword.overlap.index");
        CcaKwEnrichedNeBayesNetClassifier c1 = new CcaKwEnrichedNeBayesNetClassifier(projectionFile, keywordIndexFile);
        System.out.println("SMO - Cosine Similarity via Semantic Space Features:");
        CosineSimilarityBayesNetClassifier c2 = new CosineSimilarityBayesNetClassifier();
        System.out.println("************************* COTRAINING *************************");
        Cotraining droid = new Cotraining();
        droid.load(c1, c2);
        droid.run();
    }

}
