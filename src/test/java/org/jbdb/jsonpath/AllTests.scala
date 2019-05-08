package org.jbdb.jsonpath

object AllTests {
  val tests = List(JsonPathParserTest, PathComponentTest, ApplicationTests)

  def main(args: Array[String]): Unit = {
    tests.foreach(t => t.main(args))
  }
}
