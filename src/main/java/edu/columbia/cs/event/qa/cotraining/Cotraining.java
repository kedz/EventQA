package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.classifier.KeywordOverlapBayesNetClassifier;
import edu.columbia.cs.event.qa.classifier.NamedEntityOverlapBayesNetClassifier;
import edu.columbia.cs.event.qa.classifier.WekaClassifierInterface;
import edu.columbia.cs.event.qa.task.WekaEvaluator;
import edu.columbia.cs.event.qa.util.ExcelMachine;
import edu.columbia.cs.event.qa.util.LoadMachine;
import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import edu.columbia.cs.event.qa.util.SaveMachine;
import weka.classifiers.Evaluation;
import weka.core.Instance;

import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/2/13
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */

public class Cotraining {

    private int k;  // # iterations
    private int u;  // pool size
    private int v;  // leap size

    private String coTrainFile;
    private String coTestFile;
    private String saveDir;
    private String version;

    private LabeledData labeledData;
    private LabeledData testData;

    private boolean printExamplesOn;

    public Cotraining () {

        k = Integer.parseInt(ProjectConfiguration.newInstance().getProperty("number.of.iterations"));
        u = Integer.parseInt(ProjectConfiguration.newInstance().getProperty("pool.size"));
        v = Integer.parseInt(ProjectConfiguration.newInstance().getProperty("leap.size"));

        coTrainFile = ProjectConfiguration.newInstance().getProperty("cotrain.amt.file");
        coTestFile = coTrainFile.replaceAll("train", "test");

        saveDir = ProjectConfiguration.newInstance().getProperty("classifier.dir");

        Pattern p = Pattern.compile("v\\d");
        Matcher m = p.matcher(coTrainFile);
        if (m.find()) {
            version = m.group(0);
        } else {
            version = "v00";
        }
    }

    public void load (WekaClassifierInterface c1, WekaClassifierInterface c2) {
        System.out.println("**************************************************************");
        System.out.println("Building AMT Seed Training Data:");
        labeledData = LoadMachine.newInstance().loadAmazonMechanicalTurkData(c1, c2, coTrainFile);
        System.out.println("**************************************************************");
        System.out.println("Building AMT Seed Testing Data");
        testData = LoadMachine.newInstance().loadAmazonMechanicalTurkData(c1, c2, coTestFile);
    }

    public void run () {
        ExcelMachine excel = new ExcelMachine(labeledData.getClassifier1().nickname(), labeledData.getClassifier2().nickname());
        System.out.println("**************************************************************");
        System.out.println("Running Cotraining with parameters: (k="+k+" u="+u+" v="+v+")");
        for (int i=0; i<k; i++) {
            System.out.println("***************************** "+i+" ******************************");
            System.out.println("Labeled data: "+ labeledData.getInstances1().numInstances()+" : "+ labeledData.getInstances2().numInstances());
            System.out.println("1) Training classifiers using labeled data:");
            labeledData.buildClassifiers();
            System.out.println("2) Adding selected examples from C1 to labeled data.");
            int a = addExamplesFromNewsblaster(labeledData.getClassifier1());
            System.out.println("2) Adding selected examples from C2 to labeled data.");
            int b = addExamplesFromNewsblaster(labeledData.getClassifier2());
            System.out.println("Total examples added: " + a + " : " + b+"");

            System.out.println("************************* EVALUATION *************************");
            Evaluation eval = labeledData.evaluateClassifier1(testData.getInstances1());
            String result = excel.updateSheet(labeledData.getClassifier1().nickname(), eval);
            System.out.println(result);
            System.out.println("************************* EVALUATION *************************");
            eval = labeledData.evaluateClassifier2(testData.getInstances2());
            result = excel.updateSheet(labeledData.getClassifier2().nickname(), eval);
            System.out.println(result);
        }
        labeledData.shutdown();
        save(excel);
    }

    public void save (ExcelMachine excel) {
        System.out.println("*************************** SAVING ***************************");
        String template = saveDir+version+"_type_k"+k+"_v"+v+"_c_"+labeledData.getClassifier1().nickname()+"_"+labeledData.getClassifier2().nickname()+".ext";
        System.out.println("Save Template: "+template);
        excel.saveWorkbook(template.replaceAll("ext", "xls").replaceAll("type", "eval").replaceAll("c_", ""));
        template = template.replaceAll("ext", "arff");
        SaveMachine.newInstance().saveInstancesToARFF(labeledData.getInstances1(), template.replaceAll("type", "train").replaceAll("c_", "c1_"));
        SaveMachine.newInstance().saveInstancesToARFF(labeledData.getInstances2(), template.replaceAll("type", "train").replaceAll("c_", "c2_"));
        SaveMachine.newInstance().saveInstancesToARFF(testData.getInstances1(), template.replaceAll("type", "test").replaceAll("c_", "c1_"));
        SaveMachine.newInstance().saveInstancesToARFF(testData.getInstances2(), template.replaceAll("type", "test").replaceAll("c_", "c2_"));
    }

//    public void saveInstances (LabeledData data, String type) {
//        SaveMachine.newInstance().saveInstancesToARFF(data.getInstances1(), instances1File.replaceAll("type", type));
//        SaveMachine.newInstance().saveInstancesToARFF(data.getInstances2(), instances2File.replaceAll("type", type));
//    }

    public int addExamplesFromNewsblaster (WekaClassifierInterface classifier) {
        /* Randomly choose set of examples from Newsblaster data */
        HashMap<Instance,QAPair> instanceMap = selectExamplesFromNB(classifier);
        System.out.println("Number examples selected: "+instanceMap.size());
        /* Select v balanced examples from set */
        Stack<Instance> balancedInstances = getReducedSetOfBalancedExamples(instanceMap.keySet());
        System.out.println("Reduced set of examples: "+balancedInstances.size());
        int numInstances = balancedInstances.size();
        /* Add examples to labeledData data and update labeledData table */
        while (!balancedInstances.isEmpty()) {
            Instance view = balancedInstances.pop();
            QAPair pair = instanceMap.get(view);
            labeledData.addInstance(view, pair);
            NewsblasterData.newInstance().updateSeedTable(pair.getQAString());
            if (printExamplesOn) {
                System.out.print(pair.getLabel() );
                System.out.print(" [Q] " + pair.getQueryString());
                System.out.println(" [A] "+pair.getAnswerString());
            }
        }
        instanceMap = null;
        balancedInstances = null;
        return numInstances;
    }

    public HashMap<Instance,QAPair> selectExamplesFromNB (WekaClassifierInterface classifier) {
        HashMap<Instance,QAPair> map = new HashMap<Instance,QAPair>();
        for (int i=0; i<u/2; i++) {
            QAPair pair = NewsblasterData.newInstance().selectUniqueQAPair();
            Instance view = classifier.buildWekaInstance(pair);
            String label = classifier.classifyInstance(view);
            pair.setLabel(label);
            map.put(view, pair);
            if (printExamplesOn) {
                System.out.print(pair.getLabel() );
                System.out.print(" [Q] "+pair.getQueryString());
                System.out.println(" [A] "+pair.getAnswerString());
            }
        }
        return map;
    }

    public Stack<Instance> getReducedSetOfBalancedExamples (Set<Instance> instances) {
        Stack<Instance> positiveInstances = new Stack();
        Stack<Instance> negativeInstances = new Stack();
        Stack<Instance> balancedInstances = new Stack();
        for (Instance view : instances) {
            if (view.toString(view.classIndex()).equals("1")) { positiveInstances.push(view); }
            else { negativeInstances.push(view); }
        }
        int num = Math.min(v/2, Math.min(positiveInstances.size(), negativeInstances.size()));
        while (num-- > 0) {
            balancedInstances.push(positiveInstances.pop());
            balancedInstances.push(negativeInstances.pop());
        }
        instances = null;
        positiveInstances = null;
        negativeInstances = null;
        return balancedInstances;
    }
}
