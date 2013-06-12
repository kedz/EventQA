package edu.columbia.cs.event.qa.needsrefactoring;

import java.util.HashMap;
import java.util.Map;

public class ManageMappings
{
	static Map<String, String> expander=new HashMap<String, String>();
	
	public static String replaceTokens(String doc)
	{
		doc=doc.replaceAll("U.S.", "United States");
		doc=doc.replaceAll("US", "United States");
		doc=doc.replaceAll(" Co. ", "Company ");
		doc=doc.replaceAll("Sen. ", "Senator ");
		doc=doc.replaceAll("Gen. ", "General ");
		doc=doc.replaceAll("Gov. ", "Governor ");
		doc=doc.replaceAll("St. ", "Saint ");
		doc=doc.replace(" don't", " do not");
		doc=doc.replace("Don't", "Do not");
		
		return doc;
	}
}