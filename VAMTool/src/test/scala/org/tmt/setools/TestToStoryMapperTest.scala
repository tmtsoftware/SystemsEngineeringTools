package org.tmt.setools

import java.io.File

import org.scalatest.{FunSuite, Matchers}
import org.tmt.setools.Utilities.{TestFile, TestReference, UserStoryReference}

import scala.collection.immutable.ListMap

class TestToStoryMapperTest extends FunSuite with Matchers {
  val HOME: String = System.getProperty("user.home")

  test("should parse scala file") {
    val mapper = new TestToStoryMapper("", ".")
    val f      = new File(".")
    println(f.getAbsolutePath)
    val testMapIn = ListMap[TestReference, Set[UserStoryReference]]()
    val testMap =
      mapper.processScalaFile(testMapIn,
                              TestFile("./src/test/scala/org/tmt/setools", "src/test/scala/org/tmt/setools/TestFile01.scala"))
    TestToStoryMapper.printMap(testMap)
  }

  test("should print tests") {
    val mapper  = new TestToStoryMapper("csw", s"$HOME/tmtsoftware")
    val testMap = mapper.updateMapFromSheets()
    testMap.foreach { kv =>
      val t = kv._1
      println(t.packageName + "." + t.className + "," + t.testName + ",0.000,PASSED")
    }
  }
}
