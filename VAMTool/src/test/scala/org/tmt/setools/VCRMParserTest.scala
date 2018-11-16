package org.tmt.setools

import org.scalatest.FunSuite

class VCRMParserTest extends FunSuite {

  test("should parse VCRM") {
    val reqs = VCRMParser.getRequirements()
    reqs.foreach(println)
  }
}
