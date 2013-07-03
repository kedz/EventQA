package edu.columbia.cs.event.qa.cotraining;

import edu.columbia.cs.event.qa.util.ProjectConfiguration;
import weka.core.Instance;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/2/13
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cotraining {

    private int k;  // # iterations
    private int u;  // pool size
    private int v;  // leap size

    private SeedData seed;

    public Cotraining (WekaInterface c1, WekaInterface c2) {
        k = Integer.parseInt(ProjectConfiguration.getInstance().getProperty("number.of.iterations"));
        u = Integer.parseInt(ProjectConfiguration.getInstance().getProperty("pool.size"));
        v = Integer.parseInt(ProjectConfiguration.getInstance().getProperty("leap.size"));
        load(c1, c2);
        run();
    }

    public void load (WekaInterface c1, WekaInterface c2) {
        System.out.println("Building AMT Seed Training Data");
        this.seed = AMTData.newInstance(c1, c2).loadAMTData(ProjectConfiguration.getInstance().getProperty("amt.train.file"));
    }

    public void run () {

        for (int i=0; i<k; i++) {

            /* Train Classifiers C1 and C2 using Seed data */
            seed.buildClassifiers();

            /* Randomly choose u examples from Newsblaster data */
            HashMap<Instance,QAPair> mapOfInstances1 = select(seed.getClassifier1());
            HashMap<Instance,QAPair> mapOfInstances2 = select(seed.getClassifier2());

            /* Select v balanced examples and add to Seed data */

        }

    }

    public HashMap<Instance,QAPair> select (WekaInterface classifier) {
        HashMap<Instance,QAPair> map = new HashMap<Instance,QAPair>();
        for (int i=0; i<u/2; i++) {
            QAPair pair = NewsblasterData.newInstance().selectUniqueQAPair();
            Instance view = classifier.buildWekaInstance(pair);
            pair.setLabel(classifier.classifyInstance(view));
            map.put(view, pair);
        }
        return map;
    }

}
