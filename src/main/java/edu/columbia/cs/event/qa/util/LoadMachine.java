package edu.columbia.cs.event.qa.util;

import edu.columbia.cs.event.qa.classifier.WekaClassifierInterface;
import edu.columbia.cs.event.qa.cotraining.LabeledData;
import edu.columbia.cs.event.qa.cotraining.QAPair;
import org.jblas.DoubleMatrix;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

public class LoadMachine {

    private String corpusFileName;
    private String termFileName;
    private String spaceFileName;
    private int numEigenVectors;
    private int numTerms;

    private static LoadMachine LoadMachine;

    public static LoadMachine newInstance() {
        if(LoadMachine == null)
            LoadMachine = new LoadMachine();
        return LoadMachine;
    }

    public LoadMachine() {
        ProjectConfiguration config = ProjectConfiguration.newInstance();
        this.corpusFileName = config.getProperty("corpus.file");
        this.termFileName = config.getProperty("terms.file");
        this.spaceFileName = config.getProperty("semantic.space.file");
        this.numEigenVectors = Integer.parseInt(config.getProperty("number.of.eigenvectors"));
        this.numTerms = Integer.parseInt(config.getProperty("number.of.terms"));
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

    public LabeledData loadAmazonMechanicalTurkData (WekaClassifierInterface c1, WekaClassifierInterface c2, String fileName) {
        LabeledData seed = new LabeledData(c1, c2);
        System.out.println("Loading Amazon Mechanical Turk data: "+fileName);
        try {
            Document xmlData = loadXMLFile(fileName);
            NodeList queries = xmlData.getElementsByTagName("Question");
            NodeList answers = xmlData.getElementsByTagName("Response");
            NodeList labels = xmlData.getElementsByTagName("Label");

            for (int i=0; i<queries.getLength(); i++) {
                String label = labels.item(i).getFirstChild().getNodeValue();
                QAPair pair = new QAPair(queries.item(i), answers.item(i), label);
                seed.addInstance(pair);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return seed;
    }

    public ArrayList<HashMap<String,Integer>> loadNewsblasterCorpus (String directory) {

        ArrayList<HashMap<String,Integer>> articles = new ArrayList<HashMap<String, Integer>>();
        HashMap<String,Integer> termCounts = new HashMap<String,Integer>();

        File folder = new File(directory);
        File[] xmlFiles = folder.listFiles();

        for (int i=0; i<xmlFiles.length; i++) {
            try {
                Document document = loadXMLFile(xmlFiles[i].getAbsolutePath());
                System.out.println("************************** DOCUMENT #"+i+" **************************");
                System.out.println("[ "+xmlFiles[i].getName()+" ]");
                ArrayList<HashMap<String,Integer>> localArticles = extractWordsFromDocument(document);
                HashMap<String,Integer> localTermCounts = localArticles.remove(localArticles.size()-1);
                System.out.println("***************************** STATS *****************************");
                articles.addAll(localArticles);
                System.out.println("[ DOCUMENTS = "+i+" ]");
                System.out.println("[ ARTICLES = "+articles.size()+" ]");
                for (String word : localTermCounts.keySet()) {
                    int c = 0;
                    if (termCounts.containsKey(word)) { c = termCounts.get(word); }
                    termCounts.put(word, c+localTermCounts.get(word));
                }
                System.out.println("[ TERMS = "+termCounts.size()+" ]");
            } catch (Exception e) {
                System.err.println("Error: loading XML file: " + xmlFiles[i].getName());
            }
        }
        articles.add(termCounts);
        return articles;
    }

    public ArrayList<HashMap<String,Integer>> extractWordsFromDocument (Document document) {

        ArrayList<HashMap<String,Integer>> articles = new ArrayList<HashMap<String, Integer>>();
        HashMap<String,Integer> termCounts = new HashMap<String,Integer>();

        NodeList summaries = document.getElementsByTagName("Summary");
        if (summaries != null) {
            System.out.println("[ ARTICLES: "+summaries.getLength()+" ]");
            for (int i=0; i<summaries.getLength(); i++) {
                System.out.print(" Summary: "+i+" ");
                Element summary = (Element) summaries.item(i);
                NodeList lemmas = summary.getElementsByTagName("Lemma");
                System.out.println("Lemmas: "+lemmas.getLength());
                if (lemmas != null) {
                    HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
                    for (int j=0; j<lemmas.getLength(); j++) {
                        int c = 0;
                        String word = lemmas.item(j).getFirstChild().getNodeValue();
                        if (wordCount.containsKey(word)) { c = wordCount.get(word); }
                        else {
                            int d = 0;
                            if (termCounts.containsKey(word)) { d = termCounts.get(word); }
                            termCounts.put(word, d+1);
                        }
                        wordCount.put(word, c+1);
                        articles.add(wordCount);
                    }
                } else {
                    System.err.println("Error: No words were found in this summary.");
                }
            }
        } else {
            System.err.println("Error: No summaries were found in this document.");
        }
        System.out.println("[ ARTICLES: "+articles.size()+" ]");
        System.out.println("[ TERMS: "+termCounts.size()+" ]");
        articles.add(termCounts);
        return articles;
    }

    public ArrayList<String> loadTerms (String fileName) throws IOException {
        System.out.println("Loading Terms Vector: "+fileName);
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
        System.out.println("Loading Semantic Space Matrix: "+fileName);
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

    public Document loadXMLFile (String fileName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File (fileName));
        doc.getDocumentElement().normalize();
        return doc;
    }

    public HashSet<String> loadStopWords () throws IOException { return loadStopWords("/stopwords.txt"); }
    public HashMap<String,String> loadReplaceWords () throws IOException { return loadReplaceWords("/replacements.txt"); }
    public ArrayList<String> loadTerms () throws IOException { return loadTerms(termFileName); }
    public DoubleMatrix loadSemanticSpace () throws IOException { return loadSemanticSpace(spaceFileName); }

    public void setCorpusFileName (String fileName) { this.corpusFileName = fileName; }
    public void setTermFileName (String fileName) { this.termFileName = fileName; }
    public void setSpaceFileName (String fileName) { this.spaceFileName = fileName; }
    public void setNumEigenVectors (int num) { this.numEigenVectors = num; }
    public void setNumTerms (int num) { this.numTerms = num; }
}
