package org.tmt.setools

import scala.collection.mutable

class VerificationMatrixParser {
  private val sheets = new SheetsAccess()

  private val spreadsheetId = "1n6-R5x4Br7NFJ219zCexHbE34DZFRZTtLkuhjtHJNEc"
  private val sheetIds = List("Configuration Service - User St", "Logging Service - User Stories")

  private val reqPattern = "\\[.*?\\]".r
  private val reqColumn = 7
  private val storyColumn = 9
  private val validRowSize = 10

  def createMap(): mutable.HashMap[String, Set[String]] = {
    val reqToStoryMap = mutable.HashMap[String, Set[String]]()

    sheetIds.foreach { sheet =>
      val data = sheets.getAllDataScala(spreadsheetId, sheet)
      data.foreach { row =>
        if (row.size == validRowSize) {
          val reqStrings = row(7).toString.split("\n")
          val reqs = reqStrings.map(s => reqPattern.findFirstIn(s))

          val story = row(9).toString

          if (!story.isEmpty) {
            reqs.foreach { req =>
              req.foreach { r =>
                if (reqToStoryMap.contains(r)) {
                  reqToStoryMap.update(r, reqToStoryMap(r) + story)
                } else {
                  reqToStoryMap.update(r, Set(story))
                }
              }
            }
          }
        }
      }
    }
    reqToStoryMap
  }
}
