package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.util.FileLoader;
import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: wojo
* Date: 6/27/13
* Time: 4:12 PM
* To change this template use File | Settings | File Templates.
*/

public class NewsblasterData {

    private File[] XMLFiles;
    private Map<Integer,Document> cache;
    private HashSet<String> seedTable;

    private static NewsblasterData NBData;

    public static NewsblasterData newInstance() {
        if(NBData == null)
            NBData = new NewsblasterData();
        return NBData;
    }

    public NewsblasterData () {
        File folder = new File(ProjectConfiguration.getInstance().getProperty("processed.nb.dir"));
        XMLFiles = folder.listFiles();
        cache = new HashMap<Integer,Document>();
        seedTable = new HashSet<String>();
    }

    /*
     * SELECTION
     */

    public int randNumInRange (int len) { return (int) Math.floor(Math.random() * len); }

    public QAPair selectUniqueQAPair () {
        while (true) {
            QAPair pair = selectRandomQAPair(selectRandomDocument());
            if (!existsInSeedTable(pair.getQAString())) { return pair;}
        }
    }

    public Document selectRandomDocument () {
        Document doc = null;
        int item = randNumInRange(XMLFiles.length);
        if (cache.containsKey(item)) {
            doc = cache.get(item);
            System.out.println("CACHE#"+item+": "+XMLFiles[item].getName());
        } else {
            try {
                doc = FileLoader.newInstance().loadXMLData(XMLFiles[item].getAbsolutePath());
                System.out.println("DOC#"+item+": "+XMLFiles[item].getName());
                cache.put(item, doc);
            } catch (Exception e) {
                System.err.println("Error: loading XML File: "+XMLFiles[item].getName());
                e.printStackTrace();
            }
        }
        return doc;
    }

    public QAPair selectRandomQAPair (Document doc) {
        NodeList summaries = doc.getElementsByTagName("Summary");
        if (summaries != null) {
            int item = randNumInRange(summaries.getLength());
            Element summary = (Element) summaries.item(item);
            if (summary != null) {
                Node title = summary.getElementsByTagName("Title").item(0);
                NodeList sentences = summary.getElementsByTagName("Sentence");
                if (title != null && sentences.getLength() != 0) {
                    item = randNumInRange(sentences.getLength());
                    Node sentence = sentences.item(item);
                    if (sentence != null) {
                        return new QAPair(title, sentence);
                    }
                }
            }
        }
        return selectRandomQAPair(selectRandomDocument());
    }

    /*
     * SEED TABLE
     */

    public void addToSeedTable (String entry) {
        seedTable.add(entry);
    }

    public boolean existsInSeedTable (String entry) {
        if (seedTable.contains(entry)) { return true;} else { return false; }
    }

    public void clearSeedTable () {
        seedTable.clear();
    }

}
