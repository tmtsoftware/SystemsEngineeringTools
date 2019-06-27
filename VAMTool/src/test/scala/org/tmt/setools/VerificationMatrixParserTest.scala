package org.tmt.setools

import org.scalatest.{FunSuite, Matchers}

class VerificationMatrixParserTest extends FunSuite with Matchers {

  test("should create map") {
    val map = VerificationMatrixParser.createMap()

    printSortedMap(map)
    println(s"map size = ${map.size}")
  }

  def printSortedMap(map: Map[String, Set[String]]): Unit = {
    for ((t, s) <- map.toSeq.sortBy(_._1.slice(11, 15).toInt)) {
      println(s"$t: -> ${s.mkString(",")}")
    }
  }
}
