package edu.columbia.cs.event.qa.task;

import edu.columbia.cs.event.qa.util.LoadMachine;
import edu.columbia.cs.event.qa.util.ProjectConfiguration;

import java.util.*;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 6/13/13
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */

public class BuildTermByDocumentMatrix {

    private List<HashMap<String,Integer>> documents;
    private Map<String,Integer> terms;
    private int numFreqTerms;

    public BuildTermByDocumentMatrix() {
        documents = new ArrayList<HashMap<String, Integer>>();
        terms = new HashMap<String,Integer>();
    }

    public void load () throws IOException {
        Object[] tmp = LoadMachine.newInstance().loadCorpus();
        this.terms = (HashMap<String,Integer>) tmp[0];
        this.documents = (ArrayList<HashMap<String,Integer>>) tmp[1];
    }

    public void loadNewsblasterCorpus (String directory)  {
        this.documents = LoadMachine.newInstance().loadNewsblasterCorpus(directory);
        this.terms = documents.remove(documents.size()-1);
    }

    public void save () throws IOException {
        save(ProjectConfiguration.newInstance().getProperty("terms.file"), ProjectConfiguration.newInstance().getProperty("term.doc.file"));
    }

    public void save (String termFileName, String termByDocFileName) throws IOException {

        PrintWriter writer1 = new PrintWriter(new FileWriter(termFileName));
        PrintWriter writer2 = new PrintWriter(new FileWriter(termByDocFileName));

        int i = 0;
        System.out.println("***************************** BUILD *****************************");
        System.out.println("[ ARTICLES = "+documents.size()+" ]");
        System.out.println("[ TERMS = "+terms.size()+" ]");

        for (Map.Entry<String,Integer> entry : terms.entrySet()) {
            String word = entry.getKey();
            if (entry.getValue() > 4) {
                writer1.println(word);
                Iterator<HashMap<String,Integer>> iter = documents.iterator();
                int j = 0;
                while (iter.hasNext()) {
                    HashMap<String, Integer> wordCount = iter.next();
                    if (wordCount.containsKey(word)) {
                        writer2.println((i+1)+","+(j+1)+","+wordCount.get(word));
                    }
                    j++;
                }
                if (i%100 == 0) {
                    System.out.println("[ PROCESSED: "+i+" TERMS ]");
                }
                i++;
            }
        }

        numFreqTerms = i;

        writer1.flush(); writer1.close();
        writer2.flush(); writer2.close();
    }

    public void printCorpusStats () {
        System.out.println("*********************** Corpus Statistics ***********************");
        System.out.println("Total # of Terms: "+numFreqTerms);
        System.out.println("Total # of Docs: "+documents.size());
        System.out.println("*****************************************************************");
    }

    public void run () {
        String dir = "/Users/wojo/Documents/eventQA/resources/";
        try {
            long a = System.currentTimeMillis();
            loadNewsblasterCorpus(dir + "nb_corpus/distillation/");
            long b = System.currentTimeMillis();
            save(dir+"nb_terms_03_13.txt", dir+"nb_txd_03_13.csv");
            long c = System.currentTimeMillis();
            printCorpusStats();
            System.out.println("Building time: "+(b-a)+"ms");
            System.out.println("Saving time: "+(c-b)+"ms");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main (String[] args) {
        (new BuildTermByDocumentMatrix()).run();
    }
}
