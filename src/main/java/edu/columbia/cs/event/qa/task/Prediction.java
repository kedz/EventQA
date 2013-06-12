package edu.columbia.cs.event.qa.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import edu.columbia.cs.event.qa.ManageMappings;
import edu.columbia.cs.event.qa.Stem;
import edu.columbia.cs.event.qa.WekaSMOClassifierFactory;
import edu.columbia.cs.event.qa.WekaSMOClassifier;
import edu.columbia.cs.event.qa.util.EventQAConfig;
import edu.columbia.cs.event.qa.util.StopWordFilter;
import org.jblas.DoubleMatrix;

public class Prediction {

    static String delims = " \r\n\t()";

    private String termFileName;
    private String spaceFileName;
    private String testInputFileName;
    private String trainWekaFile;
    private String testWekaFile;
    private int numEigenVectors;
    private int mode;

    public WekaSMOClassifier classifier;
    public ArrayList<String> terms;
    public DoubleMatrix semanticSpace;


    public Prediction () {
        loadConfigurations();
        this.terms = new ArrayList<String>();
    }

    public void load () throws Exception {
        System.out.print("Loading Terms Vector... ");
        loadTerms();
        System.out.print("Done\nLoading Semantic Space Matrix... ");
        loadSemanticSpaceMatrix();
        System.out.print("Done\nLoading Classifier... ");
        loadClassifier();
        System.out.println("Done");
    }

    private void loadConfigurations () {
        EventQAConfig config = EventQAConfig.getInstance();
        this.termFileName = config.getProperty("vocab.file");
        this.spaceFileName = config.getProperty("semantic.space.file");
        this.testInputFileName = config.getProperty("test.file");
        this.trainWekaFile = config.getProperty("weka.training.file");
        this.testWekaFile = config.getProperty("weka.testing.file");
        this.numEigenVectors = Integer.parseInt(config.getProperty("number.of.eigenvectors"));
        this.mode = Integer.parseInt(config.getProperty("mode"));
    }

    public void loadClassifier () throws Exception {
        this.classifier = (new WekaSMOClassifierFactory()).getWekaSMOClassifier();
    }

    public void loadTerms () throws Exception {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(termFileName));
        while((line = reader.readLine()) != null) {
            terms.add(line);
        }
        reader.close();
    }

    public void loadSemanticSpaceMatrix() throws Exception {

        semanticSpace = DoubleMatrix.zeros(numEigenVectors, terms.size()); //new double[1000][terms.size()];

        String line; int c = 0;
        BufferedReader reader = new BufferedReader(new FileReader(spaceFileName));
        while((line = reader.readLine())!=null) {

            String[] scores = line.substring(0, line.length()-1).split(",");
            for (int i=0; i<scores.length; i++) {
                try {
                    semanticSpace.put(c, i, Double.parseDouble(scores[i]));
                } catch(Exception e) {
                    System.out.println(scores[i]);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            c++;
        }
    }

    private ArrayList<String> preprocess (String s) {
        s = s.replace("\"", "");
        s = ManageMappings.replaceTokens(s);
        s = s.replaceAll("'s", "");
        s = s.replaceAll("\n", " ");
        s = s.replaceAll("\\s+", " ");
        s = s.replaceAll("\\s$", "");
        s = s.replaceAll("^\\s", "");
        s = s.replaceAll("\\p{Punct}", " ");
        s = s.toLowerCase();
        return tokenize(s);
    }

    private ArrayList<String> tokenize (String s) {
        ArrayList<String> tokens = new ArrayList<String>();
        StringTokenizer stokenizer = new StringTokenizer(s, delims, true);
        while (stokenizer.hasMoreTokens()) {
            String tok = stokenizer.nextToken();
            if (!tok.trim().equals("")) {
                tok = StopWordFilter.filter(tok);
                tok = Stem.stemmer(tok);
                if (!tok.equals("")) {
                    tokens.add(tok);
                }
            }
        }
        return tokens;
    }

    // TODO Speedup
    private double[] transform (ArrayList<String> tokens) {
        double[] termCounts = new double[terms.size()];
        for (int i=0; i<terms.size(); i++) {
            if (tokens.contains(terms.get(i))) {
                termCounts[i] = computeCount(terms.get(i), tokens);
            } else {
                termCounts[i] = 0.0;
            }
        }
        return termCounts;
    }

    private double computeCount (String term, ArrayList<String> tokens) {
        double count = 0.0;
        for (String t : tokens) {
            if (term.equals(t)) { count = count+1.0; }
        }
        return count;
    }

    private double[] foldin (double[] termCounts) {
        return semanticSpace.mmul(new DoubleMatrix(termCounts)).toArray();
    }

    private double computeSimilarityBetweenPair (double[] d1, double[] d2) {
        return dotProduct(d1, d2) / (magnitude(d1)*magnitude(d2));
    }

    private double magnitude (double[] d) {
        double sum=0;
        for (int i=0; i<d.length;i++) { sum += d[i]*d[i]; }
        return Math.sqrt(sum);
    }

    private double dotProduct (double[] d1, double[] d2) {
        double sum = 0;
        for (int i=0; i<d1.length; i++) { sum += d1[i]*d2[i]; }
        return sum;
    }

    public void computeSimilarityScore (String QAset) throws Exception {
        if (mode == 1) {
            computeSimilarityScoreParagraphLevel(QAset);
        } else if (mode == 2) {
            System.out.println("Get Summarization");
            computeSimilarityScoreSentenceLevel(QAset);
        } else {
            System.out.println("Enter correct processing level: (1) for Paragraph level and (2) for Sentence level");
            System.exit(1);
        }
    }

    public void computeSimilarityScoreParagraphLevel(String QASet) throws Exception {
        PrintWriter writer = new PrintWriter(new FileWriter(testWekaFile));
        writer.println("@relation event");
        writer.println("@attribute cosine numeric");
        writer.println("@attribute class {yes, no}");
        writer.println();
        writer.println("@data");
        writer.flush();

        // TODO Use different delimeter - readNewsblasterXML
        String[] QA = QASet.split("`");

        double[] query = foldin(transform(preprocess(QA[0])));

        ArrayList<Double> sims = new ArrayList<Double>();

        for (int i=1; i<QA.length; i++) {
            double[] answer = foldin(transform(preprocess(QA[i])));
            double similarity = computeSimilarityBetweenPair(query, answer);
            sims.add(similarity);
            writer.println(similarity+",yes");
            writer.flush();
        }
        writer.flush();
        writer.close();

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

        PrintWriter writer = new PrintWriter(new FileWriter(testWekaFile));
        writer.println("@relation event");
        writer.println("@attribute cosine numeric");
        writer.println("@attribute class {yes, no}");
        writer.println();
        writer.println("@data");
        writer.flush();

        String[] QA = QASet.split("`");

        double[] q = foldin(transform(preprocess(QA[0])));

        ArrayList<String> actualStr=new ArrayList<String>();

        ArrayList<Double> sims=new ArrayList<Double>();

        int cnt=0;
        for (int i=1;i<QA.length;i++)
        {
            for (String sentence: sentenceSplitter(QA[i]))
            {
                cnt++;
                double[] ans=foldin(transform(preprocess(sentence)));
                double similarity=computeSimilarityBetweenPair(q, ans);
                if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
                {
                    actualStr.add(sentence);
                    sims.add(similarity);
                    writer.println(similarity+",yes");
                    writer.flush();
                }
            }
        }
        writer.flush();
        writer.close();

        System.out.println("Total number of sentences seen: "+cnt);
        System.out.println("Valid sentences: "+actualStr.size());

        StringBuilder summary=new StringBuilder();
        StringBuilder notChosen=new StringBuilder();

        ArrayList<String> predictions = classifier.predict(testWekaFile); //WekaSMOClassifier.getPredictedClass(trainFName, testFName);
        if (predictions.size()!=actualStr.size())
        {
            System.out.println("NOT SAME");
            System.out.println(predictions.size()+"!="+actualStr.size());
            System.exit(0);
        }
        else
        {
            for (int i=0;i<predictions.size();i++)
            {
                if (predictions.get(i).equals("yes"))
                {
                    summary.append(actualStr.get(i));
                }
                else
                {
                    notChosen.append(actualStr.get(i));
                }
            }
        }

        for (String s: actualStr)
            System.out.println(s);

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

    public static void main (String args[]) throws Exception {
        Prediction droid = new Prediction();
        droid.load();
        droid.run();
    }
}