package org.jbdb.jsonpath

object JsonPathParserTest extends TestCase { //FIXME: rename some of these test cases

  def testParsePath_emptyString(): Unit = {
    val testString = ""
    val out = JsonPathParser.apply(testString)
    val expected = List()
    check(expected, out)
  }

  def testParsePath_$(): Unit = {
    val testString = "$"
    val out = JsonPathParser.apply(testString)
    val expected = List()
    check(expected, out)
  }

  def testParsePath_$test(): Unit = {
    val testString = "$.test"
    val out = JsonPathParser.apply(testString)
    val expected = List(new JsonObjectPath("test"))
    check(expected, out)
  }

  def testParsePath_testtest2(): Unit = {
    val testString = "test.test2"
    val out = JsonPathParser.apply(testString)
    val expected = List(new JsonObjectPath("test"), new JsonObjectPath("test2"))
    check(expected, out)
  }

  def testParsePath_0(): Unit = {
    val testString = "0"
    val out = JsonPathParser.apply(testString)
    val expected = List(new JsonObjectPath("0"))
    check(expected, out)
  }

  def testParsePath_test0(): Unit = {
    val testString = "test[0]"
    val out = JsonPathParser.apply(testString)
    val expected = List(new JsonObjectPath("test"), new JsonArrayPath(0))
    check(expected, out)
  }

  def testParsePath_0test(): Unit = {
    val testString = "[0]test"
    val out = JsonPathParser.apply(testString)
    val expected = List(new JsonArrayPath(0), new JsonObjectPath("test"))
    check(expected, out)
  }

  def testParsePath_wc(): Unit = {
    val testString = "*"
    val out = JsonPathParser.apply(testString)
    val expected = List(new WildCardPath)
    check(expected, out)
  }

  def testParsePath_wctest(): Unit = {
    val testString = "*.test"
    val out = JsonPathParser.apply(testString)
    val expected = List(new WildCardPath, new JsonObjectPath("test"))
    check(expected, out)
  }

  def testParsePath_testwc(): Unit = {
    val testString = "[10]*"
    val out = JsonPathParser.apply(testString)
    val expected = List(new JsonArrayPath(10), new WildCardPath)
    check(expected, out)
  }

  def testParsePath_$testwctest2(): Unit = {
    val testString = "$.key.*[2]"
    val out = JsonPathParser.apply(testString)
    val expected = List(new JsonObjectPath("key"), new WildCardPath, new JsonArrayPath(2))
    check(expected, out)
  }

}
