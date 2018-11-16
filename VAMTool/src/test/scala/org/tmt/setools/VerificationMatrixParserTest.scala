package org.tmt.setools

import org.scalatest.{FunSuite, Matchers}

class VerificationMatrixParserTest extends FunSuite with Matchers {

  test("should create map") {
    val map = VerificationMatrixParser.createMap()

    map.foreach(println)
  }
}
