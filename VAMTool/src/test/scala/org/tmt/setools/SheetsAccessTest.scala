package org.tmt.setools

import org.scalatest.{FunSuite, Matchers}

class SheetsAccessTest extends FunSuite with Matchers {
  private val spreadsheetId = "1n6-R5x4Br7NFJ219zCexHbE34DZFRZTtLkuhjtHJNEc"
  private val sheetIds      = List("Logging Service - User Stories")

  private val data = SheetsAccess.getAllData(spreadsheetId, sheetIds.head)

  printData()

  private val firstValidRow = data.collectFirst {
    case row if row.size == 10 => row
  }.get

  test("should get data from verification matrix") {
    data.size should be > 0
  }

  test("should get user story id") {
    firstValidRow(9).toString shouldBe "DEOPSCSW-114"
  }

  test("should get requirements") {
    val reqInfo = firstValidRow(7).toString.split("\n")

    reqInfo.size shouldBe 3

    val pattern = "\\[.*?\\]".r

    val reqs = reqInfo.map(s => pattern.findFirstIn(s).getOrElse(""))

    reqs shouldBe Array("[REQ-2-CSW-3700]", "[REQ-2-CSW-3705]", "[REQ-2-CSW-3755]")
  }

  private def printData(): Unit = data.foreach(println)

}
