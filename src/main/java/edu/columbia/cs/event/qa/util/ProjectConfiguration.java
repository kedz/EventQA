package edu.columbia.cs.event.qa.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 6/12/13
 * Time: 11:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectConfiguration {

    private Properties properties;

    private static ProjectConfiguration ProjectConfiguration;

    public static ProjectConfiguration newInstance() {
        if(ProjectConfiguration == null)
            ProjectConfiguration = new ProjectConfiguration();
        return ProjectConfiguration;
    }

    private ProjectConfiguration() {
        Properties prop = new Properties();
        try {
            prop.load(ProjectConfiguration.class.getClassLoader().getResourceAsStream("eventqa.properties"));
            this.properties = prop;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String property) {
        String output = "";
        if (property.contains("amt")) {
            output += properties.getProperty("processed.amt.dir");
        } else if (property.contains("file")) {
            output += properties.getProperty("root.dir");
        }
        return output+properties.getProperty(property);
    }
}
