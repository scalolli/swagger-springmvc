package com.mangofactory.swagger.springmvc.util

import java.util.Collection
import java.util

/**
 * Created with IntelliJ IDEA.
 * User: xpanxion
 * Date: 1/31/13
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
object Util {

  def  isListType[E](classType:Class[E]) : Boolean = {
    classOf[java.lang.Iterable[E]].isAssignableFrom(classType)
  }

}
