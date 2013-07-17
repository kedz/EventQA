package edu.columbia.cs.event.qa.util;

import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 6/13/13
 * Time: 1:21 PM
 * To change this template use File | Settings | File Templates.
 */

public class SimplePreprocessor {

    private String delimeters = " \r\n\t()";
    private HashSet<String> stopwords;
    private HashMap<String,String> replacewords;

    private static SimplePreprocessor SimplePreprocessor;

    public static SimplePreprocessor newInstance() {
        if(SimplePreprocessor == null)
            SimplePreprocessor = new SimplePreprocessor();
        return SimplePreprocessor;
    }

    public SimplePreprocessor() {
        try {
            stopwords = LoadMachine.newInstance().loadStopWords();
            replacewords = LoadMachine.newInstance().loadReplaceWords();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String replace (String line) {
        for (String word : replacewords.keySet()) {
            if (line.contains(word)) {
                line = line.replaceAll(word, replacewords.get(word));
            }
        }
        line = line.replaceAll("\\s+", " ");
        line = line.replaceAll("\\s$", "");
        line = line.replaceAll("^\\s", "");
        line = line.replaceAll("\\p{Punct}", " ");
        return line.toLowerCase();
    }

    public ArrayList<String> tokenize (String sentence) {
        ArrayList<String> words = new ArrayList<String>();
        StringTokenizer stokenizer = new StringTokenizer(sentence, delimeters, true);
        while (stokenizer.hasMoreTokens()) {
            String token = stokenizer.nextToken();
            if (!token.trim().equals("")) {
                token = filter(token);
                token = stem(token);
                if (!token.equals("")) {
                    words.add(token);
                }
            }
        }
        return words;
    }

    public String filter (String word) {
        if (stopwords.contains(word)) { return ""; }
        else { return word; }
    }

    public String stem (String word) {
        return PorterStemmerTokenizerFactory.stem(word);
    }

    public String escape (String line) {
        return StringEscapeUtils.unescapeXml(line);
    }

    public ArrayList<String> forge (String line) {
        return tokenize(replace(escape(line)));
    }
}
