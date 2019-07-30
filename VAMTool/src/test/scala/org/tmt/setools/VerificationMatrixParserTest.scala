package org.tmt.setools

import java.io.{File, PrintWriter}

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

  test ("should create req to story map") {
    val allReqs = DRDParser.getRequirements()
    val map = VerificationMatrixParser.createReqToStoryMap(allReqs)

    map.toList.sortBy(_._1.id).foreach {
      case (r, s) => println(s"${r.id} -> ${s.map(_.reference).mkString(",")}")
    }
  }

  test ("should create req to story map and write to file") {
    val allReqs = DRDParser.getRequirements()
    val map = VerificationMatrixParser.createReqToStoryMap(allReqs)

    val file = new File("/tmp/reqToStoryMap.tsv")

    val writer = new PrintWriter(file)
    map.toList.sortBy(_._1.id).foreach {
      case (r, s) => writer.println(s"${r.id}\t${'"'}${r.fullText}${'"'}\t${'"'}${s.map(r => s"${r.reference.reference}: ${r.getText}").mkString("\n")}${'"'}")
    }
    writer.close()
  }

}
