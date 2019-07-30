package org.tmt.setools

import org.tmt.setools.Utilities.Requirement

/**
 * Extracts data from a google doc: "CSW Unit Test Verification Matrix".
 * See example doc below.
 */
object DRDParser {
  // Example: https://docs.google.com/spreadsheets/d/1XxGE2USwKohWxLuZJUCPBdqNc5sXvE8S
  private val spreadsheetId = "180ZP3I9SnkOWjZycyR9Truf6Fszp1myfrLLnF-lCXsY"
  private val sheetId       = "Sheet1"
  private val idColumn      = 'A'
  private val textColumn    = 'B'
  private val idColumnNum   = idColumn - 'A'
  private val textColumnNum = textColumn - 'A'


  def getRequirements(spreadsheetId: String = spreadsheetId): List[Requirement] = {
    SheetsAccess
      .getAllData(spreadsheetId, sheetId)
      .drop(1)
      .map(row => Requirement(row(idColumnNum).toString.trim, row(textColumnNum).toString, verifiedByTestSuite = true))
  }
}
