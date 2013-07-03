package edu.columbia.cs.event.qa.cotraining;

import org.apache.commons.lang.StringUtils;
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
    private int label;

    public QAPair (Element query, Element answer) {
        this(query, answer, -1);
    }

    public QAPair (Node query, Node answer) {
        this(query, answer, -1);
    }

    public QAPair (Node query, Node answer, int label) {
        this((Element) query, (Element) answer, label);
    }

    public QAPair (Element query, Element answer, int label) {
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

    public Element getQuery () { return query; }
    public String getQueryString () { return elementToString(query); }
    public Element getAnswer () { return answer; }
    public String getAnswerString () { return elementToString(answer); }
    public String getQAString () { return getQueryString()+"<->"+getAnswerString(); }
    public int getLabel () { return label; }
    public void setLabel (int label) { this.label = label; }
}
