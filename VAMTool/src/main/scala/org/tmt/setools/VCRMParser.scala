package org.tmt.setools

import org.tmt.setools.Utilities.Requirement

object VCRMParser {
  val spreadsheetId = "1qv5-aAWNt8t30RtFU6GSll2F0ZeP6arRB11Xj8ytmJU"
  val sheetId = "VCRM"
  val idRow = 1
  val textRow = 2
  val methodRow = 3

  private def isVerifiedByTestSuite(text: String) = {
    text.contains("Demonstration")
  }

  // https://docs.google.com/spreadsheets/d/1qv5-aAWNt8t30RtFU6GSll2F0ZeP6arRB11Xj8ytmJU
  def getRequirements(spreadsheetId: String = spreadsheetId): List[Requirement] = {
    SheetsAccess.getAllData(spreadsheetId, sheetId)
      .filter(_.size > 3)
      .map(row => Requirement(row(idRow).toString, row(textRow).toString, isVerifiedByTestSuite(row(methodRow).toString)))
  }
}
