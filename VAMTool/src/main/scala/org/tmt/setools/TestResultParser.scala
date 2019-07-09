package org.tmt.setools

import scala.io.Source
import Utilities.TestReportResult

object TestResultParser {
  val delim  = "\t"
  val header = s"class$delim title$delim duration (ms)$delim status"

  val baseGitUri =
    "https://api.github.com/repos/tmtsoftware/csw-acceptance/contents/results/"

  def parseCSV(csv: String): Map[String, TestReportResult] = {
    val source = Source.fromString(csv)
    try {
      source.getLines.zipWithIndex
        .filter(_._1.startsWith("csw."))
        .map(line  => (line._1.split(delim), line._2))
        .map(parts => parts._1(0) + "." + parts._1(1) -> TestReportResult(parts._2+1, parts._1(2).equalsIgnoreCase("PASSED")))
        .toMap
    } finally {
      source.close()
    }
  }

  def print(map: Map[String, TestReportResult]): Unit = {
    map.foreach { a =>
      println(s"${a._1} (${a._2.lineNumber}) --> ${a._2.passFail}")
    }
  }
}
