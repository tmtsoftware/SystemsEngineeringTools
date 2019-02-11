package org.tmt.setools

import java.io.File

import org.scalatest.{FunSuite, Matchers}

class TestResultParserTest extends FunSuite with Matchers {
  val parser = new TestResultParser()

  test("should read csv file") {
    val inFile = new File("/Users/weiss/acceptTest/testResults.csv")

    parser.parseCSV(inFile).toList.foreach(println)

  }

  test("should get map from github") {
    parser.getResultMapFromGithub.toList.foreach(println)

  }

}
