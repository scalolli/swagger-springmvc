package com.mangofactory.swagger.springmvc.util;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
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
           Collection<String> values;
        }
        Assert.assertTrue(Util.isListType(Test.class.getDeclaredField("values").getType()));
    }

    @Test
    public void testWithArrayList() throws Exception {
        class Test {
            ArrayList<String> values;
        }
        Assert.assertTrue(Util.isListType(Test.class.getDeclaredField("values").getType()));
    }

    @Test
    public void testScalaUtilWithIterable() throws Exception {
        class Test {
            Iterable<String> values;
        }
        Assert.assertTrue(Util.isListType(Test.class.getDeclaredField("values").getType()));
    }

    @Test
    public void testScalaUtilWithList() throws Exception {
        class Test {
            List<String> values;
        }
        Assert.assertTrue(Util.isListType(Test.class.getDeclaredField("values").getType()));
    }

    @Test
    @Ignore("Currently not supporting maps")
    public void testWithMap() throws Exception {
        class Test {
            Map<String, String> values;
        }
        Assert.assertTrue(Util.isListType(Test.class.getDeclaredField("values").getClass()));
    }
}
