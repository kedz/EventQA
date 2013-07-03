package edu.columbia.cs.event.qa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.columbia.cs.event.qa.util.SimplePreprocessor;
import org.jdom2.Document;
import org.jdom2.Element;

import org.jdom2.filter.ElementFilter;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;


class TextFileFilter implements FileFilter
{
     public boolean accept(File f)
    {
    	boolean decision=false;
        if(f.isDirectory() && f.getAbsolutePath().length()==67)
        {
            return true;
        }
        return decision;
    }
}

class ReadNewsblaster
{
	static ArrayList<String> eventKeywords=new ArrayList<String>();
	static Map<String, String> summaries=new HashMap<String, String>();
	static String matched;
	static PrintWriter pW;
	static PrintWriter pWLog;
    static SimplePreprocessor simplePreprocessor = new SimplePreprocessor();

	public static void initializeEvents()
	{
		eventKeywords.add(simplePreprocessor.stem("election"));
		eventKeywords.add(simplePreprocessor.stem("bomb"));
		eventKeywords.add(simplePreprocessor.stem("earthquake"));
		eventKeywords.add(simplePreprocessor.stem("tornado"));
		eventKeywords.add(simplePreprocessor.stem("tsunami"));
		eventKeywords.add(simplePreprocessor.stem("blast"));
		eventKeywords.add(simplePreprocessor.stem("terrorist"));
		eventKeywords.add(simplePreprocessor.stem("terrorism"));
		eventKeywords.add(simplePreprocessor.stem("boston"));
		eventKeywords.add(simplePreprocessor.stem("marathon"));
		eventKeywords.add(simplePreprocessor.stem("explosives"));
		eventKeywords.add(simplePreprocessor.stem("explosive"));
		eventKeywords.add(simplePreprocessor.stem("war"));
		eventKeywords.add(simplePreprocessor.stem("osama"));
		eventKeywords.add(simplePreprocessor.stem("nuclear"));
		eventKeywords.add(simplePreprocessor.stem("reactor"));
		eventKeywords.add(simplePreprocessor.stem("radiation"));
		eventKeywords.add(simplePreprocessor.stem("suspect"));
		eventKeywords.add(simplePreprocessor.stem("tsarnaev"));
		eventKeywords.add(simplePreprocessor.stem("watertown"));
		eventKeywords.add(simplePreprocessor.stem("sandy"));
		eventKeywords.add(simplePreprocessor.stem("katrina"));
		eventKeywords.add(simplePreprocessor.stem("japan"));
		eventKeywords.add(simplePreprocessor.stem("cyclone"));
		eventKeywords.add(simplePreprocessor.stem("hurricane"));
		eventKeywords.add(simplePreprocessor.stem("volcano"));
		eventKeywords.add(simplePreprocessor.stem("volcanic"));
	}

	public static String correctFile(String file) throws Exception
	{

		FileWriter fW=new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/file.xml");
		PrintWriter pW=new PrintWriter(fW);

		FileReader fR=new FileReader(file);
		BufferedReader bR=new BufferedReader(fR);

		String data=null;
		while((data=bR.readLine())!=null)
		{
			if (data.equals("<?xml version='1.0' encoding='utf8'?>"))
			{
				data="<?xml version='1.0' encoding='utf-8'?>";				
			}
			if (data.contains("^B"))
			{
				System.out.println("CORRECTIN");
				data=data.replace("^B", "");
			}
			pW.println(data);
			
		}
		pW.flush();
		pW.close();
		fW.close();
		fR.close();
		bR.close();

		return "/proj/fluke/users/shreya2k7/newsblaster/file.xml";
	}

	public static boolean containKeys(String keywrds, ArrayList<String> wordList)
	{
		boolean contains=false;

		String[] key=keywrds.split(",");
		StringBuilder sBuilder=new StringBuilder(",");
		for(String k: key)
		{
			try{
				if (k.charAt(0)==' ')
				{
					sBuilder.append(simplePreprocessor.stem(k.substring(1))+",");
				}
				else
				{
					sBuilder.append(simplePreprocessor.stem(k)+",");
				}
			}
			catch(StringIndexOutOfBoundsException e)
			{
				System.out.println("k:"+key.length+keywrds);
				for(int j=0;j<key.length;j++)
					System.out.println("kk:"+key[j]);
				e.printStackTrace();
				System.exit(0);
			}
		}		

		String keyword=sBuilder.toString();

		for (String word: wordList)
		{
			if (keyword.contains(","+word+",")) //|| keyword.startsWith(word+",") || keyword.endsWith(", "+word+","))
			{
				//System.out.println(keyword);
				matched=word;
				contains=true;
				break;
			}			
		}

		return contains;
	}


	/*public static void extractTraining(String fileName) throws Exception
	{		

		String corrFileName=correctFile(fileName);
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(corrFileName);

		Element root=doc.getRootElement();
		Namespace ns=root.getNamespace();

		Iterator<Element> itr = doc.getRootElement().getChildren().iterator();
		while (itr.hasNext())
		{
			//String title=null;

			Element category=itr.next();
			if (category.getName().equals("Category"))
			{
				for (Element grp: category.getChildren())
				{
					String keywords=grp.getAttribute("keywords").getValue();

					if (containKeys(keywords, eventKeywords))
					{
						for(Element events: grp.getChildren())
						{
							String title=events.getAttributeValue("title");
							for (Element cluster: grp.getChildren())
							{
								for (Element summary: cluster.getChildren())
								{
									StringBuilder summaryText=new StringBuilder();
									System.out.println(summaries.size());
									for (Element sentence: summary.getChildren())
									{										
										//System.out.println(sentence.getNamespace());
										summaryText.append(sentence.getChild("Fragment", sentence.getNamespace()).getAttributeValue("text"));
									}
									summaries.put(title, summaryText.toString());
								}
							}
						}

					}
				}
				System.out.println(category.getName());
			}
		}
	}*/

	public static void getTrainingSamples(String fileName) throws Exception
	{
		String corrFileName=correctFile(fileName);
		try{
		SAXBuilder builder = new SAXBuilder();
		
		Document doc = builder.build(corrFileName);
		
		Element root=doc.getRootElement();

		for (Element group: root.getDescendants(new ElementFilter("Group")))
		{
			String keywords=group.getAttribute("keywords").getValue().toLowerCase();
			if (keywords.length()<2)
			{
				pWLog.println("                 *** No KeyWords ***");
				for (Element event:group.getDescendants(new ElementFilter("Event")))
				{
					String title=event.getAttributeValue("title");
					pWLog.println("                 ::"+title);
				}
			}
			else
			{
				if (containKeys(keywords, eventKeywords))
				{
					System.out.println(keywords+"<>"+matched);
					pWLog.println("    Content In =>"+fileName);
					pWLog.println("                 "+keywords+"<>"+matched);
					for (Element event:group.getDescendants(new ElementFilter("Event")))
					{
						ArrayList<String> allSent=new ArrayList<String>();					
						String title=event.getAttributeValue("title");
						pWLog.println("                 ::"+title);

						StringBuilder summaries=new StringBuilder();
						for (Element sentence:event.getDescendants(new ElementFilter("Fragment")))
						{						
							if (!allSent.contains(sentence.getAttributeValue("text")))
							{
								String currSent=sentence.getAttributeValue("text");
								allSent.add(currSent);							
								//System.out.println("=>"+sentence.getAttributeValue("text"));							
								summaries.append(currSent);
							}		

						}
						if (!summaries.toString().isEmpty())
						{
							pW.println(title+"`"+summaries.toString());						
						}
					}
				}
			}
		}	
		pW.flush();
		
		} catch(JDOMParseException e)
		{
			pWLog.println("                 Invalid XML Character ** NOT PROCESSED");
		}
	}

	public static void extractTraining(String filePath) throws Exception
	{
		TextFileFilter tff=new TextFileFilter();
		File fHandle = new File(filePath);
		File[] allFiles=fHandle.listFiles(tff);
		//File[] allFiles=fHandle.listFiles();
		
		System.out.println(allFiles.length);

		int processed=0;
		for(int i=0;i<allFiles.length;i++)
		{
			if (allFiles[i].isDirectory())
			{
				String dirName=allFiles[i].getAbsolutePath();

				if (dirName.startsWith("/proj/fluke/users/shreya2k7/newsblaster/archive/2009") || dirName.startsWith("/proj/fluke/users/shreya2k7/newsblaster/archive/2010") ||dirName.startsWith("/proj/fluke/users/shreya2k7/newsblaster/archive/2011") || dirName.startsWith("/proj/fluke/users/shreya2k7/newsblaster/archive/2012") || dirName.startsWith("/proj/fluke/users/shreya2k7/newsblaster/archive/2013"))
				{
					String nextinLine=allFiles[i+1].getAbsolutePath();
					try{
					if (dirName.substring(dirName.lastIndexOf("/")+1, dirName.lastIndexOf("/")+11).equals(nextinLine.substring(nextinLine.lastIndexOf("/")+1, nextinLine.lastIndexOf("/")+11)))
						continue;
					else
					{
						pWLog.println("##Processing:"+dirName+"/data/today.xml");
						System.out.println(dirName+"/data/today.xml");
						getTrainingSamples(dirName+"/data/today.xml");
						processed++;
						if (processed%100>10)
						{
							pWLog.println("Processed:"+processed);
						}
						pWLog.flush();
					}
					}
					catch(StringIndexOutOfBoundsException e)
					{
						pWLog.println("ERROR**************PLS SEE**********");
						pWLog.println(dirName+"->"+nextinLine);
					}
				}
			}
		}		
	}

	public static void main(String[] args) throws Exception
	{
		initializeEvents();
		String fPath="/proj/fluke/users/shreya2k7/newsblaster/archive";
		FileWriter fW=new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/training2007.txt");
		FileWriter fWlog=new FileWriter("/proj/fluke/users/shreya2k7/newsblaster/training2007Log.txt");
		pW=new PrintWriter(fW);
		pWLog=new PrintWriter(fWlog);
		extractTraining(fPath);

		//String xmlFile="/proj/fluke/users/shreya2k7/newsblaster/archive/2013-04-20-05-03-08/data/today.xml";
		//String xmlFile="/proj/fluke/users/shreya2k7/newsblaster/file.xml";
		//getTrainingSamples(xmlFile);
		fW.close();
		pW.close();
		fWlog.close();
		pWLog.close();
	}
}

