package org.tmt.setools

import java.io.File

import org.scalatest.{FunSuite, Matchers}

class TestResultParserTest extends FunSuite with Matchers {
  val HOME = System.getProperty("user.home")
  val parser = new TestResultParser()

  test("should read csv file") {
    val inFile = new File(s"$HOME/acceptTest/testResults.csv")

    parser.parseCSV(inFile).toList.foreach(println)

  }

  test("should get map from github") {
    parser.getResultMapFromGithub.toList.foreach(println)

  }

}
