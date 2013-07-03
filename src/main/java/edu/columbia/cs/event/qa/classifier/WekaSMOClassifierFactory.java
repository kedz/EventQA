package edu.columbia.cs.event.qa.classifier;

import edu.columbia.cs.event.qa.util.ProjectConfiguration;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 6/12/13
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */

public class WekaSMOClassifierFactory {

    private File serializedSMOFile;

    public WekaSMOClassifierFactory () {
        String serialiedSMOFileName = ProjectConfiguration.getInstance().getProperty("serialized.smo.file");
        this.serializedSMOFile = new File(serialiedSMOFileName);
    }

    public WekaSMOClassifier getWekaSMOClassifier () throws Exception {
        WekaSMOClassifier classifier;
        if (serializedSMOFile.exists()) {
            classifier = deserialize();
        } else {
            classifier = new WekaSMOClassifier();
            serialize(classifier);
        }
        return classifier;
    }

    public WekaSMOClassifier getWekaSMOClassifier (String trainFileName) throws Exception {
        WekaSMOClassifier classifier;
        if (serializedSMOFile.exists()) {
            classifier = deserialize();
        } else {
            classifier = new WekaSMOClassifier(trainFileName);
            serialize(classifier);
        }
        return classifier;
    }

    private void serialize (WekaSMOClassifier classifier) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(serializedSMOFile);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(classifier);
        out.close();
        fileOut.close();
//        try {
//        } catch(IOException e) {
//            System.err.println(e.getMessage());
//            e.printStackTrace();
//            System.exit(1);
//        }
    }

    private WekaSMOClassifier deserialize () throws Exception {
        WekaSMOClassifier classifier = null;
        FileInputStream fileIn = new FileInputStream(serializedSMOFile);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        classifier = (WekaSMOClassifier) in.readObject();
        in.close();
        fileIn.close();
//        try {
//        } catch(IOException e) {
//            System.err.println(e.getMessage());
//            e.printStackTrace();
//            System.exit(1);
//
//        } catch(ClassNotFoundException e) {
//            System.err.println(e.getMessage());
//            e.printStackTrace();
//            System.exit(1);
//        }
        return classifier;
    }

}
