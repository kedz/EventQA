package edu.columbia.cs.event.qa.task;

import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import edu.columbia.cs.event.qa.util.FileLoader;
import java.util.*;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 6/13/13
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */

public class BuildTxDMatrix {

    private List<HashMap<String,Integer>> documents;
    private Map<String,Integer> terms;
    private boolean sparseOn;
    private int numTerms;

    public BuildTxDMatrix() {
        documents = new ArrayList<HashMap<String, Integer>>();
        terms = new HashMap<String,Integer>();
        sparseOn = true;
    }

    public void load () throws IOException {
        Object[] tmp = FileLoader.newInstance().loadCorpus();
        this.terms = (HashMap<String,Integer>) tmp[0];
        this.documents = (ArrayList<HashMap<String,Integer>>) tmp[1];
    }

    public void save () throws IOException {
        save(ProjectConfiguration.getInstance().getProperty("vocab.file"), ProjectConfiguration.getInstance().getProperty("term.doc.file"));
    }

    public void save (String termFileName, String termByDocFileName) throws IOException {

        PrintWriter writer1 = new PrintWriter(new FileWriter(termFileName));
        PrintWriter writer2 = new PrintWriter(new FileWriter(termByDocFileName));

        int i = 0;

        for (Map.Entry<String,Integer> entry : terms.entrySet()) {
            String word = entry.getKey();
            if (entry.getValue() > 5) {
                writer1.println(word);
                Iterator<HashMap<String,Integer>> iter = documents.iterator();
                int j = 0;
                while (iter.hasNext()) {
                    HashMap<String, Integer> wordCount = iter.next();
                    if (sparseOn) {
                        if (wordCount.containsKey(word)) {
                            writer2.println((i+1)+","+(j+1)+","+wordCount.get(word));
                        }
                    } else {
                        if (wordCount.containsKey(word)) {
                            writer2.print(wordCount.get(word));
                        } else {
                            writer2.print("0");
                        }
                        if (iter.hasNext()) { writer2.print(","); }
                        else { writer2.print("\n"); }
                    }
                    j++;
                }
                if (i%100 == 0) { System.out.println("Processed "+i+" terms"); }
                i++;
            }
        }

        numTerms = i;

        writer1.flush(); writer1.close();
        writer2.flush(); writer2.close();
    }

    public void printCorpusStats () {
        System.out.println("********** Printing Corpus Statistics **********");
        System.out.println("Total # of Terms: "+numTerms);
        System.out.println("Total # of Docs: "+documents.size());
        System.out.println("************************************************");
    }

    public void run () {
        try {
            long a = System.currentTimeMillis();    load();
            long b = System.currentTimeMillis();    save();
            long c = System.currentTimeMillis();    printCorpusStats();
            System.out.println("Building time: "+(b-a)+"ms");
            System.out.println("Saving time: "+(c-b)+"ms");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main (String[] args) {
        (new BuildTxDMatrix()).run();
    }
}
