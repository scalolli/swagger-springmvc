package com.mangofactory.swagger.springmvc.util;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: basu
 * Date: 30/1/13
 * Time: 10:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class UtilsTest {

    @Test
    public void testWithList() throws Exception {
        class Test {
           List<String> values;
        }
        Assert.assertTrue(Utils.isParametrizeListOfValues(Test.class.getDeclaredField("values")));
    }

    @Test
    public void testWithArrayList() throws Exception {
        class Test {
            ArrayList<String> values;
        }
        Assert.assertTrue(Utils.isParametrizeListOfValues(Test.class.getDeclaredField("values")));
    }

    @Test
    @Ignore("Currently ignored since I still need to add support for Maps as response types.")
    public void testWithMap() throws Exception {
        class Test {
            Map<String, String> values;
        }
        Assert.assertTrue(Utils.isParametrizeListOfValues(Test.class.getDeclaredField("values")));
    }
}
