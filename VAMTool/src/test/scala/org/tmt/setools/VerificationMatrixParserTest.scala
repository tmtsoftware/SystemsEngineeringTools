package org.tmt.setools

import org.scalatest.{FunSuite, Matchers}
import org.tmt.setools.VerificationMatrixParser.reqPattern

class VerificationMatrixParserTest extends FunSuite with Matchers {

  test("should create map") {
    val map = VerificationMatrixParser.createReqToStoryMap()

    printSortedMap(map)
    println(s"map size = ${map.size}")
  }

  test("should parse req from string") {
    val testString = "[REQ-2-CSW-1234] Blah Blah"
    VerificationMatrixParser.reqPattern.findFirstIn(testString).get shouldBe "REQ-2-CSW-1234"
  }

  def printSortedMap(map: Map[String, Set[String]]): Unit = {
    for ((t, s) <- map.toSeq.sortBy(_._1.slice(11, 15).toInt)) {
      println(s"$t: -> ${s.mkString(",")}")
    }
  }
}
