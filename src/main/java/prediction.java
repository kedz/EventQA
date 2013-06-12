import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import edu.columbia.cs.event.qa.util.StopWordFilter;
import org.jblas.DoubleMatrix;

public class prediction {

    static String delims = " \r\n\t()";
    static ArrayList<String> terms = new ArrayList<String>();
    static DoubleMatrix Space;

    static void readTerms (String termFileName) throws Exception {
        System.out.println("Adding terms");
        BufferedReader reader = new BufferedReader(new FileReader(termFileName));
        String line;
        while((line = reader.readLine()) != null) {
            terms.add(line);
        }
        reader.close();
    }

    // TODO Speedup -
    static double[] transform (ArrayList<String> str) {

        double[] queryArr = new double[terms.size()];

        for (int i=0; i<terms.size(); i++) {
            if (str.contains(terms.get(i))) {
                queryArr[i] = getCount(terms.get(i), str);
            } else {
                queryArr[i] = 0.0;
            }
        }

        return queryArr;
    }

    static double getCount(String vocab, ArrayList<String> terms) {
        double count=0.0;

        for (String t: terms)
        {
            if (vocab.equals(t))
            {
                count=count+1.0;
            }
        }

        return count;
    }

    static ArrayList<String> preprocessDoc (String s) {
        String DOC = s.replace("\"", "");
        DOC = manageMappings.replaceTokens(DOC);
        DOC = DOC.replaceAll("'s", "");
        DOC = DOC.replaceAll("\n", " ");
        DOC = DOC.replaceAll("\\s+", " ");
        DOC = DOC.replaceAll("\\s$", "");
        DOC = DOC.replaceAll("^\\s", "");
        DOC = DOC.replaceAll("\\p{Punct}", " ");
        DOC = DOC.toLowerCase();
        ArrayList<String> allTokens = Split(DOC);

        return allTokens;
    }

    static ArrayList<String> Split(String str) {   		//Extract Tokens into a ArrayList
        ArrayList<String> strTokens = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delims, true);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (!s.trim().equals(""))
            {
                s= StopWordFilter.filter(s);
                s=Stem.stemmer(s);
                if (!s.equals(""))
                {
                    strTokens.add(s);
                }
            }
        }
        return strTokens;
    }

    static double dotprod(double[] d1, double[] d2) {

        double sum=0;
        for (int i=0;i<d1.length;i++)
        {
            sum += d1[i]*d2[i];
        }
        return sum;
    }

    static double length(double[] d) {
        double sum=0;
        for (int i=0;i<d.length;i++)
        {
            sum += d[i]*d[i];
        }
        return Math.sqrt(sum);
    }

    public static double getSimilaritybwDocs (double[] d1, double[] d2) //Ecach document pair
    {
        double sim=dotprod(d1, d2) / (length(d1)*length(d2));
        return sim;
    }

    static void readSpace(String spaceFileName, int k) throws Exception {
        System.out.println("Reading Semantic Space");
        Space = DoubleMatrix.zeros(k, terms.size()); //new double[1000][terms.size()];
        FileReader fR=new FileReader(spaceFileName);
        BufferedReader bR=new BufferedReader(fR);

        String data=null;
        int cnt=0;
        while((data=bR.readLine())!=null)
        {
            String[] tfs=data.substring(0, data.length()-1).split(",");
            for (int i=0;i<tfs.length;i++)
            {
                //Space[cnt][i]=Double.parseDouble(tfs[i]);
                try{
                    Space.put(cnt, i, Double.parseDouble(tfs[i]));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    System.out.println(tfs[i]);
                    System.exit(0);//xit
                }
            }
            cnt++;
        }
        System.out.println("Done Reading Space");
    }

    static double[] foldin(double[] str)
    {
        DoubleMatrix strDM=new DoubleMatrix(str);
        return Space.mmul(strDM).toArray();
    }

    static ArrayList<String> sentenceSplitter(String source)
    {
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

    static void getFinalSimilarityScoreParaLevel(String trainWekaFile, String QASet, String testWekaFile) throws Exception
    {
        PrintWriter writer = new PrintWriter(new FileWriter(testWekaFile));

        writer.println("@relation event");
        writer.println("@attribute cosine numeric");
        writer.println("@attribute class {yes, no}");
        writer.println();
        writer.println("@data");
        writer.flush();

        // TODO Use different delimeter - readNewsblasterXML
        // Q[0] = event query (article title)
        // Q[1]+ = candidate sentence
        String[] QA = QASet.split("`");

        // Preprocessing step + tokenizing string
        // Transform to vector representation
        //
        double[] q = foldin(transform(preprocessDoc(QA[0])));

        ArrayList<Double> sims = new ArrayList<Double>();

        for (int i=1; i<QA.length; i++) {
            double[] ans = foldin(transform(preprocessDoc(QA[i])));
            double similarity = getSimilaritybwDocs(q, ans);
            //System.out.println(similarity);
            sims.add(similarity);
            writer.println(similarity+",yes");
            writer.flush();
        }
        writer.flush();
        writer.close();

        System.out.println("**Query:"+QA[0]);
        int c = 1;

        // TODO Load parameters instead of training classifier each time (getPredictedClass)

        for(String s: wekaSVM.getPredictedClass(trainWekaFile, testWekaFile)) {
            System.out.println("**Ans"+c+":"+QA[c]);
            System.out.println("Similarity:"+sims.get(c-1));
            System.out.println("Prediction:"+s);
            c++;
        }
    }

    static void getFinalSimilarityScoreSentenceLevel(String trainFName, String QASet, String testFName) throws Exception
    {
        System.out.println("Get Summarization");
        FileWriter fW=new FileWriter(testFName);
        PrintWriter pW=new PrintWriter(fW);

        pW.println("@relation event");
        pW.println("@attribute cosine numeric");
        pW.println("@attribute class {yes, no}");
        pW.println();

        pW.println("@data");
        pW.flush();
        String[] QA=QASet.split("`");

        double[] q=foldin(transform(preprocessDoc(QA[0])));
        ArrayList<String> actualStr=new ArrayList<String>();
        ArrayList<Double> sims=new ArrayList<Double>();

        int cnt=0;
        for (int i=1;i<QA.length;i++)
        {
            for (String sentence: sentenceSplitter(QA[i]))
            {
                cnt++;
                double[] ans=foldin(transform(preprocessDoc(sentence)));
                double similarity=getSimilaritybwDocs(q, ans);
                if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
                {
                    actualStr.add(sentence);
                    sims.add(similarity);
                    pW.println(similarity+",yes");
                    pW.flush();
                }
            }
        }
        pW.flush();
        pW.close();
        fW.close();

        System.out.println("Total number of sentences seen: "+cnt);
        System.out.println("Valid sentences: "+actualStr.size());

        StringBuilder summary=new StringBuilder();
        StringBuilder notChosen=new StringBuilder();
        ArrayList<String> predictions=wekaSVM.getPredictedClass(trainFName, testFName);
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

    public static void main (String args[]) throws Exception {

        // TODO Load eventQA properties
        String vocabFileName = args[0];
        String spaceFileName = args[1];
        String testInputFileName = args[2];
        String trainWekaFile = args[3];
        String testWekaFile = args[4];

        int eigenVecs = Integer.parseInt(args[5]);
        int mode = Integer.parseInt(args[6]);

        readTerms(vocabFileName);
        readSpace(spaceFileName, eigenVecs);

        BufferedReader reader = new BufferedReader(new FileReader(testInputFileName));
		
		/*
		readTerms("/bolt/ir2/columbia/EventQA/training_Vocab_Full.txt");
		readSpace("/bolt/ir2/columbia/EventQA/Space.txt", eigenVecs);
		BufferedReader bR=new BufferedReader(new FileReader("/bolt/ir2/columbia/EventQA/Test_QA_Input.txt"));

		String trainfName="/bolt/ir2/columbia/EventQA/train.arff";		
		String testFName="/bolt/ir2/columbia/EventQA/test.arff";
		*/

        String QASet = "";
        while((QASet = reader.readLine()) != null) {
            if (mode == 1) {
                getFinalSimilarityScoreParaLevel(trainWekaFile, QASet, testWekaFile);
            } else if (mode == 2) {
                getFinalSimilarityScoreSentenceLevel(trainWekaFile, QASet, testWekaFile);
            } else {
                System.out.println("Enter correct processing level: (1) for Paragraph level and (2) for Sentence level");
                System.exit(1);
            }
            System.out.println("****************************************************");
        }
    }
}