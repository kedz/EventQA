package edu.columbia.cs.event.qa.util;

import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class Preprocessor {

    private String delims = " \r\n\t()";

    private HashSet<String> stopwords;
    private HashMap<String,String> replacewords;

    public Preprocessor() { load(); }

    public void load () { loadStopWords(); loadReplaceWords(); }

    public ArrayList<String> run (String line) {
        return tokenize(replace(line));
    }

    public void loadStopWords () { loadStopWords("stopwords.txt"); }

    public void loadStopWords (String stopWordsFile) {
        try {
            this.stopwords = new HashSet<String>();
            InputStream input = ClassLoader.getSystemResourceAsStream(stopWordsFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                stopwords.add(line.trim());
            }
            input.close();
            reader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void loadReplaceWords () { loadReplacements("replacements.txt"); }

    public void loadReplacements (String replaceWordsFile) {
        try {
            this.replacewords = new HashMap<String, String>();
            InputStream input = ClassLoader.getSystemResourceAsStream(replaceWordsFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pair = line.trim().split("->");
                if (pair.length > 1){
                    replacewords.put(pair[0], pair[1]);
                } else{
                    replacewords.put(pair[0], "");
                }
            }
            input.close();
            reader.close();
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
        StringTokenizer stokenizer = new StringTokenizer(sentence, delims, true);
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
}
