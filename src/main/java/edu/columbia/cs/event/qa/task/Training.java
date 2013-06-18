package edu.columbia.cs.event.qa.task;

import edu.columbia.cs.event.qa.util.EventQAConfig;
import edu.columbia.cs.event.qa.util.Preprocessor;
import org.jblas.DoubleMatrix;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
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

    private HashMap<Integer, ArrayList<String>> docIndexMap;
    private HashMap<String, ArrayList<Integer>> wordSeenInDocMap;
    private ArrayList<String> terms;

    private DoubleMatrix TxDMatrix;
    private int numDoc;

    public Training () {
        preprocessor = new Preprocessor();
        docIndexMap = new HashMap<Integer,ArrayList<String>>();
        wordSeenInDocMap = new HashMap<String, ArrayList<Integer>>();
        terms = new ArrayList<String>();
        numDoc = 0;
    }

    public void loadTrainingData () throws IOException {
        loadTrainingData(EventQAConfig.getInstance().getProperty("corpus.file"));
    }

    public void loadTrainingData (String fileName) throws IOException {

        String line; int i = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while ((line = reader.readLine()) != null) {

            ArrayList<String> tokenList = preprocessor.run(line);
            if (tokenList.size()>0) {
                docIndexMap.put(i, tokenList);

                for (String word : tokenList) {
                    if (wordSeenInDocMap.containsKey(word)) {
                        ArrayList<Integer> existingDocs = wordSeenInDocMap.get(word);

                        if (!existingDocs.contains(i)) {
                            existingDocs.add(i);
                            wordSeenInDocMap.put(word, existingDocs);
                        }

                    } else {
                        ArrayList<Integer> existingDocs = new ArrayList<Integer>();
                        existingDocs.add(i);
                        wordSeenInDocMap.put(word, existingDocs);
                    }
                }
            }
            if (i%100 == 0) { System.out.println("Read "+i+" documents"); } i++;
            if (i > 100) break;
        }

        for (Map.Entry<String, ArrayList<Integer>> entry: wordSeenInDocMap.entrySet()) {
            if (entry.getValue().size() > 5) {
                terms.add(entry.getKey());
            }
        }

        numDoc = i;
    }

    public void buildTxDMatrix() throws Exception {
        TxDMatrix = DoubleMatrix.zeros(terms.size(), numDoc);
        for (int i=0; i<terms.size(); i++) {
            for (int j : wordSeenInDocMap.get(terms.get(i))) {
                try {
                    TxDMatrix.put(i, j, getCount(terms.get(i), docIndexMap.get(j)));
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.err.println(i+"-"+j);
                }
            }
            if (i % 1000 == 0) { System.out.println("Processed "+i+" terms"); }
        }
    }

    private double getCount (String vocab, ArrayList<String> terms) {
        double count = 0.0;
        for (String t: terms) {
            if (vocab.equals(t)) {
                count = count+1.0;
            }
        }
        return count;
    }

    public void saveTxDMatrix () throws Exception {
        saveTxDMatrix(EventQAConfig.getInstance().getProperty("term.doc.file"));
    }

    public void saveTxDMatrix (String fileName) throws Exception {

        PrintWriter writer = new PrintWriter(new FileWriter(fileName));

        for (int i=0; i<terms.size(); i++) {
            double[] row = new double[numDoc];

            for (int j=0; j<row.length; j++) { row[j] = 0.0; }

            for (int j: wordSeenInDocMap.get(terms.get(i))) {
                row[j] = getCount(terms.get(i), docIndexMap.get(j));
            }

            for (int j=0; j<row.length; j++) {
                if (j == row.length-1) {
                    writer.print(row[j]);
                } else {
                    writer.print(row[j]+",");
                }
            }

            if (i%1000 == 0) { System.out.println("Processed "+i+" terms"); }
            writer.println();
            writer.flush();
        }
        writer.close();
    }

    public void printCorpusStats () throws Exception { printCorpusStats(false); }

    public void printCorpusStats (boolean wordsSeenInDocON) throws IOException {

        System.out.println("********** Printing Corpus Statistics **********");
        System.out.println("TxD Map Size: "+wordSeenInDocMap.size());

        if (wordsSeenInDocON) {
            for (Entry<String, ArrayList<Integer>> entry: wordSeenInDocMap.entrySet()) {
                StringBuilder integerSet = new StringBuilder("");
                for (Integer i: entry.getValue()) {
                    if (integerSet.length() == 0)
                        integerSet.append(i);
                    else
                        integerSet.append(","+i);
                }
                System.out.println(entry.getKey()+" ==> "+integerSet);
            }
        }

        System.out.println("Total Number of Terms: "+terms.size());
        System.out.println("Total Number of Docs: "+numDoc);
        System.out.println("************************************************");
    }

    public void saveFreqTerms () throws Exception {
        saveTxDMatrix(EventQAConfig.getInstance().getProperty("vocab.file"));
    }

    public void saveFreqTerms (String fileName) throws Exception {
        PrintWriter writer = new PrintWriter(new FileWriter(fileName));
        for (String word : terms) {
            writer.println(word);
        }
        writer.flush();
        writer.close();
    }

    public DoubleMatrix getTxDMatrix () { return TxDMatrix; }

    public static void main (String[] args) {
        try {
            Training trainer = new Training();
            long a = System.currentTimeMillis();
            trainer.loadTrainingData();
            long b = System.currentTimeMillis();
            trainer.buildTxDMatrix();
            long c = System.currentTimeMillis();
            //trainer.saveFreqTerms();
            trainer.printCorpusStats();
            System.out.println("Loading time: "+(b-a)+"ms");
            System.out.println("Building time: "+(c-b)+"ms");
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
