package edu.columbia.cs.event.qa.task;

import java.io.IOException;

import edu.columbia.cs.event.qa.classifier.CosineSimilarityBayesNetClassifier;
import edu.columbia.cs.event.qa.classifier.WekaClassifierInterface;
import edu.columbia.cs.event.qa.cotraining.QAPair;
import edu.columbia.cs.event.qa.util.LoadMachine;
import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import edu.columbia.cs.event.qa.util.SaveMachine;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class WekaEvaluator {

    private WekaClassifierInterface classifier;
    private boolean printResultsOn;
    private boolean saveInstancesOn;

    public WekaEvaluator(WekaClassifierInterface classifier) {
        this.classifier = classifier;
    }

    public Document loadXMLData (String fileName) {
        try {
            System.out.println("***************************** LOADING *****************************");
            return LoadMachine.newInstance().loadXMLFile(fileName);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void analyze () throws Exception {
        System.out.println("***************************** TRAINING ****************************");
        Instances trainExamples = documentToInstances(ProjectConfiguration.newInstance().getProperty("amt.train.file"));
        System.out.println("***************************** TESTING *****************************");
        Instances testExamples = documentToInstances(ProjectConfiguration.newInstance().getProperty("amt.test.file"));
        System.out.println("*************************** EVALUATION ****************************");
        String results = evaluate(trainExamples, testExamples);
        System.out.println(results);
    }

    public Instances documentToInstances (String fileName) throws IOException {
        Instances instances = documentToInstances(loadXMLData(fileName));
        if (saveInstancesOn) {
            SaveMachine.newInstance().saveInstancesToARFF(instances, fileName.replaceAll("xml", "arff"));
        }
        return instances;
    }

    public Instances documentToInstances (Document data) throws IOException {

        NodeList queries = data.getElementsByTagName("Question");
        NodeList answers = data.getElementsByTagName("Response");
        NodeList labels = data.getElementsByTagName("Label");

        Instances instances = classifier.initializeInstances();

        for (int i=0; i<queries.getLength(); i++) {
            String label = labels.item(i).getFirstChild().getNodeValue();
            QAPair pair = new QAPair(queries.item(i), answers.item(i), label);
            instances.add(classifier.buildWekaInstance(pair));

            if (printResultsOn) {
                System.out.println("*******************************************************************");
                System.out.println("Query: "+pair.getQueryString());
                System.out.println("Answer: "+pair.getAnswerString());
                System.out.println("Label: "+label);
            }
        }

        return instances;
    }

    public String evaluate (Instances trainInstances, Instances testInstances) throws Exception {
        Classifier c = classifier.buildWekaClassifier(trainInstances);
        Evaluation e = new Evaluation(trainInstances);
        e.evaluateModel(c, testInstances);
        return e.toSummaryString();
    }

    public void setPrintResultsOn(boolean printResultsOn) { this.printResultsOn = printResultsOn; }
    public void setSaveInstancesOn(boolean saveInstancesOn) { this.saveInstancesOn = saveInstancesOn; }

    public static void main (String[] args) {

        WekaClassifierInterface classifier = new CosineSimilarityBayesNetClassifier();
        WekaEvaluator eval = new WekaEvaluator(classifier);
        eval.setPrintResultsOn(true);
        //eval.setSaveInstancesOn(true);
        try {
            eval.analyze();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}