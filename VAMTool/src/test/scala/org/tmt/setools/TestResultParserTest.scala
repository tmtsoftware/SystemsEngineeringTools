package org.tmt.setools

import java.io.File

import org.scalatest.{FunSuite, Matchers}

class TestResultParserTest extends FunSuite with Matchers {
  val HOME: String = System.getProperty("user.home")

  test("should read csv file") {
    val inFile = new File(s"$HOME/acceptTest/testResults.csv")

    TestResultParser.parseCSV(inFile).toList.foreach(println)

  }

  test("should get map from github") {
    TestResultParser.getResultMapFromGithub.toList.foreach(println)

  }

}
