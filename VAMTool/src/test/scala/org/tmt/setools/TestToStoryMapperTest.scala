package org.tmt.setools

import java.io.File

import org.scalatest.{FunSuite, Matchers}
import org.tmt.setools.utilities.TestFile

class TestToStoryMapperTest extends FunSuite with Matchers {

  test("should parse scala file") {
    val mapper = new TestToStoryMapper("",".")
    val f = new File(".")
    println(f.getAbsolutePath)
    mapper.processScalaFile(TestFile("./src/test/scala/org/tmt/setools", "src/test/scala/org/tmt/setools/TestFile01.scala"))
    mapper.printMap()
  }

  test("should print tests") {
    val mapper = new TestToStoryMapper("csw","/Users/weiss/tmtsoftware")
    mapper.updateMapFromSheets()
    mapper.testMap.foreach {kv =>
      val t = kv._1
      println(t.packageName+"."+t.className+","+t.testName+",0.000,PASSED")
    }
  }
}
