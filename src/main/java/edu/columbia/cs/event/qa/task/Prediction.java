package edu.columbia.cs.event.qa.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;
import edu.columbia.cs.event.qa.classifier.WekaSMOClassifier;
import edu.columbia.cs.event.qa.util.ProjectConfiguration;

public class Prediction {

    private String testInputFileName;
    private String testWekaFile;

    private int mode;

    private WekaSMOClassifier classifier;

    public Prediction () {
        this.mode = Integer.parseInt(ProjectConfiguration.getInstance().getProperty("mode"));
    }

    public void computeSimilarityScore (String QAset) throws Exception {
        if (mode == 1) {
            computeSimilarityScoreParagraphLevel(QAset);
        } else if (mode == 2) {
            computeSimilarityScoreSentenceLevel(QAset);
        } else {
            System.out.println("Enter correct processing level: (1) for Paragraph level and (2) for Sentence level");
            System.exit(1);
        }
    }

    public void computeSimilarityScoreParagraphLevel(String QASet) throws Exception {
//        PrintWriter writer = new PrintWriter(new FileWriter(testWekaFile));
//        writer.println("@relation event");
//        writer.println("@attribute cosine numeric");
//        writer.println("@attribute class {yes, no}");
//        writer.println();
//        writer.println("@data");
//        writer.flush();

        // TODO Use different delimeter - readNewsblasterXML
        String[] QA = QASet.split("`");

//        double[] query = foldin(transform(simplePreprocessor.forge(QA[0])));
        double[] query = SSProjection.newInstance().transform(QA[0]);

        ArrayList<Double> sims = new ArrayList<Double>();

        for (int i=1; i<QA.length; i++) {
//            double[] answer = foldin(transform(simplePreprocessor.forge(QA[i])));
            double[] answer = SSProjection.newInstance().transform(QA[i]);
//            double similarity = computeSimilarityBetweenPair(query, answer);
            double similarity = SSProjection.newInstance().computeCosignSimilarity(query, answer);
            sims.add(similarity);
//            writer.println(similarity+",yes");
//            writer.flush();
        }
//        writer.flush();
//        writer.close();

        System.out.println("** Query:"+QA[0]);
        int c = 1;
        for (String s : classifier.predict(testWekaFile)) {
            System.out.println("** Ans"+c+":"+QA[c]);
            System.out.println("Similarity:"+sims.get(c-1));
            System.out.println("Prediction:"+s);
            c++;
        }
    }

    public void computeSimilarityScoreSentenceLevel(String QASet) throws Exception {

//        PrintWriter writer = new PrintWriter(new FileWriter(testWekaFile));
//        writer.println("@relation event");
//        writer.println("@attribute cosine numeric");
//        writer.println("@attribute class {yes, no}");
//
//        writer.println("@data");
//        writer.println();
//        writer.flush();

        String[] QA = QASet.split("`");

//        double[] q = foldin(transform(simplePreprocessor.forge(QA[0])));
        double[] q = SSProjection.newInstance().transform(QA[0]);

        ArrayList<String> actualStr=new ArrayList<String>();

        ArrayList<Double> sims=new ArrayList<Double>();

        int cnt=0;
        for (int i=1;i<QA.length;i++)
        {
            for (String sentence: sentenceSplitter(QA[i]))
            {
                cnt++;
//                double[] ans=foldin(transform(simplePreprocessor.forge(sentence)));
                double[] ans = SSProjection.newInstance().transform(sentence);
//                double similarity=computeSimilarityBetweenPair(q, ans);
                double similarity = SSProjection.newInstance().computeCosignSimilarity(q, ans);
                if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
                {
                    actualStr.add(sentence);
                    sims.add(similarity);
//                    writer.println(similarity+",yes");
//                    writer.flush();
                }
            }
        }
//        writer.flush();
//        writer.close();

        System.out.println("Total number of sentences seen: "+cnt);
        System.out.println("Valid sentences: "+actualStr.size());

        StringBuilder summary = new StringBuilder();
        StringBuilder notChosen = new StringBuilder();

        ArrayList<String> predictions = classifier.predict(testWekaFile);

        if (predictions.size()!=actualStr.size()) {
            System.out.println("NOT SAME");
            System.out.println(predictions.size()+"!="+actualStr.size());
            System.exit(0);
        } else {
            for (int i=0;i<predictions.size();i++) {
                if (predictions.get(i).equals("yes")) {
                    summary.append(actualStr.get(i));
                } else {
                    notChosen.append(actualStr.get(i));
                }
            }
        }

        for (String s: actualStr) { System.out.println(s); }
        System.out.println("Chosen:"+summary.toString());
        System.out.println("*******");
        System.out.println("NOT Chosen:"+notChosen.toString());
    }

    private ArrayList<String> sentenceSplitter(String source) {
        ArrayList<String> sentences=new ArrayList<String>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(source);
        int start = iterator.first();
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            sentences.add(source.substring(start,end));
        }
        return sentences;
    }

    public void run () throws Exception {
        String QASet;
        BufferedReader reader = new BufferedReader (new FileReader(testInputFileName));
        while((QASet = reader.readLine()) != null) {
            computeSimilarityScore(QASet);
            System.out.println("****************************************************");
        }
    }

    public static void main (String args[]) throws Exception { (new Prediction()).run(); }
}