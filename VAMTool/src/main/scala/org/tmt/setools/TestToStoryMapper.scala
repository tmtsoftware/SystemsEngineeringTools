package org.tmt.setools

import scala.io.Source
import scala.collection.immutable.{ListMap, Set}
import java.io.{File, PrintWriter}

import com.google.api.services.sheets.v4.model.UpdateValuesResponse

import org.tmt.setools.Utilities.{TestFile, TestReference, UserStoryReference}

// rules:
//
// if USR (user story reference) comes before class, USR applies to all tests.
// if USR just above test (no lines between USR and test declaration), only applies to that test
// if USR is within a test, it only applies to that test.

// TODO csw.services.config.server.ConfigServiceTest is an abstract class. does this need special handling?
// TODO $HOME/tmtsoftware/csw/csw-alarm/csw-alarm-client/src/test/scala/csw/alarm/client/internal/services/SeverityServiceModuleTest.scala
// TODO $HOME/tmtsoftware/csw/csw-event/csw-event-client/src/test/scala/csw/event/client/EventSubscriberTest.scala

object TestToStoryMapper {
  private val githubPath    = "https://github.com/tmtsoftware"
  private val delim         = "\t"
  private val header        = s"path${delim}lineNumber${delim}githubLink${delim}className${delim}testName${delim}userStories"
  private val spreadsheetId = "1nykcRxvKTftgEYqAjUUErluaukBaUDZ3ybQQP9z-d7A"
  private val range         = "Sheet1"

  type RefMap = ListMap[TestReference, Set[UserStoryReference]]

  def printMap(testMap: RefMap): Unit = {
    for ((t, s) <- testMap) {
      println(s"${t.file.relativePath}: ${t.lineNumber} (${t.packageName}.${t.className}) - ${t.testName} ->")
      s.foreach(x => println(s"-- ${x.reference}"))
    }
  }

}

class TestToStoryMapper(project: String, rootDir: String) {

  import TestToStoryMapper._

  private def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  private val testFiles = {
    val projectDir  = new File(rootDir, project)
    val projectPath = projectDir.toPath
    recursiveListFiles(projectDir)
      .filter(_.isFile)
      .filter(f => f.getPath.contains("/test/scala") || f.getPath.contains("/test/java") || f.getPath.contains("/multi-jvm"))
      .map(f => TestFile(projectPath.relativize(f.toPath).toString, f.getCanonicalPath))
  }

  def getReference(line: String) = UserStoryReference("DEOPSCSW-" + line.drop(line.indexOf("DEOPSCSW") + 9).takeWhile(testEnd))

  def getGithubLink(t: TestReference) = s"$githubPath/$project/blob/master/${t.file.relativePath}#L${t.lineNumber}"

  private def testEnd(x: Char) = {
    (x != ':') && (x != ' ') && (x != '-')
  }

  def dumpCSV(testMap: RefMap, file: File): Unit = {
    val writer = new PrintWriter(file)
    writer.write(s"$header\n")
    for ((t, s) <- testMap) {
      writer.write(
        s"${t.file.relativePath}$delim${t.lineNumber}$delim${getGithubLink(t)}$delim${t.packageName}$delim${t.className}$delim${t.testName}$delim[${s.map(_.reference).mkString(",")}]\n")
    }
    writer.close()
  }

  def openCSV(testMapIn: RefMap, file: File): RefMap = {
    var testMap = testMapIn
    val source  = Source.fromFile(file)
    for (line <- source.getLines()) {
      if (!line.startsWith(header) && (line.length > 0)) {
        val parts    = line.split(delim)
        val file     = new File(rootDir, parts(0))
        val testFile = TestFile(parts(0), file.getCanonicalPath)
        val testRef = TestReference(
          testFile,
          parts(3),
          parts(4),
          parts(5) // replace double quote with single quote, eliminate single quotes.  this artifact is created by excel, and may be removed.
            .replace("\"\"", "|")
            .replace("\"", "")
            .replace("|", "\""),
          parts(1).toInt
        )

        val userStories = {
          if (parts(5).contains("[]"))
            Set[UserStoryReference]()
          else
            parts(5) // quotes are an artifact from creating file from excel
              .replace("\"", "")
              .replace("[", "")
              .replace("]", "")
              .split(",")
              .map(UserStoryReference)
              .toSet
        }
        testMap = addToMap(testMap, Some(testRef), userStories)
      }
    }
    source.close()
    testMap
  }

  private def getDataFromSheets(testMapIn: RefMap): RefMap = {
    var testMap = testMapIn
    val values  = SheetsAccess.getAllData(spreadsheetId, range)

    if (values == null || values.isEmpty) {
      println("No data found")
    } else {
      println("Class, test, Stories")
      for (row <- values) {
        println(s"${row(3)} | ${row(4)} | ${row(5)}")
        if (row.mkString(delim) != header) {
          val parts    = row.map(_.toString)
          val file     = new File(rootDir, parts.head)
          val testFile = TestFile(parts.head, file.getCanonicalPath)
          val testRef  = TestReference(testFile, parts(2), parts(3), parts(4), parts(1).toInt)

          val userStories = {
            if (parts(5).contains("[]"))
              Set[UserStoryReference]()
            else
              parts(5)
                .replace("[", "")
                .replace("]", "")
                .split(",")
                .map(UserStoryReference)
                .toSet
          }
          testMap = addToMap(testMap, Some(testRef), userStories)
        }
      }
    }
    testMap
  }

  private def addToMap(testMap: RefMap, testRef: Option[TestReference], storyRefs: Set[UserStoryReference]): RefMap = {
    val currentRefs = testMap.getOrElse(testRef.get, Set[UserStoryReference]())
    testMap + (testRef.get -> (storyRefs ++ currentRefs))
  }

  private def addGlobalsToMap(testMap: RefMap, testRef: Option[TestReference], storyRefs: Set[UserStoryReference]): RefMap = {
    testMap + (testRef.get -> storyRefs)
  }

  private[setools] def processScalaFile(testMapIn: RefMap, file: TestFile): RefMap = {
    var testMap                                  = testMapIn
    var globalRefs                               = Set[UserStoryReference]()
    var testRefs                                 = Set[UserStoryReference]()
    var lastTestReference: Option[TestReference] = None

    def getClassName(line: String) = line.drop(6).takeWhile(_ != ' ')

    def getAbstractClassName(line: String) = line.drop(15).takeWhile(_ != ' ')

    def getTestName(line: String) = line.drop(line.indexOf("test(") + 6).takeWhile(_ != '"')

    def getPackageName(line: String) = line.drop(8).takeWhile(_ != ' ')

    val source                      = Source.fromFile(file.filename)
    var className: Option[String]   = None
    var packageName: Option[String] = None
    var lineNumber                  = 0
    for (line <- source.getLines()) {
      lineNumber += 1
      if (line.startsWith("package")) {
        packageName = Some(getPackageName(line))
      } else if (line.startsWith("class")) {
        className = Some(getClassName(line))
      } else if (line.startsWith("abstract class")) {
        className = Some(getAbstractClassName(line))
      } else if (line.contains("DEOPSCSW")) {
        if (className.isEmpty) {
          globalRefs += getReference(line)
        } else {
          testRefs += getReference(line)
        }
      } else if (line.dropWhile(_ == ' ').startsWith("test(\"")) {
        lastTestReference = Some(
          TestReference(file, packageName.getOrElse(""), className.getOrElse("None"), getTestName(line), lineNumber))
        testMap = addGlobalsToMap(testMap, lastTestReference, globalRefs)
        testMap = addToMap(testMap, lastTestReference, testRefs)
        testRefs = Set[UserStoryReference]()
      } else {
        if (lastTestReference.isDefined) {
          testMap = addToMap(testMap, lastTestReference, testRefs)
          testRefs = Set[UserStoryReference]()
        }
      }
    }
    source.close
    testMap
  }

  private[setools] def processJavaFile(testMapIn: RefMap, file: TestFile): RefMap = {
    var testMap                                  = testMapIn
    var globalRefs                               = Set[UserStoryReference]()
    var testRefs                                 = Set[UserStoryReference]()
    var lastTestReference: Option[TestReference] = None
    var testStart                                = false

    def getClassName(line: String) = line.drop(13).takeWhile(_ != ' ')

    def getAbstractClassName(line: String) = line.drop(22).takeWhile(_ != ' ')

    def getTestName(line: String) = line.dropWhile(_ == ' ').drop(12).takeWhile(_ != '(')

    def getPackageName(line: String) = line.drop(8).takeWhile(_ != ';')

    val source                      = Source.fromFile(file.filename)
    var className: Option[String]   = None
    var packageName: Option[String] = None

    var lineNumber = 0
    for (line <- source.getLines()) {
      lineNumber += 1
      if (line.startsWith("package")) {
        packageName = Some(getPackageName(line))
      } else if (line.startsWith("public class")) {
        className = Some(getClassName(line))
      } else if (line.startsWith("public abstract class")) {
        className = Some(getAbstractClassName(line))
      } else if (line.contains("DEOPSCSW")) {
        if (className.isEmpty) {
          globalRefs += getReference(line)
        } else {
          testRefs += getReference(line)
        }
      } else if (line.dropWhile(_ == ' ').startsWith("@Test")) {
        testStart = true
      } else if (testStart) {
        lastTestReference = Some(
          TestReference(file, packageName.getOrElse(""), className.getOrElse("None"), getTestName(line), lineNumber))
        testMap = addGlobalsToMap(testMap, lastTestReference, globalRefs)
        testMap = addToMap(testMap, lastTestReference, testRefs)
        testRefs = Set[UserStoryReference]()
        testStart = false
      } else {
        if (lastTestReference.isDefined) {
          testMap = addToMap(testMap, lastTestReference, testRefs)
          testRefs = Set[UserStoryReference]()
        }
      }
    }
    source.close
    testMap
  }

  def updateMapFromSheets(): RefMap = {
    var testMap: RefMap = ListMap[TestReference, Set[UserStoryReference]]()
    testFiles.filter(_.filename.endsWith(".scala")).foreach(f => testMap = processScalaFile(testMap, f))
    testFiles.filter(_.filename.endsWith(".java")).foreach(f => testMap = processJavaFile(testMap, f))
    testMap
  }

  private def makeSheetsData(testMap: RefMap): List[List[Any]] = {
    testMap.toList.map(
      item =>
        List(item._1.file.relativePath,
             item._1.lineNumber,
             getGithubLink(item._1),
             item._1.className,
             item._1.testName,
             s"[${item._2.map(_.reference).mkString(",")}]"))
  }

  def writeMapToSheets(testMap: RefMap): UpdateValuesResponse = {
    SheetsAccess.clearData(spreadsheetId, range)
    SheetsAccess.writeData(spreadsheetId, range, makeSheetsData(testMap))
  }


  def createStoryToTestMap(): Map[UserStoryReference, List[String]] = {
    val testMap = updateMapFromSheets()
    val tempMap = Utilities.invertMap(testMap)
    tempMap.map { case (a, b) => a -> b.map(ref => ref.packageName + "." + ref.className + "." + ref.testName) }
  }

  def printSortedStoryToTestMap(storyToTestMap: Map[UserStoryReference, List[TestReference]]): Unit = {
    for ((t, s) <- storyToTestMap.toSeq.sortBy(_._1.reference.drop(9).toInt)) {
      println(s"${t.reference}: ->")
      s.foreach(x => println(s"-- ${x.className} - ${x.testName}"))
    }
  }

  def printSortedStoryToTestStringMap(storyToTestMap: Map[UserStoryReference, List[String]]): Unit = {
    for ((t, s) <- storyToTestMap.toSeq.sortBy(_._1.reference.drop(9).toInt)) {
      println(s"${t.reference}: ->")
      s.foreach(x => println(s"-- $x"))
    }
  }

}
