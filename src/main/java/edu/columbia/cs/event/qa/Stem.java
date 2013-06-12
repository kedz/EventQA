package edu.columbia.cs.event.qa;

import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;

public class Stem
{
	public static String stemmer(String word)
	{
		String stemmed="";
		try{
			stemmed=PorterStemmerTokenizerFactory.stem(word);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return stemmed;
	}
	
	public static void main(String args[])
	{
		System.out.println(stemmer(" tornadoes"));
		String str="/proj/fluke/users/shreya2k7/newsblaster/archive/2009-06-15-04-22-03";
		System.out.println(str.length());
	}
	
}

