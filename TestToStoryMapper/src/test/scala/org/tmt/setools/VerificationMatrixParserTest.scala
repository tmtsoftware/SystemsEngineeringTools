package org.tmt.setools

import org.scalatest.{FunSuite, Matchers}

class VerificationMatrixParserTest extends FunSuite with Matchers {
  val parser = new VerificationMatrixParser()

  test("should create map") {
    val map = parser.createMap()

    map.foreach(println)
  }
}
