package org.tmt.setools

import scala.io.Source

object TestResultParser {
  val delim  = "\t"
  val header = s"class$delim title$delim duration (ms)$delim status"

  val baseGitUri =
    "https://api.github.com/repos/tmtsoftware/csw-acceptance/contents/results/"

  def parseCSV(csv: String): Map[String, Boolean] = {
    val source = Source.fromString(csv)
    try {
      source.getLines
        .filter(_.startsWith("csw."))
        .map(_.split(delim))
        .map(parts => parts(0) + "." + parts(1) -> parts(2).equalsIgnoreCase("PASSED"))
        .toMap
    } finally {
      source.close()
    }
  }

  def print(map: Map[String, Boolean]): Unit = {
    map.foreach { a =>
      println(s"${a._1} --> ${a._2}")
    }
  }
}
