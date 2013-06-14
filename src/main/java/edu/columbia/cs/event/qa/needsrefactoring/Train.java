package edu.columbia.cs.event.qa.needsrefactoring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.columbia.cs.event.qa.util.Preprocessor;
import org.jblas.DoubleMatrix;

public class Train {

    static Preprocessor preprocessor;

	static HashMap<String, ArrayList<Integer>> wordDocMapping=new HashMap<String, ArrayList<Integer>>();
	static HashMap<Integer, ArrayList<String>> documents=new HashMap<Integer, ArrayList<String>>();
	static ArrayList<String> vocab=new ArrayList<String>();
	static DoubleMatrix TxDMatrix;
	static int numDoc=0;

    public HashMap<Integer, ArrayList<String>> docIndexMap;
    public HashMap<String, ArrayList<Integer>> wordSeenInDocMap;
    public ArrayList<String> terms;

    public Train () throws IOException {
        load();
    }

    public void load () throws IOException {
        loadTrainingData();
    }

    public void loadTrainingData () throws IOException { loadTrainingData("/Users/wojo/Documents/eventQA/resources/QApair_text_training_Full.txt"); }

    public void loadTrainingData (String fileName) throws IOException {

        preprocessor = new Preprocessor();
        docIndexMap = new HashMap<Integer,ArrayList<String>>();
        wordSeenInDocMap = new HashMap<String, ArrayList<Integer>>();
        terms = new ArrayList<String>();

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

        for (Entry<String, ArrayList<Integer>> entry: wordSeenInDocMap.entrySet()) {
            if (entry.getValue().size() > 5) {
                terms.add(entry.getKey());
                //writer.println(e.getKey());
            }
        }

        //writer.flush();
        numDoc = i;
        //writer.close();

        System.out.println("Total number of terms: "+terms.size());
        System.out.println("Total number of documents: "+numDoc);
    }

	public static void readTrainingData(String fileName) throws IOException  {

        String line; int i = 0;
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
		while ((line = reader.readLine()) != null) {

			//String fragment = data;

			//ArrayList<String> tokenList = preprocessDoc(line);
            ArrayList<String> tokenList = preprocessor.run(line);

			if (tokenList.size()>0) {

				documents.put(i, tokenList);

				for (String word : tokenList) {

					if (wordDocMapping.containsKey(word)) {

						ArrayList<Integer> existingDocs = wordDocMapping.get(word);

						if (!existingDocs.contains(i)) {
							existingDocs.add(i);
							wordDocMapping.put(word, existingDocs);
						}
					} else {
						ArrayList<Integer> existingDocs=new ArrayList<Integer>();							
						existingDocs.add(i);
						wordDocMapping.put(word, existingDocs);
					}			
				}
			}
			
			if (i%100 == 0) { System.out.println("Read "+i+" documents"); }
			i++;
		}
		
		PrintWriter writer = new PrintWriter(new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/allTerms2007.txt"));

		for (Entry<String, ArrayList<Integer>> e: wordDocMapping.entrySet()) {
			if (e.getValue().size()>5) {
				vocab.add(e.getKey());
                writer.println(e.getKey());
			}
		}

        writer.flush();
		numDoc=i;
        writer.close();

		System.out.println("Total number of terms:"+vocab.size());
		System.out.println("Total number of documents:"+numDoc);
	}

	public static void saveTxDAsFile() throws IOException
	{		
		System.out.println("********** Printin Corpus Stats *********");

		System.out.println("TxD Map Size:"+wordDocMapping.size());

		/*for (Entry<String, ArrayList<Integer>> e: wordDocMapping.entrySet())
		{
			StringBuilder integerSet=new StringBuilder("");
			for (Integer i: e.getValue())
			{
				if (integerSet.length()==0)
					integerSet.append(i);
				else
					integerSet.append(","+i);
			}

			System.out.println(e.getKey()+"==>"+integerSet);
		}*/
		System.out.println("Total Vocab Size:"+vocab.size());
		System.out.println("****");
		System.out.println("Total number of Docs:"+numDoc);
		System.out.println("*******************");

		FileWriter fWCS=new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/Statictics_Full.txt");
		PrintWriter pWCS=new PrintWriter(fWCS);

		pWCS.println("Term:"+vocab.size()+";Doc:"+numDoc);
		pWCS.flush();
		pWCS.close();	
		
	}

	public static void buildandSaveTxDMatrix() throws Exception
	{
		FileWriter fWCS=new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/MatrixFull2805.txt");
		PrintWriter pWCS=new PrintWriter(fWCS);
		
		//TxDMatrix=new double[vocab.size()][numDoc];
		//TxDMatrix = DoubleMatrix.zeros(vocab.size(), numDoc);	
		
		/*for (int i=0;i<TxDMatrix.length;i++)
		{
			for(int j=0;j<TxDMatrix[0].length;j++)
			{
				TxDMatrix[i][j]=0;
			}
		}*/

		for (int i=0;i<vocab.size();i++)
		{
			double[][] row=new double[1][numDoc];
			for (int k=0;k<row[0].length;k++)
			{
				row[0][k]=0.0;
			}
			
			for (int j: wordDocMapping.get(vocab.get(i)))
			{
				//try{
					//TxDMatrix.put(i, j, 1.0);
					//TxDMatrix.put(i, j, getCount(vocab.get(i), documents.get(j))); //Add TF here
				row[0][j]=getCount(vocab.get(i), documents.get(j)); //Add TF here
				//}catch(ArrayIndexOutOfBoundsException e)
				//{
					//System.out.println(i+"-"+j);
				//}
			}	
			
			for (int k=0;k<row[0].length;k++)
			{
				if (k==row[0].length-1)
				{
					pWCS.print(row[0][k]);
				}
				else
				{
					pWCS.print(row[0][k]+",");
				}
			}
				
			//if (i%1==0)
			//{
				System.out.println("Processed "+i+" terms");
			//}
			
			pWCS.println();
			pWCS.flush();
			
		}
		
		pWCS.close();
		fWCS.close();
	}
	
	public static void buildTxDMatrixDM() throws Exception
	{				
		//TxDMatrix=new double[vocab.size()][numDoc];
		TxDMatrix = DoubleMatrix.zeros(vocab.size(), numDoc);	
		
		for (int i=0;i<vocab.size();i++)
		{			
			for (int j: wordDocMapping.get(vocab.get(i)))
			{
				try{
					//TxDMatrix.put(i, j, 1.0);
					TxDMatrix.put(i, j, getCount(vocab.get(i), documents.get(j))); //Add TF here
				}catch(ArrayIndexOutOfBoundsException e)
				{
				System.out.println(i+"-"+j);
				}
			}				
				
			if (i%100==0)
			{
				System.out.println("Processed "+i+" terms");
			}			
		}
		
	}
	
	public static double getCount(String vocab, ArrayList<String> terms)
	{
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

	public static void testing()
	{
		String str="Alaska certifies Sen. Murkowski's re-election`Sen. Alaska Lisa Murkowski was officially named the winner of Alaska's U.S. Senate race Thursday, following a period of legal fights and limbo that lasted longer than the write-in campaign she waged to keep her job;Miller's decision , announced at a news conference in Anchorage, came one day after the state certified  Murkowski as the winner";
		ArrayList<String> allTokens=preprocessor.run(str);

		for(String s: allTokens)
		{
			System.out.println(s);
		}
	}

	public static void main(String args[]) throws Exception {

        Train bot = new Train ();
        wordDocMapping = bot.wordSeenInDocMap;
        documents = bot.docIndexMap;
        vocab = bot.terms;

        System.exit(5);

		buildandSaveTxDMatrix();	
		saveTxDAsFile();
	}
}