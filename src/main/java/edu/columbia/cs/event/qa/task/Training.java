package edu.columbia.cs.event.qa.task;

import edu.columbia.cs.event.qa.util.EventQAConfig;
import edu.columbia.cs.event.qa.util.Preprocessor;
//import org.jblas.DoubleMatrix;
import java.util.*;
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
    private List<HashMap<String,Integer>> documents;
    private Set<String> terms;

    public Training () {
        preprocessor = new Preprocessor();
        documents = new ArrayList<HashMap<String, Integer>>();
        terms = new HashSet<String>();
    }

    public void run () {
        try {
            long a = System.currentTimeMillis();
            load();
            long b = System.currentTimeMillis();
            save();
            long c = System.currentTimeMillis();
            printCorpusStats();
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
                    } else {
                        terms.add(token);
                    }
                    wordCount.put(token, count+1);
                }

                documents.add(wordCount);

                if (i%1000 == 0) { System.out.println("Read "+i+" documents"); }
                i++;
            }
        }
    }

    public void save () throws IOException {
        save(EventQAConfig.getInstance().getProperty("vocab.file"), EventQAConfig.getInstance().getProperty("term.doc.file"));
    }

    public void save (String termFileName, String termByDocFileName) throws IOException {

        PrintWriter writer1 = new PrintWriter(new FileWriter(termFileName));
        PrintWriter writer2 = new PrintWriter(new FileWriter(termByDocFileName));

        int i = 0;

        for (String word : terms) {
            writer1.println(word);
            Iterator<HashMap<String,Integer>> iter = documents.iterator();
            while (iter.hasNext()) {
                HashMap<String, Integer> wordCount = iter.next();
                if (wordCount.containsKey(word)) {
                    writer2.print(wordCount.get(word));
                } else {
                    writer2.print("0");
                }
                if (iter.hasNext()) { writer2.print(","); }
                else { writer2.print("\n"); }
            }
            if (i%100 == 0) { System.out.println("Processed "+i+" terms"); }
            i++;
        }

        writer1.flush(); writer1.close();
        writer2.flush(); writer2.close();
    }

    public void printCorpusStats () {
        System.out.println("********** Printing Corpus Statistics **********");
        System.out.println("Total # of Terms: "+terms.size());
        System.out.println("Total # of Docs: "+documents.size());
        System.out.println("************************************************");
    }

    public static void main (String[] args) {
        (new Training()).run();
    }
}
