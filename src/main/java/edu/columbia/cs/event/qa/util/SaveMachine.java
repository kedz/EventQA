package edu.columbia.cs.event.qa.util;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;

/**
 * User: wojo
 * Date: 7/12/13
 * Time: 3:49 PM
 */

public class SaveMachine {

    private static SaveMachine SaveMachine;

    public static SaveMachine newInstance() {
        if(SaveMachine == null)
            SaveMachine = new SaveMachine();
        return SaveMachine;
    }

    public void saveInstancesToARFF (Instances instances, String fileName) {
        System.out.println("**************************************************************");
        System.out.println("Saving Instances to: "+fileName);
        System.out.println("[ # of Instances: "+instances.numInstances()+" ]");
        System.out.println("[ # of Attributes: "+instances.numAttributes()+" ]");
        try {
            ArffSaver saver = new ArffSaver();
            saver.setInstances(instances);
            saver.setFile(new File(fileName));
            saver.writeBatch();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
