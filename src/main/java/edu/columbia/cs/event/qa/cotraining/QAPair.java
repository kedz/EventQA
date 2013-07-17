package edu.columbia.cs.event.qa.cotraining;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: wojo
 * Date: 7/1/13
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */

public class QAPair {

    private Element query;
    private Element answer;
    private String label;

    public QAPair (Element query, Element answer) {
        this(query, answer, "-1");
    }

    public QAPair (Node query, Node answer) {
        this(query, answer, "-1");
    }

    public QAPair (Node query, Node answer, String label) {
        this((Element) query, (Element) answer, label);
    }

    public QAPair (Element query, Element answer, String label) {
        this.query = query;
        this.answer = answer;
        this.label = label;
    }

    public String elementToString (Element e) { return elementToString(e, "Lemma"); }

    public String elementToString (Element e, String tag) {
        String output = "";
        NodeList words = e.getElementsByTagName(tag);
        for (int i=0; i<words.getLength(); i++) {
            output += words.item(i).getFirstChild().getNodeValue()+" ";
        }
        return output;
    }

    public ArrayList<String> elementToList (Element e) { return elementToList(e, "Lemma"); }

    public ArrayList<String> elementToList (Element e, String tag) {
        ArrayList<String> output = new ArrayList<String>();
        NodeList words = e.getElementsByTagName(tag);
        for (int i=0; i<words.getLength(); i++) {
            output.add(words.item(i).getFirstChild().getNodeValue());
        }
        return output;
    }

    public Element getQuery () { return query; }
    public ArrayList<String> getQueryList () { return elementToList(query); }
    public String getQueryString () { return elementToString(query); }
    public Element getAnswer () { return answer; }
    public ArrayList<String> getAnswerList () { return elementToList(answer); }
    public String getAnswerString () { return elementToString(answer); }
    public String getQAString () { return getQueryString()+"<->"+getAnswerString(); }
    public String getLabel () { return label; }
    public void setLabel (String label) { this.label = label; }
}
