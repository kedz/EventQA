package edu.columbia.cs.event.qa.task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.columbia.cs.event.qa.util.FileLoader;
import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Evaluation {

    private static String ARRFHeader1;
    private static String ARRFHeader2;
    private static String ARRFHeader3;

    static {
        ARRFHeader1 = "@relation amt-qa-training\n\n@attribute cosign numeric\n@attribute label {1,0}\n\n@data";
        ARRFHeader2 = "@relation amt-qa-training\n\n";
        for (int i=0; i<200; i++) {
            if (i==100) { ARRFHeader3 = ARRFHeader2; }
            ARRFHeader2 += "@attribute semantic"+(i+1)+" numeric\n";
        }
        ARRFHeader2 += "@attribute label {1,0}\n\n@data";
        ARRFHeader3 += "@attribute label {1,0}\n\n@data";
    }

    private Document data;

    boolean printResults;

    public void loadXMLData (String fileName) {
        try {
            System.out.println("Loading XML Data: ("+fileName+")");
            data = FileLoader.newInstance().loadXMLData(fileName);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void analyze () throws IOException {
        analyze(ProjectConfiguration.getInstance().getProperty("train.xml.file"));
        analyze(ProjectConfiguration.getInstance().getProperty("test.xml.file"));
    }

    public void analyze (String fileName) throws IOException {

        loadXMLData(fileName);

//        PrintWriter writer1 = new PrintWriter(new FileWriter(fileName.replaceAll("xml", "cs.arff")));
//        PrintWriter writer2 = new PrintWriter(new FileWriter(fileName.replaceAll("xml", "ss.arff")));
//        PrintWriter writer3 = new PrintWriter(new FileWriter(fileName.replaceAll("xml", "ed.arff")));

//        writer1.println(ARRFHeader1);
//        writer2.println(ARRFHeader2);
//        writer3.println(ARRFHeader3);

        System.out.println("*******************************************************************");
        System.out.println("DOCUMENT");

        NodeList queries = data.getElementsByTagName("query");
        NodeList answers = data.getElementsByTagName("response");
        NodeList labels = data.getElementsByTagName("label");

        for (int i=0; i<queries.getLength(); i++) {
            String query = queries.item(i).getFirstChild().getNodeValue();
            String answer = answers.item(i).getFirstChild().getNodeValue();
            String label = labels.item(i).getFirstChild().getNodeValue();

            double[] q = SSProjection.newInstance().transform(query);
            double[] a = SSProjection.newInstance().transform(answer);
            double similarity = SSProjection.newInstance().computeCosignSimilarity(q, a);

//            writer1.print(similarity+","+label+"\n");

//            for (int j=0; j<q.length; j++) {
//                writer2.print(q[j]+",");
//                double ans = (q[j]-a[j])*(q[j]-a[j]);
//                writer3.print(ans+",");
//            }
//            for (int j=0; j<q.length; j++) {
//                writer2.print(a[j] + ",");
//            }
//            writer2.print(label+"\n");
//            writer3.print(label+"\n");

            if (printResults) {
                System.out.println("*******************************************************************");
                System.out.println("Query: "+query);
                System.out.println("Answer: "+answer);
                System.out.println("Label: "+label);
                System.out.println("Q[0]: "+q[0]);
                System.out.println("A[0]: "+a[0]);
                System.out.println("Cosign Similarity: "+similarity);
            }
        }

//        writer1.flush(); writer1.close();
//        writer2.flush(); writer2.close();
//        writer3.flush(); writer3.close();
    }

    public void setPrintResults (boolean printResults) { this.printResults = printResults; }

    public static void main (String[] args) {

        Evaluation eval = new Evaluation();
        //eval.load();
        eval.setPrintResults(true);

        String trainDir = "/Users/wojo/Documents/eventQA/amt_qa/train/";
        String testDir = "/Users/wojo/Documents/eventQA/amt_qa/test/";

        try {
            eval.analyze();
//            for (int i=1; i<6; i++) {
//                eval.analyze(trainDir + "amt_qa_train_v" + i + "_t_.45.xml");
//                eval.analyze(testDir+"amt_qa_test_v"+i+"_t_.45.xml");
//            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}