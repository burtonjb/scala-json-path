package org.jbdb.jsonpath

import org.json.{JSONArray, JSONObject}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

//TODO: add hashCode for all impls here. Otherwise they'll break in collections
trait PathComponent {
  def apply(obj: Any): Any = {
    obj match {
      case o: JSONObject => walkObject(o)
      case o: JSONArray => walkArray(o)
      case o: Number => walkNumber(o)
      case o: String => walkString(o)
      case o: List[Any] => Utils.flatten(o.map(a => apply(a)).filter(a => a != Nil && a != None && a != null))
      case _ => Nil
    }
  }

  def walkObject(jsonObject: JSONObject): Any = {
    None
  }

  def walkArray(jsonArray: JSONArray): Any = {
    None
  }

  def walkString(string: String): Any = {
    None
  }

  def walkNumber(number: Number): Any = {
    None
  }
}

class RemoveablePath extends PathComponent //Class that gets filtered out during parsing

class JsonObjectPath(path: String) extends PathComponent {
  val _path: String = path

  override def walkObject(jsonObject: JSONObject): Any = {
    jsonObject.opt(path)
  }

  override def equals(obj: Any): Boolean = obj match {
    case o: JsonObjectPath => o._path == this.path
    case _ => false
  }

  override def toString: String = {
    "(JsonObjectPath: " + path + ")"
  }
}

class MultiJsonObjectPath(paths: List[String]) extends PathComponent {
  val _paths: List[String] = paths

  override def walkObject(jsonObject: JSONObject): Any = {
    val out = paths.map(k => jsonObject.opt(k)).filter(a => Utils.notNull(a))
    if (out.isEmpty) None
    out
  }

  override def toString: String = {
    "(MultiJsonObjectPath: " + paths.toString + ")"
  }

  override def equals(obj: Any): Boolean = obj match {
    case o: MultiJsonObjectPath => o._paths == this.paths
    case _ => false
  }
}

class MultiJsonArrayPath(indexes: List[Int]) extends PathComponent {
  val _indexes: List[Int] = indexes

  override def walkArray(jsonArray: JSONArray): Any = {
    val out = indexes.map(i => jsonArray.get(i)).filter(a => Utils.notNull(a))
    if (out.isEmpty) None
    out
  }

  override def toString: String = {
    "(MultiJsonArrayPath: " + indexes.toString + ")"
  }

  override def equals(obj: Any): Boolean = obj match {
    case o: MultiJsonArrayPath => o._indexes == this.indexes
    case _ => false
  }
}

class JsonArraySlicePath(startIndex: Int, endIndex: Int, step: Int) extends PathComponent {
  val _startIndex: Int = startIndex
  val _endIndex: Int = endIndex
  val _step: Int = step

  override def walkArray(jsonArray: JSONArray): Any = {
    val range = startIndex until endIndex by step
    val out = range.map(i => jsonArray.get(i)).filter(a => Utils.notNull(a)).toList
    if (out.isEmpty) None
    out
  }

  override def toString: String = {
    "(JsonArraySlicePath: [" + startIndex + ":" + endIndex + ":" + step + "])"
  }

  override def equals(obj: Any): Boolean = obj match {
    case o: JsonArraySlicePath => o._startIndex == this.startIndex && o._endIndex == this.endIndex && o._step == this.step
    case _ => false
  }
}

class JsonArrayPath(index: Int) extends PathComponent {
  val _index: Int = index

  override def walkArray(jsonArray: JSONArray): Any = {
    jsonArray.opt(index)
  }

  override def toString: String = {
    "(JsonArrayPath: " + index.toString + ")"
  }

  override def equals(obj: Any): Boolean = obj match {
    case o: JsonArrayPath => o._index == this.index
    case _ => false
  }
}

class WildCardPath() extends PathComponent {
  override def walkObject(jsonObject: JSONObject): Any = asScalaSet(jsonObject.keySet()).map(k => jsonObject.get(k)).toList

  override def walkArray(jsonArray: JSONArray): Any = jsonArray.iterator().asScala.toList

  override def toString: String = {
    "(WildCardPath)"
  }

  override def equals(obj: Any): Boolean = obj match {
    case o: WildCardPath => true
    case _ => false
  }

}

class DeepSearchPath() extends WildCardPath {

  override def apply(obj: Any): Any = {
    //Basically DFS through all the objects
    val stack = ListBuffer[Any]()
    val outList = ListBuffer[Any]()
    stack.append(obj)
    outList.append(obj)
    while (stack.nonEmpty) {
      val obj = stack.remove(0)
      val o = super.apply(obj)
      o match {
        case None =>
        case o: List[Any] =>
          val f = o.filter(a => Utils.notNull(a))
          stack ++= f
          outList ++= f
        case _ =>
          stack.append(o)
          outList.append(o)
      }
    }
    Utils.flatten(outList.toList)
  }

  override def toString: String = {
    "(DeepSearchPath)"
  }

  override def equals(obj: Any): Boolean = obj match {
    case o: DeepSearchPath => true
    case _ => false
  }
}