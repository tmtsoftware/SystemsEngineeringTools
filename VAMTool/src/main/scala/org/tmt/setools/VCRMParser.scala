package org.tmt.setools

import org.tmt.setools.utilities.Requirement

import scala.collection.mutable


object VCRMParser {

  private def isVerifiedByTestSuite(text: String) = {
    text.contains("Demonstration")
  }
  // https://docs.google.com/spreadsheets/d/1qv5-aAWNt8t30RtFU6GSll2F0ZeP6arRB11Xj8ytmJU
  def getRequirements(spreadsheetId: String = "1qv5-aAWNt8t30RtFU6GSll2F0ZeP6arRB11Xj8ytmJU"): List[Requirement]  = {
    val sheetId = "VCRM"
    val idRow = 1
    val textRow = 2
    val methodRow = 3

    val requirements = mutable.ListBuffer[Requirement]()

    val sheets = new SheetsAccess()
    val data = sheets.getAllDataScala(spreadsheetId, sheetId)
    data.foreach { row =>
      if (row.size > 3) {
        requirements += Requirement(row(idRow).toString, row(textRow).toString, isVerifiedByTestSuite(row(methodRow).toString))
      }
    }
    requirements.toList
  }

}
