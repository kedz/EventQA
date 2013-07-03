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

    private static AMTData AMTData;

    public static AMTData newInstance() {
        if(AMTData == null)
            AMTData = new AMTData();
        return AMTData;
    }

    public void loadAMTData () { loadAMTData("amt_qa_test_v1_t_.45_preprocessed.xml"); }

    public void loadAMTData (String fileName) {
        try {
            Document data = FileLoader.newInstance().loadXMLData(fileName);
            NodeList queries = data.getElementsByTagName("Question");
            NodeList answers = data.getElementsByTagName("Response");
            NodeList labels = data.getElementsByTagName("Label");

            for (int i=0; i<queries.getLength(); i++) {
                int label = Integer.parseInt(labels.item(i).getFirstChild().getNodeValue());
                QAPair pair = new QAPair(queries.item(i), answers.item(i), label);
                addWekaInstance(pair);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Instance addWekaInstance (QAPair pair) {
        // TODO
        //
        //SeedData.newInstance().addInstanceToI1(buildWekaInstance(pair);
        //SeedData.newInstance().addInstanceToI2(buildWekaInstance(pair));
        return null;
    }
}
