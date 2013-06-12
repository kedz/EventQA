package edu.columbia.cs.event.qa;

import edu.columbia.cs.event.qa.Train;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This Class Computes the Document Frequency for each Term
 */
public class GetDocumentFrequency {
	
	public static ArrayList<String> terms=new ArrayList<String>();
	public static Map<String, Double> df=new HashMap<String, Double>();
	public static int N=0;
	
	public static void getDocFrequency(String termList, String docList, String dfreq) throws Exception
	{	
		FileReader fR=new FileReader(termList);
		BufferedReader bR=new BufferedReader(fR);
		
		FileWriter fWtr=new FileWriter(dfreq);
		PrintWriter pW=new PrintWriter(fWtr);
		String data="";
		//int cnt=0;
		while ((data=bR.readLine())!=null)
		{
			if (!data.contains("~") && !data.contains(":") && !data.contains("\\") && !data.contains("<") && !data.contains("+") && !data.contains("^"))
				terms.add(data);
			
			//cnt++;
		}
		
		System.out.println("Terms:"+terms.size());
		
		FileReader fRdr=new FileReader(docList);
		BufferedReader bRdr=new BufferedReader(fRdr);

		String docLine="";
		int cnt2=0;
		while ((docLine=bRdr.readLine())!=null)
		{
			ArrayList<String> allTokens= Train.preprocessDoc(docLine);
			for(String t: terms)
			{
				if (allTokens.contains(t))
				{
					if (!df.containsKey(t))
						df.put(t, 1.0);
					else
					{
						double dfcurr=df.get(t);
						df.put(t, dfcurr+1.0);
					}
				}
			}
			cnt2++;
			
			if (cnt2%100==0)
			{
				System.out.println("Done:"+cnt2);
			}
			
			N=N+1;
			df.put("NoOfDocuments", (double)N);
		}
		
		System.out.println("DF map size:"+df.size());
		
		ArrayList<String> remove=new ArrayList<String>();
		
		for (Entry<String, Double> edf: df.entrySet())
		{
			if (edf.getValue()<10.0)
			{
				remove.add(edf.getKey());
			}
		}
		
		for (String x: remove)
		{
			df.remove(x);
		}
		
		System.out.println("DF map size modified:"+df.size());
		/*ArrayList<String> removeTerms=new ArrayList<String>();
		
		for (String t: terms)
		{
			if (!df.containsKey(t))
				removeTerms.add(t);			
		}
		
		for (String x: removeTerms)
		{
			terms.remove(x);
		}
		
		for (Entry<String, Double> e: df.entrySet())
		{
			pW.println(e.getKey()+";"+e.getValue());
			pW.flush();
		}*/
		
		pW.flush();
		pW.close();
	}
	
	public static void main(String args[]) throws Exception
	{
		getDocFrequency("/proj/fluke/users/shreya2k7/newsblaster/allTerms.txt","/proj/fluke/users/shreya2k7/newsblaster/trainingFull.txt","/proj/fluke/users/shreya2k7/newsblaster/DF.txt");
	}
}