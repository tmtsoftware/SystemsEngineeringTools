package org.tmt.setools

import org.tmt.setools.Utilities.{UserStory, UserStoryReference}

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


  // TODO: Add other sheet ids
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

  def createStoryToReqMap(spreadsheetId: String = spreadsheetId): Map[UserStory, Set[String]] = {
    var storyToReqMap = Map[UserStory, Set[String]]()

    sheetIds.foreach { sheet =>
      val data = SheetsAccess.getAllData(spreadsheetId, sheet)
      data
        .filter(_.size >= validRowSize)
        .foreach { row =>
          val reqStrings = row(reqColumnNum).toString.split("\n").toSet
          val reqs = reqStrings.flatMap(s => reqPattern.findFirstIn(s))
          val story = storyPattern.findFirstIn(row(storyColumnNum).toString).getOrElse("")
          if (story.nonEmpty) {
            if (reqs.nonEmpty) {
              storyToReqMap = storyToReqMap + (UserStory(UserStoryReference(story), sheet, row(asAColumnNum).toString, row(iWantToColumnNum).toString, row(soThatColumnNum).toString) -> reqs)
            }
          }
        }
    }
    storyToReqMap
  }

  /**
   * Returns a map requirement id to set of user story ids.
   * For example: Map("[REQ-2-CSW-3795]" -> Set("DEOPSCSW-145", "DEOPSCSW-146", "DEOPSCSW-147"), ...)
   */
  def createReqToStoryMap(spreadsheetId: String = spreadsheetId): Map[String, Set[String]] = {
    var reqToStoryMap = Map[String, Set[String]]()

    sheetIds.foreach { sheet =>
      val data = SheetsAccess.getAllData(spreadsheetId, sheet)
      data
        .filter(_.size >= validRowSize)
        .foreach { row =>
          val reqStrings = row(reqColumnNum).toString.split("\n")
          val reqs       = reqStrings.flatMap(s => reqPattern.findFirstIn(s))
          val story      = storyPattern.findFirstIn(row(storyColumnNum).toString).getOrElse("")
          if (story.nonEmpty) {
            reqs.foreach { r =>
              if (reqToStoryMap.contains(r)) {
                reqToStoryMap = reqToStoryMap + (r -> (reqToStoryMap(r) + story))
              } else {
                reqToStoryMap = reqToStoryMap + (r -> Set(story))
              }
            }
          }
        }
    }
    reqToStoryMap
  }

}
