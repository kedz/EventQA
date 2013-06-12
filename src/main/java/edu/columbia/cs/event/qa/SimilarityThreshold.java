package edu.columbia.cs.event.qa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import edu.columbia.cs.event.qa.ManageMappings;
import edu.columbia.cs.event.qa.Stem;
import edu.columbia.cs.event.qa.util.StopWordFilter;
import org.jblas.DoubleMatrix;

public class SimilarityThreshold
{
	static String delims = " \r\n\t()"; 

	static ArrayList<String> terms=new ArrayList<String>();
	static ArrayList<ArrayList<String>> titles=new ArrayList<ArrayList<String>>();
	static Map<Integer, ArrayList<String>> summaries=new HashMap<Integer, ArrayList<String>>();
	static Map<Integer, ArrayList<ArrayList<String>>> summarySentences=new HashMap<Integer, ArrayList<ArrayList<String>>>();
	static DoubleMatrix Space;

	static void readTermDocs(String termFileName, String docFileName, int level) throws Exception
	{
		System.out.println("Adding terms");
		FileReader fR=new FileReader(termFileName);
		BufferedReader bR=new BufferedReader(fR);

		String term=null;		
		while((term=bR.readLine())!=null)
		{
			terms.add(term);
		}	

		bR.close();
		fR.close();

		System.out.println("Adding documents");
		fR=new FileReader(docFileName);
		bR=new BufferedReader(fR);

		String data=null;
		int cnt=0;
		while((data=bR.readLine())!=null)
		{
			String[] quesAns=data.split("`");
			try{
				titles.add(preprocessDoc(quesAns[0]));

				if (level==2)   //Sentence Level
				{
					ArrayList<ArrayList<String>> summarySent=new ArrayList<ArrayList<String>>();
					for (String each: sentenceSplitter(quesAns[1]))
					{
						summarySent.add(preprocessDoc(each));
					}
					summarySentences.put(cnt, summarySent);
				}
				else if (level==1)  // Paragraph level
				{
					summaries.put(cnt, preprocessDoc(quesAns[1]));
				}
				else
				{
					System.out.println("Enter correct processinf level: (1) for Paragraph level and (2) for Sentence level");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println(data);
			}
			cnt++;
		}		
		System.out.println("Done Reading terms and documents");

	}

	static double[] transform(ArrayList<String> str)
	{		
		double[] queryArr=new double[terms.size()];

		for (int i=0;i<terms.size();i++)
		{
			if (str.contains(terms.get(i)))
				queryArr[i]=getCount(terms.get(i), str);
			else
				queryArr[i]=0.0;
		}		

		return queryArr;
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

	static ArrayList<String> preprocessDoc(String s)
	{
		String DOC=s.replace("\"", "");
		DOC= ManageMappings.replaceTokens(DOC);
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
				s= StopWordFilter.filter(s);
				s= Stem.stemmer(s);
				if (!s.equals(""))
				{
					strTokens.add(s);
				}
			}
		}
		return strTokens;        	
	}

	static double dotprod(double[] d1, double[] d2)
	{

		double sum=0;
		for (int i=0;i<d1.length;i++)
		{
			sum += d1[i]*d2[i];				
		}		
		return sum;
	}

	static double length(double[] d)
	{		
		double sum=0;
		for (int i=0;i<d.length;i++)
		{
			sum += d[i]*d[i];
		}		
		return Math.sqrt(sum);
	}	

	public static double getSimilaritybwDocs(double[] d1, double[] d2) //Ecach document pair
	{					
		double sim=dotprod(d1, d2) / (length(d1)*length(d2));
		return sim;
	}

	static void readSpace(String sName, int k) throws Exception
	{
		System.out.println("Reading Space");
		Space=DoubleMatrix.zeros(k, terms.size());//new double[1000][terms.size()];
		FileReader fR=new FileReader(sName);
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
					System.exit(0);
				}
			}
			cnt++;
		}
		System.out.println("Done Reading Space");
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

	static double[] foldin(double[] str)
	{
		DoubleMatrix strDM=new DoubleMatrix(str);
		return Space.mmul(strDM).toArray();
	}

	static void getSVMTrainingFileSentenceLevel(String trainWekaFile) throws Exception
	{
		FileWriter fW=new FileWriter(trainWekaFile);
		PrintWriter pW=new PrintWriter(fW);

		pW.println("@relation event");
		pW.println("@attribute cosine numeric");
		pW.println("@attribute class {yes, no}");
		pW.println();

		pW.println("@data");
		pW.flush();

		System.out.println("Space:"+Space.getRow(0));
		for (int i=0;i<titles.size();i++)
		{
			if (i%100==0)
				System.out.println("Processing doc"+i);

			double[] d1=foldin(transform(titles.get(i)));

			for (ArrayList<String> each: summarySentences.get(i))
			{
				double[] d2=foldin(transform(each));
				double similarity=getSimilaritybwDocs(d1, d2);
				if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
				{
					pW.println(similarity+",yes");
				}
			}
			pW.flush();

			//*********
			if (i+1>titles.size()-1)
			{
				for (ArrayList<String> each: summarySentences.get(i-1))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			else
			{
				for (ArrayList<String> each: summarySentences.get(i+1))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			pW.flush();

			//*********
			if (i+2>titles.size()-1)
			{
				for (ArrayList<String> each: summarySentences.get(i-2))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			else
			{
				for (ArrayList<String> each: summarySentences.get(i+2))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			pW.flush();

			//*********
			if (i+3>titles.size()-1)
			{
				for (ArrayList<String> each: summarySentences.get(i-3))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			else
			{
				for (ArrayList<String> each: summarySentences.get(i+3))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			pW.flush();

			//*********
			if (i+4>titles.size()-1)
			{
				for (ArrayList<String> each: summarySentences.get(i-4))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			else
			{
				for (ArrayList<String> each: summarySentences.get(i+4))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			pW.flush();

			//*********
			/*if (i+5>titles.size()-1)
			{
				for (ArrayList<String> each: summarySentences.get(i-5))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			else
			{
				for (ArrayList<String> each: summarySentences.get(i+5))
				{
					double[] d2=foldin(transform(each));
					double similarity=getSimilaritybwDocs(d1, d2);
					if (!Double.isNaN(similarity) && !Double.isInfinite(similarity))
					{
						pW.println(similarity+",no");
					}
				}
			}
			pW.flush();*/


		}
		pW.flush();
		pW.close();
		fW.close();
	}

	static void getSVMTrainingFileParaLevel(String trainWekaFile) throws Exception
	{
		FileWriter fW=new FileWriter(trainWekaFile);
		PrintWriter pW=new PrintWriter(fW);

		pW.println("@relation event");
		pW.println("@attribute cosine numeric");
		pW.println("@attribute class {yes, no}");
		pW.println();

		pW.println("@data");
		pW.flush();

		System.out.println("Space:"+Space.getRow(0));
		for (int i=0;i<titles.size();i++)
		//for (int i=0;i<1;i++)
		{
			if (i%100==0)
				System.out.println("Processing doc"+i);

			double[] d1=foldin(transform(titles.get(i)));

			double[] d2=foldin(transform(summaries.get(i)));
			double similarity=getSimilaritybwDocs(d1, d2);
			pW.println(similarity+",yes");
			pW.flush();

			d1=foldin(transform(titles.get(i)));
			if (i+1>titles.size()-1)
				d2=foldin(transform(summaries.get(i-1)));
			else
				d2=foldin(transform(summaries.get(i+1)));
			similarity=getSimilaritybwDocs(d1, d2);
			pW.println(similarity+",no");

			if (i+2>titles.size()-1)
				d2=foldin(transform(summaries.get(i-2)));
			else
				d2=foldin(transform(summaries.get(i+2)));
			similarity=getSimilaritybwDocs(d1, d2);
			pW.println(similarity+",no");

			if (i+3>titles.size()-1)
				d2=foldin(transform(summaries.get(i-3)));
			else
				d2=foldin(transform(summaries.get(i+3)));
			similarity=getSimilaritybwDocs(d1, d2);
			pW.println(similarity+",no");

			/*if (i+4>titles.size()-1)
				d2=foldin(transform(summaries.get(i-4)));
			else
				d2=foldin(transform(summaries.get(i+4)));
			similarity=getSimilaritybwDocs(d1, d2);
			pW.println(similarity+",no");

			if (i+5>titles.size()-1)
				d2=foldin(transform(summaries.get(i-5)));
			else
				d2=foldin(transform(summaries.get(i+5)));
			similarity=getSimilaritybwDocs(d1, d2);
			pW.println(similarity+",no");

			if (i+6>titles.size()-1)
				d2=foldin(transform(summaries.get(i-6)));
			else
				d2=foldin(transform(summaries.get(i+6)));
			similarity=getSimilaritybwDocs(d1, d2);
			pW.println(similarity+",no");*/

			pW.flush();
		}
		pW.flush();
		pW.close();
		fW.close();
	}

	public static void main(String args[]) throws Exception
	{
		if (args.length!=6)
		{
			System.out.println("Incorrect number of input arguments");
			System.exit(0);
		}

		String vocabFileName=args[0];
		String trainingdocFileName=args[1];
		String spaceFileName=args[2];
		String trainWekaFileName=args[3];		
		int eigenVecs=Integer.parseInt(args[4]);
		int level=Integer.parseInt(args[5]);

		readTermDocs(vocabFileName, trainingdocFileName, level);
		readSpace(spaceFileName, eigenVecs);
		
		if (level==1)
		{
			getSVMTrainingFileParaLevel(trainWekaFileName);
		}
		else if(level==2)
		{
			getSVMTrainingFileSentenceLevel(trainWekaFileName);
		}
			

		/*readTermDocs("/bolt/ir2/columbia/EventQA/training_Vocab_Full.txt", "/bolt/ir2/columbia/EventQA/QApair_text_training_Full.txt");
		readSpace("/bolt/ir2/columbia/EventQA/Space.txt", eigenVecs);
		getTrainingFile("/bolt/ir2/columbia/EventQA/train.arff");

		String str="Japan is generally considered the best-prepared country on earth when it comes to tsunami and earthquake hazards. This was truly an enormous earthquake, and short of not living in zones with this kind of earthquake risk, it's not clear how much more could be done. The one area of poor preparation so far seems to have been the Fukushima nuclear reactors. It's not clear yet what went so wrong, but clearly the plans there were inadequate.";
		int cnt=0;
		for (String s: sentenceSplitter(str))
		{
			cnt++;
			System.out.println(cnt+"->"+s);
		}*/
	}
}