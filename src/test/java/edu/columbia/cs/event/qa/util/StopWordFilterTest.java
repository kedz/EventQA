package edu.columbia.cs.event.qa.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 6/11/13
 * Time: 2:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class StopWordFilterTest {

    @Test
    public void testFilter() throws Exception {

        StopWordFilter StopWordFilter = new StopWordFilter();

        String aStopWord = "the";
        String nonStopWord = "stimpy";

        Assert.assertEquals("",StopWordFilter.filter(aStopWord));
        Assert.assertEquals("stimpy",StopWordFilter.filter(nonStopWord));
    }
}
