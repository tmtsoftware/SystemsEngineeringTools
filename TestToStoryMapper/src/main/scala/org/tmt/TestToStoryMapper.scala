package org.tmt

import scala.io.Source
import scala.collection.immutable.{ListMap, Set}
import java.io.File
// rules:
//
// if USR (user story reference) comes before class, USR applies to all tests.
// if USR just above test (no lines between USR and test declaration), only applies to that test
// if USR is within a test, it only applies to that test.

case class UserStoryReference(reference: String)
case class TestReference(filename: String, className: String, testName: String)

object TestToStoryMapper extends App {
  var testMap = ListMap[TestReference, Set[UserStoryReference]]()

  def getReference(line:String) = UserStoryReference(line.drop(line.indexOf("DEOPSCSW")).takeWhile(testEnd))

  def printMap(): Unit = {
    for ((t,s) <- testMap) {
      println(s"${t.filename}: ${t.className} - ${t.testName} ->")
      s.foreach(x => println(s"-- ${x.reference}"))
    }
  }

  def testEnd(x:Char) = {
    (x != ':') && (x != ' ')
  }

  def addToMap(testRef: Option[TestReference], storyRefs: Set[UserStoryReference]): Unit = {
    val currentRefs = testMap.getOrElse(testRef.get, Set[UserStoryReference]())
    testMap += (testRef.get -> (storyRefs ++ currentRefs))
  }
  def addGlobalsToMap(testRef: Option[TestReference], storyRefs: Set[UserStoryReference]): Unit = {
    testMap += (testRef.get -> storyRefs)
  }
  def processScalaFile(filename:String): Unit = {
    var globalRefs = Set[UserStoryReference]()
    var testRefs = Set[UserStoryReference]()
    var lastTestReference: Option[TestReference] = None

    def getClassName(line:String) = line.drop(6).takeWhile(_ != ' ')
    def getTestName(line:String) = line.drop(line.indexOf("test(")).takeWhile(_ != ')')+')'

    val source = Source.fromFile(filename)
    var className: Option[String] = None
    for (line <- source.getLines()) {
      if (line.startsWith("class")) {
        className = Some(getClassName(line))
      } else if (line.contains("DEOPSCSW")) {
        if (className.isEmpty) {
          globalRefs += getReference(line)
        } else {
          testRefs += getReference(line)
        }
      } else if (line.dropWhile(_ == ' ').startsWith("test(\"")) {
        lastTestReference = Some(TestReference(filename, className.getOrElse("None"), getTestName(line)))
        addGlobalsToMap(lastTestReference, globalRefs)
        addToMap(lastTestReference, testRefs)
        testRefs = Set[UserStoryReference]()
      } else {
        if (lastTestReference.isDefined) {
          addToMap(lastTestReference, testRefs)
          testRefs = Set[UserStoryReference]()
        }
      }
    }
    source.close
  }

  def processJavaFile(filename:String): Unit = {
    var globalRefs = Set[UserStoryReference]()
    var testRefs = Set[UserStoryReference]()
    var lastTestReference: Option[TestReference] = None
    var testStart = false

    def getClassName(line:String) = line.drop(13).takeWhile(_ != ' ')
    def getTestName(line:String) = line.dropWhile(_ == ' ').drop(12).takeWhile(_ != '(')

    val source = Source.fromFile(filename)
    var className: Option[String] = None
    for (line <- source.getLines()) {
      if (line.startsWith("public class")) {
        className = Some(getClassName(line))
      } else if (line.contains("DEOPSCSW")) {
        if (className.isEmpty) {
          globalRefs += getReference(line)
        } else {
          testRefs += getReference(line)
        }
      } else if (line.dropWhile(_ == ' ').startsWith("@Test")) {
        testStart = true
      } else if (testStart) {
        lastTestReference = Some(TestReference(filename, className.getOrElse("None"), getTestName(line)))
        addGlobalsToMap(lastTestReference, globalRefs)
        addToMap(lastTestReference, testRefs)
        testRefs = Set[UserStoryReference]()
        testStart = false
      } else {
        if (lastTestReference.isDefined) {
          addToMap(lastTestReference, testRefs)
          testRefs = Set[UserStoryReference]()
        }
      }
    }
    source.close
  }
  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  def testFilenames(rootDir: String): Array[String] = {
    recursiveListFiles(new File(rootDir))
      .filter(_.isFile)
      .map(x => x.getAbsolutePath)
      .filter(_.contains("/test/"))
      .filter(!_.contains("/resources/"))
  }

  val testFiles = testFilenames("/Users/weiss/tmtsoftware/csw-prod/")


  testFiles.filter(_.endsWith(".scala")).foreach(processScalaFile)
  testFiles.filter(_.endsWith(".java")).foreach(processJavaFile)

  printMap()

}
