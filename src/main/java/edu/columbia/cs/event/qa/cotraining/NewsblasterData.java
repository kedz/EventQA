package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import edu.columbia.cs.event.qa.util.LoadMachine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashSet;

/**
* Created with IntelliJ IDEA.
* User: wojo
* Date: 6/27/13
* Time: 4:12 PM
* To change this template use File | Settings | File Templates.
*/

public class NewsblasterData {

    private File[] XMLFiles;
    private HashSet<String> seedTable;

    private boolean printDocIDOn;

    private static NewsblasterData NBData;

    public static NewsblasterData newInstance() {
        if(NBData == null)
            NBData = new NewsblasterData();
        return NBData;
    }

    public NewsblasterData () {
        File folder = new File(ProjectConfiguration.newInstance().getProperty("processed.nb.dir"));
        XMLFiles = folder.listFiles();
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
        try {
            doc = LoadMachine.newInstance().loadXMLFile(XMLFiles[item].getAbsolutePath());
            if (printDocIDOn) { System.out.println("DOC#"+item+": "+XMLFiles[item].getName()); }
        } catch (Exception e) {
            System.err.println("Error: loading XML File: "+XMLFiles[item].getName());
            e.printStackTrace();
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
                        doc = null;
                        return new QAPair(title, sentence);
                    }
                }
            }
        }
        doc = null;
        return selectRandomQAPair(selectRandomDocument());
    }

    /*
     * SEED TABLE
     */

    public void updateSeedTable (String entry) {
        seedTable.add(entry);
    }

    public boolean existsInSeedTable (String entry) {
        if (seedTable.contains(entry)) { return true;} else { return false; }
    }

    public void clearSeedTable () { seedTable.clear(); }

    public HashSet<String> getSeedTable () { return seedTable; }

//    public static void main (String[] args) {
//        QAPair pair = NewsblasterData.newInstance().selectUniqueQAPair();
//        System.out.println(pair.getQAString());
//    }
}
