package org.tmt.setools

import org.scalatest.FunSuite

class DRDParserTest extends FunSuite {

  test("should parse DRD") {
    val reqs = DRDParser.getRequirements()
    reqs.foreach(println)
  }
}
