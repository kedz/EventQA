package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.util.FileLoader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/1/13
 * Time: 4:10 PM
 * To change this template use File | Settings | File Templates.
 */

public class AMTData {

    private SeedData seed;

    private static AMTData AMTData;

    public static AMTData newInstance(WekaInterface c1, WekaInterface c2) {
        if(AMTData == null)
            AMTData = new AMTData(c1, c2);
        return AMTData;
    }

    public AMTData (WekaInterface c1, WekaInterface c2) {
        System.out.println("Loading Weka Classifiers");
        this.seed = new SeedData(c1, c2);
    }

    public SeedData loadAMTData () { return loadAMTData("amt_qa_test_v1_t_.45_preprocessed.xml"); }

    public SeedData loadAMTData (String fileName) {
        try {
            Document data = FileLoader.newInstance().loadXMLData(fileName);
            NodeList queries = data.getElementsByTagName("Question");
            NodeList answers = data.getElementsByTagName("Response");
            NodeList labels = data.getElementsByTagName("Label");

            for (int i=0; i<queries.getLength(); i++) {
                int label = Integer.parseInt(labels.item(i).getFirstChild().getNodeValue());
                QAPair pair = new QAPair(queries.item(i), answers.item(i), label);
                addWekaInstancesToSeed(pair);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return seed;
    }

    public void addWekaInstancesToSeed (QAPair pair) {
        seed.addInstanceToI1(pair);
        seed.addInstanceToI2(pair);
    }
}
