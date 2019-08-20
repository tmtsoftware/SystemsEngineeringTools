package org.tmt.setools

import org.tmt.setools.Utilities.{Requirement, UserStory, UserStoryReference}

import scala.util.matching.Regex

/**
 * Extracts data from a given google doc in the format of a "CSW Unit Test Verification Matrix".
 * See example spreadsheetId below.
 */
object VerificationMatrixParser {

  // Example input: https://docs.google.com/spreadsheets/d/1n6-R5x4Br7NFJ219zCexHbE34DZFRZTtLkuhjtHJNEc
  private val spreadsheetId    = "1LB7elq-mIcpG4jvTirn7qUjKDE3ucpMA-Srll5QpeVI"
  val reqPattern: Regex        = "(?<=\\[)(.*?)(?=\\])".r
  private val storyPattern     = "[A-Z0-9\\-]*".r
  private val asAColumn        = 'D'
  private val iWantToColumn    = 'E'
  private val soThatColumn     = 'F'
  private val reqColumn        = 'H'
  private val storyColumn      = 'J'
  private val validRowSize     = 10
  private val reqColumnNum     = reqColumn - 'A'
  private val storyColumnNum   = storyColumn - 'A'
  private val asAColumnNum     = asAColumn - 'A'
  private val iWantToColumnNum = iWantToColumn - 'A'
  private val soThatColumnNum  = soThatColumn - 'A'

  private val sheetIds = List(
    "Location Service",
    "Configuration Service",
    "Logging Service",
    "Logging Aggregator",
    "Framework",
    "Command Service",
    "Event Service",
    "Alarm Service",
    "AAS",
    "Time Service",
    "Database Service"
  )

  def getReqs(row: List[Any]): Set[String] = row(reqColumnNum).toString.split("\n").flatMap(s => reqPattern.findFirstIn(s)).toSet
  def getStory(row: List[Any]): String = storyPattern.findFirstIn(row(storyColumnNum).toString).getOrElse("")

  def createStoryToReqMap(spreadsheetId: String = spreadsheetId): Map[UserStory, Set[String]] = {
    sheetIds.flatMap { sheet =>
      SheetsAccess
        .getAllData(spreadsheetId, sheet)
        .filter(_.size >= validRowSize)
        .filter(getReqs(_).nonEmpty)
        .filter(getStory(_).nonEmpty)
        .map { row =>
          UserStory(UserStoryReference(getStory(row)), sheet, row(asAColumnNum).toString.trim, row(iWantToColumnNum).toString.trim, row(soThatColumnNum).toString.trim) -> getReqs(row)
        }
    }.toMap
  }

  def createReqToStoryMap(allReqs: List[Requirement], matrixSheetId: String = spreadsheetId): Map[Requirement, List[UserStory]] = {
    val storyToReqMap = createStoryToReqMap()
    val partialReqToStoryMap = Utilities.invertMap(storyToReqMap)
    allReqs.map { req =>
      req -> partialReqToStoryMap.getOrElse(req.id, List(UserStory.none))
    }.toMap
  }
}
