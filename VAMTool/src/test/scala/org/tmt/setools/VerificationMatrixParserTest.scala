package org.tmt.setools

import org.scalatest.{FunSuite, Matchers}

class VerificationMatrixParserTest extends FunSuite with Matchers {

  test("should create map") {
    val map = VerificationMatrixParser.createStoryToReqMap()

    map.toList.sortBy(_._1).foreach {
      case (s, r) => println(s"${s.reference.reference} -> ${r.mkString(",")}")
    }
    println(s"map size = ${map.size}")
  }

  test("should parse req from string") {
    val testString = "[REQ-2-CSW-1234] Blah Blah"
    VerificationMatrixParser.reqPattern.findFirstIn(testString).get shouldBe "REQ-2-CSW-1234"
  }
}
