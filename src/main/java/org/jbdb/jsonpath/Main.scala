package org.jbdb.jsonpath

import org.json.JSONObject

object Main {
  val jsonString = "{'test': {'sub': 'v', 'sub2': 'v2'}, 'other': {'sub': 'other_v'}, 'null': null}"

  def main(args: Array[String]): Unit = {
    val path = "$.null"
    val paths = JsonPathParser.parsePath(path)
    println(paths)
    val obj = new JSONObject(jsonString)
    println(walk(obj, paths))
  }

  //TODO: change the parser to return a jsonpath object with one method - walk (or maybe apply)
  // the methods should be like: jsonPath.walk(string) or jsonPath.walk(JSONObject) or jsonPath.walk(JSONArray)
  // which basically do this
  def walk(root: JSONObject, paths: List[PathComponent]): Any = {
    var current: Any = root
    for (p <- paths) current = p.walk(current)
    current
  }
}
