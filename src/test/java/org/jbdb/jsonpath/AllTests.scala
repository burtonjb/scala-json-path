package org.jbdb.jsonpath

object AllTests {
  val tests = List(JsonPathParserTest, PathComponentTest)

  def main(args: Array[String]): Unit = {
    tests.foreach(t => t.main(args))
  }
}
