package org.tmt.setools

import org.tmt.setools.Utilities.Requirement

/**
 * Extracts data from a google doc: "CSW DRD CCR16".
 * See example doc below.
 */
object DRDParser {
  // Example: https://docs.google.com/spreadsheets/d/1jzcBOOz2SoN9njfrm-V8BR7WWLwhOZoxZSv3CNRqsZ8
  private val spreadsheetId_CCR15 = "180ZP3I9SnkOWjZycyR9Truf6Fszp1myfrLLnF-lCXsY"
  private val spreadsheetId_CCR16 = "1jzcBOOz2SoN9njfrm-V8BR7WWLwhOZoxZSv3CNRqsZ8"
  private val defaultSpreadsheet = spreadsheetId_CCR16
  private val sheetId       = "Sheet1"
  private val idColumn      = 'A'
  private val textColumn    = 'B'
  private val idColumnNum   = idColumn - 'A'
  private val textColumnNum = textColumn - 'A'


  def getRequirements(spreadsheetId: String = defaultSpreadsheet): List[Requirement] = {
    SheetsAccess
      .getAllData(spreadsheetId, sheetId)
      .drop(1)
      .map(row => Requirement(row(idColumnNum).toString.trim, row(textColumnNum).toString, verifiedByTestSuite = true))
  }
}
