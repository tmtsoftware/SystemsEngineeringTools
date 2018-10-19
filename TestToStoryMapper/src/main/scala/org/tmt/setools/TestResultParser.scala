package org.tmt.setools

import java.io.File

import scala.collection.mutable
import scala.io.Source

class TestResultParser {
  val delim=","
  val header = s"class$delim title$delim duration (ms)$delim status"


  def parseCSV(file: File) = {
    val testResultMap = mutable.HashMap[String, Boolean]()
    val source = Source.fromFile(file)
    for (line <- source.getLines()) {
      if (!line.startsWith(header) && (line.length > 0)) {

        val parts = line.split(delim)

        testResultMap.update(parts(0)+"."+parts(1), parts(3).equalsIgnoreCase("PASSED"))
      }
    }
    source.close()

    testResultMap
  }

  def print(map: mutable.HashMap[String, Boolean]) = {
    map.foreach { a =>
      println(s"${a._1} --> ${a._2}")
    }
  }

}
