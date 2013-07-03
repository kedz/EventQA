package edu.columbia.cs.event.qa.util;

import org.jblas.DoubleMatrix;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 6/24/13
 * Time: 10:56 AM
 * To change this template use File | Settings | File Templates.
 */

public class FileLoader {

    private String corpusFileName;
    private String termFileName;
    private String spaceFileName;
    private String trainXMLFileName;
    private String testXMLFileName;
    private String testInputFileName;
    private String trainWekaFile;
    private String testWekaFile;
    private int numEigenVectors;
    private int numTerms;
    private int numDocs;
    private int mode;

    private static FileLoader FileLoader;

    public static FileLoader newInstance() {
        if(FileLoader == null)
            FileLoader = new FileLoader();
        return FileLoader;
    }

    public FileLoader() {
        ProjectConfiguration config = ProjectConfiguration.getInstance();
        this.corpusFileName = config.getProperty("corpus.file");
        this.termFileName = config.getProperty("terms.file");
        this.spaceFileName = config.getProperty("semantic.space.file");
        this.trainXMLFileName = config.getProperty("train.xml.file");
        this.testXMLFileName = config.getProperty("test.xml.file");
        this.testInputFileName = config.getProperty("test.file");
        this.trainWekaFile = config.getProperty("weka.training.file");
        this.testWekaFile = config.getProperty("weka.testing.file");
        this.numEigenVectors = Integer.parseInt(config.getProperty("number.of.eigenvectors"));
        this.numTerms = Integer.parseInt(config.getProperty("number.of.terms"));
        this.numDocs = Integer.parseInt(config.getProperty("number.of.docs"));
        this.mode = Integer.parseInt(config.getProperty("mode"));
    }

    public HashSet<String> loadStopWords (String stopWordsFile) throws IOException {
        HashSet<String> stopwords = new HashSet<String>();
        InputStream input = getClass().getResourceAsStream(stopWordsFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            stopwords.add(line.trim());
        }
        input.close();
        reader.close();
        return stopwords;
    }

    public HashMap<String,String> loadReplaceWords (String fileName) throws IOException {
        HashMap<String,String> replacewords = new HashMap<String, String>();
        InputStream input = getClass().getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] pair = line.trim().split("->");
            if (pair.length > 1){
                replacewords.put(pair[0], pair[1]);
            } else {
                replacewords.put(pair[0], "");
            }
        }
        input.close();
        reader.close();
        return replacewords;
    }

    public Object[] loadCorpus (String fileName) throws IOException {
        ArrayList<HashMap<String,Integer>> documents = new ArrayList<HashMap<String, Integer>>();
        HashMap<String,Integer> terms = new HashMap<String,Integer>();
        String line; int i = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while ((line = reader.readLine()) != null) {
            ArrayList<String> tokenList = SimplePreprocessor.newInstance().forge(line);
            if (tokenList.size() > 0) {
                HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
                for (String token : tokenList) {
                    int count = 0;
                    if (wordCount.containsKey(token)) {
                        count = wordCount.get(token);
                    } else {
                        int dount = 0;
                        if (terms.containsKey(token)) {
                            dount = terms.get(token);
                        }
                        terms.put(token, dount+1);
                    }
                    wordCount.put(token, count+1);
                }
                documents.add(wordCount);
                i++;
            }
        }
        return new Object[] {terms, documents};
    }

    public ArrayList<String> loadTerms (String fileName) throws IOException {
        ArrayList<String> terms = new ArrayList<String>();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while((line = reader.readLine()) != null) {
            terms.add(line);
        }
        reader.close();
        return terms;
    }

    public DoubleMatrix loadSemanticSpace (String fileName) throws IOException {
        DoubleMatrix semanticSpace = DoubleMatrix.zeros(numEigenVectors, numTerms);
        String line; int i = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while((line = reader.readLine()) != null) {
            String[] scores = line.substring(0, line.length()-1).split(",");
            for (int j=0; j<scores.length; j++) {
                try {
                    semanticSpace.put(i, j, Double.parseDouble(scores[j]));
                } catch(Exception e) {
                    System.out.println(scores[j]);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            i++;
        }
        return semanticSpace;
    }

    public Document loadXMLData (String fileName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File (fileName));
        doc.getDocumentElement().normalize();
        return doc;
    }

    public HashSet<String> loadStopWords () throws IOException { return loadStopWords("/stopwords.txt"); }
    public HashMap<String,String> loadReplaceWords () throws IOException { return loadReplaceWords("/replacements.txt"); }
    public Object[] loadCorpus () throws IOException { return loadCorpus(corpusFileName); }
    public ArrayList<String> loadTerms () throws IOException { return loadTerms(termFileName); }
    public DoubleMatrix loadSemanticSpace () throws IOException { return loadSemanticSpace(spaceFileName); }

    public void setCorpusFileName (String fileName) { this.corpusFileName = fileName; }
    public void setTermFileName (String fileName) { this.termFileName = fileName; }
    public void setSpaceFileName (String fileName) { this.spaceFileName = fileName; }
    public void setTestInputFileName (String fileName) { this.testInputFileName = fileName; }
    public void setTrainWekaFile (String fileName) { this.trainWekaFile = fileName; }
    public void setTestWekaFile (String fileName) { this.testWekaFile = fileName; }
    public void setNumEigenVectors (int num) { this.numEigenVectors = num; }
    public void setNumTerms (int num) { this.numTerms = num; }
    public void setNumDocs (int num) { this.numDocs = num; }
    public void setMode (int num) { this.mode = num; }
}
