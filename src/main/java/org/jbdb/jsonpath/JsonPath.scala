package org.jbdb.jsonpath

import org.jbdb.jsonpath.JsonPathParser.JsonPathParserException
import org.json.{JSONArray, JSONObject}

/*
Usage:
val jp = new JsonPath(pathExpression)
jp.apply(jsonObject) -> get result from jsonPath
 */
class JsonPath(jsonPath: String) {
  val _jsonPath: String = jsonPath
  val _pathComponents: List[PathComponent] = JsonPathParser(jsonPath)

  def apply(root: String): Any = {
    // Check if the string is an array or object
    if (root.trim().startsWith("{")) apply(new JSONObject(root))
    else if (root.trim().startsWith("[")) apply(new JSONArray(root))
    else throw JsonPathParserException("Could not parse json object from: " + root)
  }

  def apply(root: JSONArray): Any = {
    var current: Any = root
    for (p <- _pathComponents) current = p.apply(current)
    current
  }

  def apply(root: JSONObject): Any = {
    var current: Any = root
    for (p <- _pathComponents) current = p.apply(current)
    current
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case o: JsonPath => o._jsonPath == this.jsonPath
      case _ => false
    }
  }

  override def hashCode(): Int = jsonPath.hashCode

  override def toString: String = jsonPath
}
