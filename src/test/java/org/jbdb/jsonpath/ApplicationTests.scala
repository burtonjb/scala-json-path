package org.jbdb.jsonpath

import org.json.JSONObject

object ApplicationTests extends TestCase {
  def testOriginal(): Unit = {
    //Tests from Goessner's original write up https://goessner.net/articles/JsonPath/

    val json =
      """
        {
          "store" : {
            "book" : [{
                "category" : "reference",
                "author" : "Nigel Rees",
                "title" : "Sayings of the Century",
                "price" : 8.95
              }, {
                "category" : "fiction",
                "author" : "Evelyn Waugh",
                "title" : "Sword of Honour",
                "price" : 12.99
              }, {
                "category" : "fiction",
                "author" : "Herman Melville",
                "title" : "Moby Dick",
                "isbn" : "0-553-21311-3",
                "price" : 8.99
              }, {
                "category" : "fiction",
                "author" : "J. R. R. Tolkien",
                "title" : "The Lord of the Rings",
                "isbn" : "0-395-19395-8",
                "price" : 22.99
              }
            ],
            "bicycle" : {
              "color" : "red",
              "price" : 19.95
            }
          }}
      """

    check(List("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"), new JsonPath("$.store.book.*.author").apply(json))
    check(List("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"), new JsonPath("$..author").apply(json))
//    check("List([{\"author\":\"Nigel Rees\",\"price\":8.95,\"category\":\"reference\",\"title\":\"Sayings of the Century\"},{\"author\":\"Evelyn Waugh\",\"price\":12.99,\"category\":\"fiction\",\"title\":\"Sword of Honour\"},{\"author\":\"Herman Melville\",\"price\":8.99,\"isbn\":\"0-553-21311-3\",\"category\":\"fiction\",\"title\":\"Moby Dick\"},{\"author\":\"J. R. R. Tolkien\",\"price\":22.99,\"isbn\":\"0-395-19395-8\",\"category\":\"fiction\",\"title\":\"The Lord of the Rings\"}], {\"color\":\"red\",\"price\":19.95})", new JsonPath("$.store.*").apply(json).toString)
    check(List(19.95, 8.95, 12.99, 8.99, 22.99), new JsonPath("$.store..price").apply(json))
    check("List({\"author\":\"Herman Melville\",\"price\":8.99,\"isbn\":\"0-553-21311-3\",\"category\":\"fiction\",\"title\":\"Moby Dick\"})", new JsonPath("$..book[2]").apply(json).toString)
    //TODO: add the rest of the test cases
  }


  //Test cases pulled from: https://github.com/gregsdennis/JSON-Path-Test-Suite/
  def testDotOperator(): Unit = {
    val json =
      """
        {
        		"firstName" : "John",
        		"lastName" : "Doe",
        		"home" : "123-456-7890",
        		"mobile" : null
        }
      """

    check("John", new JsonPath("$.firstName").apply(json))
    check("Doe", new JsonPath("$.lastName").apply(json))
    check("123-456-7890", new JsonPath("$.home").apply(json))
    check(JSONObject.NULL, new JsonPath("$.mobile").apply(json))
    check(null, new JsonPath("$.missingKey").apply(json))
    check(List(JSONObject.NULL, "123-456-7890", "John", "Doe"), new JsonPath("$.*").apply(json))
  }

  def testDeepSearchOperator(): Unit = {
    val json =
      """
        {
          "firstName" : "John",
          "lastName" : "Doe",
          "eyes" : "blue",
          "children" : [{
            "firstName" : "Sally",
            "lastName" : "Doe",
            "favoriteGames" : ["Halo", "Minecraft", "Lego: Star Wars"]
          }, {
            "firstName" : "Mike",
            "lastName" : "Doe",
            "eyes" : "green"
          }]
        }
      """

    check(List("John", "Sally", "Mike"), new JsonPath("$..firstName").apply(json))
    check(List("Doe", "Doe", "Doe"), new JsonPath("$..lastName").apply(json))
    check(List("blue", "green"), new JsonPath("$..eyes").apply(json))
    check(List(), new JsonPath("$..missingKey").apply(json))

    check(2, new JsonPath("$..[1]").apply(json).asInstanceOf[List[Any]].size)
    check(true, new JSONObject("{\"firstName\":\"Mike\",\"lastName\":\"Doe\",\"eyes\":\"green\"}")
      .similar(new JsonPath("$..[1]").apply(json).asInstanceOf[List[Any]].head))
    check("Minecraft", new JsonPath("$..[1]").apply(json).asInstanceOf[List[Any]](1))
  }

  def testSubarrayOperator(): Unit = {
    val json =
      """
        [
          "John Doe",
          36,
          "Architect",
          "one",
          "three",
          "five"
        ]
      """

    check("Architect", new JsonPath("$[2]").apply(json))
    check(null, new JsonPath("$[10]").apply(json))
    check(List(36, "Architect", "one"), new JsonPath("$[1:4]").apply(json))
    check(List(36, "one"), new JsonPath("$[1:4:2]").apply(json))
    check(List("John Doe", "one"), new JsonPath("$[0, 3]").apply(json))
    check(List("John Doe", 36, "Architect", "one", "three", "five"), new JsonPath("$.*").apply(json))
  }
}
