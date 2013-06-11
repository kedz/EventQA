package edu.columbia.cs.event.qa.util;
import java.util.*;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: gbarber205
 * Date: 6/11/13
 * Time: 4:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class StopWordFilter {

    private static HashSet stopwords;

    static {
        try {
        String file = "stopwords.txt";
        stopwords = new HashSet<String>();
        InputStream input = ClassLoader.getSystemResourceAsStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        String line = bufferedReader.readLine();
        while (line != null) {
            stopwords.add(line);
            line = bufferedReader.readLine();
        }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

    public static String filter( String word ){        //Stop word removal
         if (stopwords.contains(word)) {
             return "";
        }
        else
             return word;
    }
}
