package org.tmt.setools

object VerificationMatrixParser {

  private val reqPattern = "\\[.*?\\]".r
  private val reqColumn = 7
  private val storyColumn = 9
  private val validRowSize = 10
  private val sheetIds = List("Configuration Service - User St", "Logging Service - User Stories")

  // https://docs.google.com/spreadsheets/d/1n6-R5x4Br7NFJ219zCexHbE34DZFRZTtLkuhjtHJNEc
  def createMap(spreadsheetId: String = "1n6-R5x4Br7NFJ219zCexHbE34DZFRZTtLkuhjtHJNEc"): Map[String, Set[String]] = {
    var reqToStoryMap = Map[String, Set[String]]()

    sheetIds.foreach { sheet =>
      val data = SheetsAccess.getAllData(spreadsheetId, sheet)
      data
        .filter(_.size == validRowSize)
        .foreach { row =>
          val reqStrings = row(7).toString.split("\n")
          val reqs = reqStrings.map(s => reqPattern.findFirstIn(s))
          val story = row(9).toString
          if (story.nonEmpty) {
            reqs.foreach { req =>
              req.foreach { r =>
                if (reqToStoryMap.contains(r)) {
                  reqToStoryMap = reqToStoryMap + (r -> (reqToStoryMap(r) + story))
                } else {
                  reqToStoryMap = reqToStoryMap + (r -> Set(story))
                }
              }
            }
          }
      }
    }
    reqToStoryMap
  }

}
