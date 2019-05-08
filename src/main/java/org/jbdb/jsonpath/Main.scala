package org.jbdb.jsonpath

object Main {
  /*
     To use:
     arg 0 is the path expression
     arg > 0 are the json objects to test the path against
   */
  def main(args: Array[String]): Unit = {
    val path = new JsonPath(args(0))
    for (a <- args.tail) println(path.apply(a))
  }
}
