package edu.columbia.cs.event.qa.task;

import edu.columbia.cs.event.qa.util.EventQAConfig;
import edu.columbia.cs.event.qa.util.Preprocessor;
//import org.jblas.DoubleMatrix;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Map.Entry;
import java.util.Map;
//import java.util.TreeMap;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 6/13/13
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */

public class Training {

    private Preprocessor preprocessor;

//    private Map<Integer, ArrayList<String>> docIndexMap;
//    private Map<String, ArrayList<Integer>> wordSeenInDocMap;
//    private ArrayList<String> terms;

    HashMap<String, String> termByDocumentCount;

//    private DoubleMatrix TxDMatrix;
    private int numDocs, numTerms;

    public Training () {
        preprocessor = new Preprocessor();
        termByDocumentCount = new HashMap<String, String>();
//
//        docIndexMap = new HashMap<Integer,ArrayList<String>>();
//        wordSeenInDocMap = new TreeMap<String, ArrayList<Integer>>();
//        terms = new ArrayList<String>();
    }

    public void load () throws IOException {
        load(EventQAConfig.getInstance().getProperty("corpus.file"));
    }

    public void load (String fileName) throws IOException {

        String line; int i = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while ((line = reader.readLine()) != null) {
            ArrayList<String> tokenList = preprocessor.run(line);
            if (tokenList.size() > 0) {

                HashMap<String, Integer> wordCount = new HashMap<String, Integer>();

                for (String token : tokenList) {
                    int count = 0;
                    if (wordCount.containsKey(token)) {
                        count = wordCount.get(token);
                    }
                    wordCount.put(token, count+1);
                }

                for (Map.Entry<String, Integer> entry: wordCount.entrySet()) {

                    String term = entry.getKey();
                    String rowCounts = "";

                    if (termByDocumentCount.containsKey(term)) {
                        rowCounts = termByDocumentCount.get(term);
                        for (int j=rowCounts.split(",").length; j<i; j++) { rowCounts += ",0"; }
                        rowCounts += ",";
                    } else {
                        for (int j=0; j<i; j++) { rowCounts += "0,"; }
                    }

                    rowCounts += entry.getValue();

                    if (rowCounts.split(",").length != i+1) {
                        System.out.println("rowCount.split.length != i+1");
                    }

                    termByDocumentCount.put(term, rowCounts);
                }
                if (i%1000 == 0) { System.out.println("Read "+i+" documents"); } i++;
                i++;
            }
        }
        numDocs = i;
    }

    public void save () throws IOException {
        save(EventQAConfig.getInstance().getProperty("vocab.file"), EventQAConfig.getInstance().getProperty("term.doc.file"));
    }

    public void save (String termFileName, String termByDocFileName) throws IOException {

        PrintWriter writer1 = new PrintWriter(new FileWriter(termFileName));
        PrintWriter writer2 = new PrintWriter(new FileWriter(termByDocFileName));

        int i = 0;
        for (Map.Entry<String, String> entry: termByDocumentCount.entrySet()) {
            String rowCounts = entry.getValue();
            for (int j=rowCounts.split(",").length; j<numDocs; j++) { rowCounts += ",0"; }
            writer1.println(entry.getKey());
            writer2.println(rowCounts);
            if (i%100 == 0) { System.out.println("Processed "+i+" terms"); }
            i++;
        }
        numTerms = i;

        writer1.flush(); writer1.close();
        writer2.flush(); writer2.close();
    }

    public void printCorpusStats () {
        System.out.println("********** Printing Corpus Statistics **********");
        System.out.println("Total # of Terms: "+numTerms);
        System.out.println("Total # of Docs: "+numDocs);
        System.out.println("************************************************");
    }

//    public void loadTrainingData () throws IOException {
//        loadTrainingData(EventQAConfig.getInstance().getProperty("corpus.file"));
//    }
//
//    public void loadTrainingData (String fileName) throws IOException {
//
//        String line; int i = 0;
//        BufferedReader reader = new BufferedReader(new FileReader(fileName));
//        while ((line = reader.readLine()) != null) {
//
//            ArrayList<String> tokenList = preprocessor.run(line);
//            if (tokenList.size()>0) {
//                docIndexMap.put(i, tokenList);
//
//                for (String word : tokenList) {
//                    if (wordSeenInDocMap.containsKey(word)) {
//                        ArrayList<Integer> existingDocs = wordSeenInDocMap.get(word);
//
//                        if (!existingDocs.contains(i)) {
//                            existingDocs.add(i);
//                            wordSeenInDocMap.put(word, existingDocs);
//                        }
//
//                    } else {
//                        ArrayList<Integer> existingDocs = new ArrayList<Integer>();
//                        existingDocs.add(i);
//                        wordSeenInDocMap.put(word, existingDocs);
//                    }
//                }
//            }
//            if (i%100 == 0) { System.out.println("Read "+i+" documents"); } i++;
//            if (i > 1000) break;
//        }
//
//        for (Map.Entry<String, ArrayList<Integer>> entry: wordSeenInDocMap.entrySet()) {
//            if (entry.getValue().size() > 5) {
//                terms.add(entry.getKey());
//            }
//        }
//
//        numDoc = i;
//    }
//
//    public void buildTxDMatrix() throws Exception {
//        TxDMatrix = DoubleMatrix.zeros(terms.size(), numDoc);
//        for (int i=0; i<terms.size(); i++) {
//            for (int j : wordSeenInDocMap.get(terms.get(i))) {
//                try {
//                    TxDMatrix.put(i, j, getCount(terms.get(i), docIndexMap.get(j)));
//                } catch(ArrayIndexOutOfBoundsException e) {
//                    System.err.println(i+"-"+j);
//                }
//            }
//            if (i % 1000 == 0) { System.out.println("Processed "+i+" terms"); }
//        }
//    }
//
//    private double getCount (String vocab, ArrayList<String> terms) {
//        double count = 0.0;
//        for (String t: terms) {
//            if (vocab.equals(t)) {
//                count = count+1.0;
//            }
//        }
//        return count;
//    }
//
//    public void saveTxDMatrix () throws Exception {
//        saveTxDMatrix(EventQAConfig.getInstance().getProperty("term.doc.file"));
//    }
//
//    public void saveTxDMatrix (String fileName) throws Exception {
//
//        PrintWriter writer = new PrintWriter(new FileWriter(fileName));
//
//        for (int i=0; i<terms.size(); i++) {
//            double[] row = new double[numDoc];
//
//            for (int j=0; j<row.length; j++) { row[j] = 0.0; }
//
//            for (int j: wordSeenInDocMap.get(terms.get(i))) {
//                row[j] = getCount(terms.get(i), docIndexMap.get(j));
//            }
//
//            for (int j=0; j<row.length; j++) {
//                if (j == row.length-1) {
//                    writer.print(row[j]);
//                } else {
//                    writer.print(row[j]+",");
//                }
//            }
//
//            if (i%100 == 0) { System.out.println("Processed "+i+" terms"); }
//            writer.println();
//            writer.flush();
//        }
//        writer.close();
//    }
//
//    public void printCorpusStats () throws Exception { printCorpusStats(false); }
//
//    public void printCorpusStats (boolean wordsSeenInDocON) throws IOException {
//
//        System.out.println("********** Printing Corpus Statistics **********");
//        System.out.println("TxD Map Size: "+wordSeenInDocMap.size());
//
//        if (wordsSeenInDocON) {
//            for (Entry<String, ArrayList<Integer>> entry: wordSeenInDocMap.entrySet()) {
//                StringBuilder integerSet = new StringBuilder("");
//                for (Integer i: entry.getValue()) {
//                    if (integerSet.length() == 0)
//                        integerSet.append(i);
//                    else
//                        integerSet.append(","+i);
//                }
//                System.out.println(entry.getKey()+" ==> "+integerSet);
//            }
//        }
//
//        System.out.println("Total Number of Terms: "+terms.size());
//        System.out.println("Total Number of Docs: "+numDoc);
//        System.out.println("************************************************");
//    }
//
//    public void saveFreqTerms () throws Exception {
//        saveTxDMatrix(EventQAConfig.getInstance().getProperty("vocab.file"));
//    }
//
//    public void saveFreqTerms (String fileName) throws Exception {
//        PrintWriter writer = new PrintWriter(new FileWriter(fileName));
//        for (String word : terms) {
//            writer.println(word);
//        }
//        writer.flush();
//        writer.close();
//    }
//
//    public DoubleMatrix getTxDMatrix () { return TxDMatrix; }

    public static void main (String[] args) {
        try {
            Training trainer = new Training();
            long a = System.currentTimeMillis();
            trainer.load();
            long b = System.currentTimeMillis();
            trainer.save();
            long c = System.currentTimeMillis();
            trainer.printCorpusStats();
            System.out.println("Building time: "+(b-a)+"ms");
            System.out.println("Saving time: "+(c-b)+"ms");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
