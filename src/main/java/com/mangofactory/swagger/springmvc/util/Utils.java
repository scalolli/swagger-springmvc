package com.mangofactory.swagger.springmvc.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: basu
 * Date: 30/1/13
 * Time: 10:41 PM
 * To change this template use File | Settings | File Templates.
 */
final public class Utils {

    private Utils() {
    }

    public static boolean isParametrizeListOfValues(Field field) {
        return isListType(field.getType()) && field.getGenericType() instanceof ParameterizedType;
    }

    public static boolean isListType(Class classType) {
        return Collection.class.isAssignableFrom(classType);
    }
}
