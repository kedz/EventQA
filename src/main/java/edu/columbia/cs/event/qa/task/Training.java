package edu.columbia.cs.event.qa.task;

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

    public void loadTrainingData () throws IOException { loadTrainingData("/Users/wojo/Documents/eventQA/resources/QApair_text_training_Full.txt"); }

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
            if (i > 300) break;
        }

        //PrintWriter writer = new PrintWriter(new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/allTerms2007.txt"));

        for (Map.Entry<String, ArrayList<Integer>> entry: wordSeenInDocMap.entrySet()) {
            if (entry.getValue().size() > 5) {
                terms.add(entry.getKey());
                //writer.println(e.getKey());
            }
        }
        //writer.flush();
        //writer.close();

        numDoc = i;

        System.out.println("Total number of terms: "+terms.size());
        System.out.println("Total number of documents: "+numDoc);
    }

    public void buildTxDMatrix(boolean saveTxDMatrixOn) throws Exception {

        //PrintWriter writer = new PrintWriter(new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/MatrixFull2805.txt"));

        for (int i=0; i<terms.size(); i++) {
            double[][] row = new double[1][numDoc];

            for (int j=0; j<row[0].length; j++) { row[0][j] = 0.0; }

            for (int j: wordSeenInDocMap.get(terms.get(i))) {
                row[0][j] = getCount(terms.get(i), docIndexMap.get(j));
            }

            if (saveTxDMatrixOn) {
                for (int j=0; j<row[0].length; j++) {
                    if (j == row[0].length-1) {
                        //writer.print(row[0][k]);
                    } else {
                        //writer.print(row[0][k]+",");
                    }
                }
            }

            if (i%100 == 0) { System.out.println("Processed "+i+" terms"); }
            //writer.println();
            //writer.flush();
        }
        //writer.close();
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
            if (i % 100 == 0) { System.out.println("Processed "+i+" terms"); }
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

    public void printCorpusStats(boolean wordsSeenInDocON) throws IOException {

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

        System.out.println("Total Vocab Size: "+terms.size());
        System.out.println("Total Number of Docs: "+numDoc);
        System.out.println("************************************************");
    }
}
