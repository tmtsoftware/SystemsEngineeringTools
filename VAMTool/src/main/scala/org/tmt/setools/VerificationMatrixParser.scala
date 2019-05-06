package org.tmt.setools

object VerificationMatrixParser {

  private val spreadsheetId = "1n6-R5x4Br7NFJ219zCexHbE34DZFRZTtLkuhjtHJNEc"
  private val reqPattern = "\\[.*?\\]".r
  private val storyPattern = "[A-Z0-9\\-]*".r
  private val reqColumn = 7
  private val storyColumn = 9
  private val validRowSize = 10

  // TODO: Add other sheet ids
  private val sheetIds = List("Configuration Service - User St", "Logging Service - User Stories")

  // https://docs.google.com/spreadsheets/d/1n6-R5x4Br7NFJ219zCexHbE34DZFRZTtLkuhjtHJNEc
  def createMap(spreadsheetId: String = spreadsheetId): Map[String, Set[String]] = {
    var reqToStoryMap = Map[String, Set[String]]()

    sheetIds.foreach { sheet =>
      val data = SheetsAccess.getAllData(spreadsheetId, sheet)
      data
        .filter(_.size == validRowSize)
        .foreach { row =>
          val reqStrings = row(7).toString.split("\n")
          val reqs = reqStrings.flatMap(s => reqPattern.findFirstIn(s))
          val story = storyPattern.findFirstIn(row(9).toString).getOrElse("")
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
