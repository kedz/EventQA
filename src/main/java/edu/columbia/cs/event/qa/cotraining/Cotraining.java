package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/2/13
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cotraining {

    private int k; // # iterations
    private int u; // pool size
    private int v; // leap size

    private SeedData seed;

    private boolean on;

    public Cotraining () {
        k = Integer.parseInt(ProjectConfiguration.getInstance().getProperty("number.of.iterations"));
        u = Integer.parseInt(ProjectConfiguration.getInstance().getProperty("pool.size"));
        v = Integer.parseInt(ProjectConfiguration.getInstance().getProperty("leap.size"));
        System.out.println("Building AMT Seed Training Data");
        AMTData.newInstance().loadAMTData("amt_qa_test_v1_t_.45_preprocessed.xml");
        seed = new SeedData();
    }

    public void run () {

        for (int i=0; i<k; i++) {

            /* Train Classifiers C1 and C2 using Seed data */
            SeedData.newInstance().buildClassifiers();

            /* Randomly choose u examples from Newsblaster data */
            HashMap<QAPair,Instance> QAInstanceMap1 = select();
            HashMap<QAPair,Instance> QAInstanceMap2 = select();

            /* Select v balanced examples and add to Seed data */
        }

    }

    public HashMap<QAPair,Instance> select () {
        HashMap<QAPair,Instance> map = new HashMap<QAPair, Instance>();
        for (int i=0; i<u/2; i++) {
            QAPair pair = NewsblasterData.newInstance().selectUniqueQAPair();
            Instance view = buildWekaInstance(pair);
            map.put(pair,view);
        }
        on = !on;
        return map;
    }

    public Instance buildWekaInstance (QAPair pair) {
        Instance view = null;
        if (on) {
            // TODO
        } else {
            // TODO
        }
        return view;
    }
}
