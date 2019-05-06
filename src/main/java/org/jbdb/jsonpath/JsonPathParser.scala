package org.jbdb.jsonpath

import scala.collection.mutable.ListBuffer

object JsonPathParser {

  private val quoteTokens = List('"', '\'')

  def parsePath(path: String): List[PathComponent] = {
    val isValid = JsonPathParser.validate(path)
    if (!isValid) return List()
    val tokens = JsonPathParser.lex(path)
    JsonPathParser.parseTokens(tokens)
  }

  private def validate(path: String) = true

  private def lex(path: String): List[ParseType] = {
    val p = path.trim
    val tokens = new ListBuffer[ParseType]
    var sb = new StringBuilder
    var state = states.default
    var currentAtoms = Atoms(new ListBuffer[Atom])
    for (c <- p) {
      state match {
        case states.default =>
          if (state == states.default) {
            if (c == '.') {
              tokens.append(Atom(sb.toString(), isNumber = false))
              sb.clear()
              state = states.dot
            }
            else if (c == '[') {
              state = states.open_square
              tokens.append(Atom(sb.toString(), isNumber = false))
              sb.clear()
            }
            else {
              sb.append(c)
            }
          }
        case states.dot =>
          if (c == '.') {
            state = states.default
            tokens.append(Atom("..", isNumber = false))
            sb.clear()
          }
          else {
            state = states.default
            sb.append(c)
          }
        case states.open_square =>
          currentAtoms = Atoms(new ListBuffer[Atom])
          if (quoteTokens.contains(c)) {
            state = states.open_quote
            sb.clear()
          }
          else if (c.isDigit) {
            state = states.digit
            sb.append(c)
          }
          else {
            throw JsonPathParserException("Unexpected character after [. Char was: " + c)
          }
        case states.digit =>
          if (c.isDigit) {
            sb.append(c)
          }
          else if (c == ',') {
            state = states.digit_comma
            currentAtoms.append(Atom(sb.toString(), isNumber = true))
            sb.clear()
          }
          else if (c == ':') {
            currentAtoms._isSlice = true
            currentAtoms.append(Atom(sb.toString(), isNumber = true))
            sb.clear()
          }
          else if (c == ']') {
            state = states.default
            currentAtoms.append(Atom(sb.toString(), isNumber = true))
            tokens.append(currentAtoms)
            sb.clear()
          }
        case states.digit_comma =>
          if (currentAtoms._isSlice) throw JsonPathParserException("Unexpected comma in slice expression")
          else if (c.isDigit) {
            sb.append(c)
            state = states.digit
          }
          else if (c.isWhitespace) {}
          else {
            throw JsonPathParserException("Unexpected character in multi-index query. Char was: " + c)
          }
        case states.open_quote =>
          if (quoteTokens.contains(c)) {
            state = states.close_quote
            currentAtoms.append(Atom(sb.toString(), isNumber = false))
            sb.clear()
          }
          else {
            sb.append(c)
          }
        case states.close_quote =>
          if (c == ']') {
            state = states.default
            tokens.append(currentAtoms)
            sb.clear()
          }
          else if (c.isWhitespace) {}
          else if (c == ',') {
            state = states.quote_comma
          }
          else {
            throw JsonPathParserException("Unexpected character after closing a quote. Char was: " + c)
          }
        case states.quote_comma =>
          if (quoteTokens.contains(c)) {
            state = states.open_quote
          }
          else if (c.isWhitespace) {}
          else {
            throw JsonPathParserException("Unexpected character in multi-key query. Char was: " + c)
          }
      }
    }
    if (state != states.default) throw new RuntimeException("Failed to close characters. Buffer is: " + sb.toString())
    tokens.append(Atom(sb.toString(), isNumber = false))
    tokens.toList
  }

  private def parseTokens(tokens: List[ParseType]): List[PathComponent] = {
    tokens.map(s => convertToPath(s)).filter(p => !p.isInstanceOf[RemoveablePath])
  }

  private def convertToPath(token: ParseType): PathComponent = {
    token match {
      case t: Atom => convertAtomToPath(t)
      case t: Atoms =>
        if (t.atoms.isEmpty) throw JsonPathParserException("Error")
        else if (t.atoms.length == 1) convertAtomToPath(t.atoms.head)
        else if (t.allInts()) {
          if (t._isSlice) {
            if (t.atoms.length < 2 || t.atoms.length > 3) throw JsonPathParserException("Error")
            new JsonArraySlicePath(t.atoms.head.token.toInt, t.atoms(1).token.toInt, if (t.atoms.length == 3) t.atoms(2).token.toInt else 1)
          } else {
            new MultiJsonArrayPath(t.atoms.map(t => t.token.toInt).toList)
          }
        }
        else if (t.allStrings()) new MultiJsonObjectPath(t.atoms.map(t => t.token).toList)
        else throw JsonPathParserException("Error, mixed types")
    }
  }

  private def convertAtomToPath(atom: Atom): PathComponent = {
    atom.token match {
      case "$" => new RemoveablePath //FIXME: replace this with a 'root returner' class
      case ".." => new DeepSearchPath
      case i if atom.isNumber => new JsonArrayPath(i.toInt)
      case "*" => new WildCardPath
      case z if z.length > 0 => new JsonObjectPath(atom.token)
      case _ => new RemoveablePath
    }
  }

  private abstract class ParseType

  final case class JsonPathParserException(message: String = "") extends RuntimeException(message)

  private case class Atom(token: String, isNumber: Boolean) extends ParseType

  private case class Atoms(atoms: ListBuffer[Atom] = new ListBuffer[Atom]) extends ParseType {
    val _tokens: ListBuffer[Atom] = atoms
    var _isSlice = false

    def append(atom: Atom): Unit = {
      atoms.append(atom)
    }

    def allInts(): Boolean = {
      atoms.forall(a => a.isNumber)
    }

    def allStrings(): Boolean = {
      atoms.forall(a => !a.isNumber)
    }
  }

  private object states extends Enumeration {
    type state = Value
    val default, dot, open_square, open_quote, digit, digit_comma, close_quote, quote_comma, slice = Value
  }

}
