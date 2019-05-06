package org.jbdb.jsonpath

import java.lang.reflect.InvocationTargetException

trait TestCase {
  /*
  This is pretty hack, but I wasn't able to get scala test to work.
  Instead I just rolled my own bad testing framework
   */

  def main(args: Array[String]): Unit = {
    this.getClass.getMethods.foreach(
      m => {
        if (m.getName.startsWith("test")) {
          println()
          try {
            println(m.getName)
            m.invoke(this)
            println("Testcase " + m.getName + " passed!")
          } catch {
            case e: InvocationTargetException =>
              val cause = e.getCause
              println(Console.RED_B + "Testcase " + m.getName + " failed")
              println(Console.RED_B + "Error was: " + cause)
              cause.printStackTrace()
              print(Console.BLACK)
          }
        }
      }
    )
  }

  def check(expected: Any, actual: Any): Unit = {
    if (expected != actual) throw CheckFailed("expected: " + expected + ", actual: " + actual)
  }

  def checkException(expected: Class[_], actual: Any): Unit = {
    if (actual.getClass != expected) throw CheckFailed("expected: " + expected + ", actual: " + actual.getClass)
  }

  case class CheckFailed(message: String) extends RuntimeException(message, null)
}
