import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.jblas.DoubleMatrix;

class train
{
	static String delims = " \r\n\t()"; 
	//static PrintWriter pW;

	static HashMap<String, ArrayList<Integer>> wordDocMapping=new HashMap<String, ArrayList<Integer>>();
	static HashMap<Integer, ArrayList<String>> documents=new HashMap<Integer, ArrayList<String>>();
	static ArrayList<String> vocab=new ArrayList<String>();
	static DoubleMatrix TxDMatrix;
	//static double[][] TxDMatrix;
	static int numDoc=0;

	public static void readTrainingData(String fileName) throws IOException 
	{
		FileReader fRead=new FileReader(fileName);
		BufferedReader bRead=new BufferedReader(fRead);
		int docCNT=0;
		String data=null;

		while((data=bRead.readLine())!=null)
		{
			String fragment=data;
			ArrayList<String> allTokens=preprocessDoc(fragment);			
			if (allTokens.size()>0)
			{
				documents.put(docCNT, allTokens);
				for (String vocab : allTokens)
				{
					if (wordDocMapping.containsKey(vocab))
					{
						ArrayList<Integer> existingDocs=wordDocMapping.get(vocab);	
						if(!existingDocs.contains(docCNT))
						{
							existingDocs.add(docCNT);
							wordDocMapping.put(vocab, existingDocs);
						}
					}
					else
					{
						ArrayList<Integer> existingDocs=new ArrayList<Integer>();							
						existingDocs.add(docCNT);
						wordDocMapping.put(vocab, existingDocs);
					}			
				}
			}
			
			if (docCNT%100==0)
			{
				System.out.println("Read "+docCNT+" documents");
			}
			docCNT++;
		}
		
		FileWriter fW=new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/allTerms2007.txt");
		PrintWriter pW=new PrintWriter(fW);

		for (Entry<String, ArrayList<Integer>> e: wordDocMapping.entrySet())
		{
			if (e.getValue().size()>5)
			{
				vocab.add(e.getKey());
				pW.println(e.getKey());
			}
		}

		pW.flush();
		numDoc=docCNT;
		pW.close();
		fW.close();
		
		System.out.println("Total number of terms:"+vocab.size());
		System.out.println("Total number of documents:"+numDoc);
	}	

	static ArrayList<String> preprocessDoc(String s)
	{
		String DOC=s.replace("\"", "");
		DOC=manageMappings.replaceTokens(DOC);
		DOC=DOC.replaceAll("'s", "");	
		DOC=DOC.replaceAll("\n", " ");
		DOC=DOC.replaceAll("\\s+", " ");
		DOC=DOC.replaceAll("\\s$", "");
		DOC=DOC.replaceAll("^\\s", "");
		DOC=DOC.replaceAll("\\p{Punct}", " ");
		DOC=DOC.toLowerCase();	
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
				s=handleStop.filter(s);
				s=Stem.stemmer(s);
				if (!s.equals(""))
				{
					strTokens.add(s);
				}
			}
		}
		return strTokens;        	
	}

	static void saveTxDAsFile() throws IOException
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

	static void buildandSaveTxDMatrix() throws Exception
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
	
	static void buildTxDMatrixDM() throws Exception
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
	
	static double getCount(String vocab, ArrayList<String> terms)
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

	static void testing()
	{
		String str="Alaska certifies Sen. Murkowski's re-election`Sen. Alaska Lisa Murkowski was officially named the winner of Alaska's U.S. Senate race Thursday, following a period of legal fights and limbo that lasted longer than the write-in campaign she waged to keep her job;Miller's decision , announced at a news conference in Anchorage, came one day after the state certified  Murkowski as the winner";
		ArrayList<String> allTokens=preprocessDoc(str);

		for(String s: allTokens)
		{
			System.out.println(s);
		}
	}

	public static void main(String args[]) throws Exception
	{
		//FileWriter fW=new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/allTerms.txt");
		//pW=new PrintWriter(fW);
		String trainFile="/proj/fluke/users/shreya2k7/newsblaster/trainingFull.txt";
		try{
			readTrainingData(trainFile);
		} catch(IOException e)
		{
			e.printStackTrace();
		}		
		buildandSaveTxDMatrix();	
		saveTxDAsFile();
	}
}