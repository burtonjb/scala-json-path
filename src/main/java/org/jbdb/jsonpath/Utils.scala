package org.jbdb.jsonpath

object Utils {

  def flatten(ls: List[Any]): List[Any] = ls flatMap {
    case i: List[_] => flatten(i)
    case e => List(e)
  }

  def notNull(a: Any): Boolean = {
    a != Nil && a != None && a != null //TODO: clean this up. Are all these checks needed?
  }

}
