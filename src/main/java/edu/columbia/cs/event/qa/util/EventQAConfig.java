package edu.columbia.cs.event.qa.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 6/12/13
 * Time: 11:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventQAConfig {

    private Properties properties;

    private static EventQAConfig EVENT_QA_CONFIGURATION;

    public static EventQAConfig getInstance() {
        if(EVENT_QA_CONFIGURATION == null)
            EVENT_QA_CONFIGURATION = new EventQAConfig();
        return EVENT_QA_CONFIGURATION;
    }


    private EventQAConfig() {

        Properties prop = new Properties();
        System.out.println("[ LOADING CONFIGURATION ]");

        try {
            prop.load(EventQAConfig.class.getClassLoader().getResourceAsStream("eventqa.properties"));
            this.properties = prop;

        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }

    public String getProperty(String property) {
        if (property.contains("file"))
            return properties.getProperty("dir")+properties.getProperty(property);
        else
            return properties.getProperty(property);
    }


}
