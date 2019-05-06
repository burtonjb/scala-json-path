package org.jbdb.jsonpath

import org.json.{JSONArray, JSONObject}

object PathComponentTest extends TestCase {

  val jsonObject = new JSONObject("{'key':'value', 'key2': 'value2', 'key3': 'v3'}")
  val jsonArray = new JSONArray("[1, 2, 3]")
  val number = 123
  val string = "test string"

  def testJsonObjectPath(): Unit = {
    val toTest = new JsonObjectPath("key")

    check("value", toTest.walk(jsonObject))
    check(None, toTest.walk(jsonArray))
    check(None, toTest.walk(string))
    check(None, toTest.walk(number))
    check(List("value"), toTest.walk(List(jsonObject, jsonArray, string, number)))
  }

  def testJsonArrayPath(): Unit = {
    val toTest = new JsonArrayPath(1)

    check(None, toTest.walk(jsonObject))
    check(2, toTest.walk(jsonArray))
    check(None, toTest.walk(string))
    check(None, toTest.walk(number))
    check(List(2), toTest.walk(List(jsonObject, jsonArray, string, number)))

    val toTest2 = new JsonArrayPath(2)
    val testArray = new JSONArray("[0, 1, {\"key\": \"value\"}]")
    check(true, new JSONObject("{\"key\": \"value\"}").similar(toTest2.walk(testArray)))
  }

  def testMultiJsonObjectPath(): Unit = {
    val toTest = new MultiJsonObjectPath(List("key", "key2"))

    check(List("value", "value2"), toTest.walk(jsonObject))
    check(None, toTest.walk(jsonArray))
    check(None, toTest.walk(string))
    check(None, toTest.walk(number))
    check(List("value", "value2"), toTest.walk(List(jsonObject, jsonArray, string, number)))
  }

  def testMultiJsonArrayPath(): Unit = {
    val toTest = new MultiJsonArrayPath(List(0, 2))

    check(None, toTest.walk(jsonObject))
    check(List(1, 3), toTest.walk(jsonArray))
    check(None, toTest.walk(string))
    check(None, toTest.walk(number))
    check(List(1, 3), toTest.walk(List(jsonObject, jsonArray, string, number)))
  }

  def testJsonArraySlicePath(): Unit = {
    val toTest = new JsonArraySlicePath(0, 1, 1)

    check(None, toTest.walk(jsonObject))
    check(List(1, 2), toTest.walk(jsonArray))
    check(None, toTest.walk(string))
    check(None, toTest.walk(number))
    check(List(1, 2), toTest.walk(List(jsonObject, jsonArray, string, number)))

    val toTest2 = new JsonArraySlicePath(2, 5, 3)
    val jsonArray2 = new JSONArray("[0, 1, 2, 3, 4, 5, 6, 7, 8]")
    check(List(2, 5), toTest2.walk(jsonArray2))
  }


  def testWildCardPath(): Unit = {
    val toTest = new WildCardPath

    check(List("value2", "v3", "value"), toTest.walk(jsonObject)) //There is an unordered set being used in the impl, so if the ordering breaks convert actual and expected to sets to compare
    check(List(1, 2, 3), toTest.walk(jsonArray))
    check(None, toTest.walk(string))
    check(None, toTest.walk(number))
    check(List("value2", "v3", "value", 1, 2, 3), toTest.walk(List(jsonObject, jsonArray, string, number)))
  }

  def testDeepSearchPath(): Unit = {
    val toTest = new DeepSearchPath

    check(true, List(jsonObject).head.similar(toTest.walk(jsonObject).asInstanceOf[List[Any]].head)) //There is an unordered set being used in the impl, so if the ordering breaks convert actual and expected to sets to compare
    check(List(jsonArray, 1, 2, 3), toTest.walk(jsonArray))
    check(List(string), toTest.walk(string))
    check(List(number), toTest.walk(number))

    val wc = new WildCardPath()
    val testObj = new JSONObject("{\"root\": {\"key\": \"value\", \"ar\": [0, 1, {\"ar_nested\": \"ar_val\"}]}, \"n\": {\"k_1\": \"v_1\", \"nn\": {\"nn_1\": \"vv_1\"}}}")
    val actual = wc.walk(toTest.walk(testObj))
    check(11, actual.asInstanceOf[List[Any]].length)
  }

}
