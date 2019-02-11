package org.tmt.setools

import scala.io.Source
import scala.collection.immutable.{ListMap, Set}
import java.io.{File, PrintWriter}

import scala.collection.JavaConverters._
import org.tmt.setools.utilities.{TestFile, TestReference, UserStoryReference}

// rules:
//
// if USR (user story reference) comes before class, USR applies to all tests.
// if USR just above test (no lines between USR and test declaration), only applies to that test
// if USR is within a test, it only applies to that test.


// TODO add URL to github for test (at line number if possible)
// TODO csw.services.config.server.ConfigServerTest is an abstract class. does this need special handling?
// TODO /Users/weiss/tmtsoftware/csw/csw-alarm/csw-alarm-client/src/test/scala/csw/alarm/client/internal/services/SeverityServiceModuleTest.scala

class TestToStoryMapper(project: String, rootDir: String)  {
  val githubPath = "https://github.com/tmtsoftware"
  val delim="\t"
  val header=s"path${delim}lineNumber${delim}githubLink${delim}className${delim}testName${delim}userStories"
  val sheets = new SheetsAccess()
  val spreadsheetId = "1nykcRxvKTftgEYqAjUUErluaukBaUDZ3ybQQP9z-d7A"
  val range = "Sheet1"


  private def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  private val testFiles = {
    recursiveListFiles(new File(rootDir+File.separator+project))
      .filter(_.isFile)
      .map(x => TestFile(x.toString.drop(rootDir.length+File.separator.length+project.length+File.separator.length), x.getCanonicalPath))
      .filter(f => f.filename.contains("/test/scala") || f.filename.contains("/test/scala") || f.filename.contains("/multi-jvm"))
  }

  def getReference(line:String) = UserStoryReference("DEOPSCSW-"+line.drop(line.indexOf("DEOPSCSW")+9).takeWhile(testEnd))
  def getGithubLink(t: TestReference) = s"$githubPath/$project/blob/master/${t.file.relativePath}#L${t.lineNumber}"

  private def testEnd(x:Char) = {
    (x != ':') && (x != ' ') && (x != '-')
  }

  var testMap = ListMap[TestReference, Set[UserStoryReference]]()

  def printMap(): Unit = {
    for ((t,s) <- testMap) {
      println(s"${t.file.relativePath}: ${t.lineNumber} (${t.packageName}.${t.className}) - ${t.testName} ->")
      s.foreach(x => println(s"-- ${x.reference}"))
    }
  }

  def dumpCSV(file: File) = {
    val writer = new PrintWriter(file)
    writer.write(s"$header\n")
    for ((t,s) <- testMap) {
      writer.write(s"${t.file.relativePath}$delim${t.lineNumber}$delim${getGithubLink(t)}$delim${t.packageName}$delim${t.className}$delim${t.testName}$delim[${s.map(_.reference).mkString(",")}]\n")
    }
    writer.close()
  }

  def openCSV(file: File) = {
    val source = Source.fromFile(file)
    for (line <- source.getLines()) {
      if (!line.startsWith(header) && (line.length > 0)) {
        val parts = line.split(delim)
        val file = new File(s"$rootDir${File.separator}${parts(0)}")
        val testFile = TestFile(parts(0), file.getCanonicalPath)
        val testRef = TestReference(testFile,
          parts(3),
          parts(4),
          parts(5)   // replace double quote with single quote, eliminate single quotes.  this artifact is created by excel, and may be removed.
            .replace("\"\"", "|")
            .replace("\"","")
            .replace("|", "\""),
          parts(1).toInt)


        val userStories = {
          if (parts(5).contains("[]"))
            Set[UserStoryReference]()
          else
            parts(5)   // quotes are an artifact from creating file from excel
            .replace("\"", "")
            .replace("[", "")
            .replace("]","")
            .split(",")
            .map(UserStoryReference)
            .toSet
        }
        addToMap(Some(testRef), userStories)
      }
    }
    source.close()
  }


  def getDataFromSheets() = {
    val values = sheets.getAllData(spreadsheetId, range)

    if (values == null || values.isEmpty) {
      println("No data found")
    } else {
      println("Class, test, Stories")
      for (javarow <- values.asScala) {
        val row = javarow.asScala
        println(s"${row(3)} | ${row(4)} | ${row(5)}")

        if (row.mkString(delim) != header) {
          val parts = row.map(_.toString)
          val file = new File(s"$rootDir${File.separator}${parts.head}")
          val testFile = TestFile(parts.head, file.getCanonicalPath)
          val testRef = TestReference(testFile,
            parts(2),
            parts(3),
            parts(4),
            parts(1).toInt)

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
          addToMap(Some(testRef), userStories)
        }
      }
    }
  }


  private def addToMap(testRef: Option[TestReference], storyRefs: Set[UserStoryReference]): Unit = {
    val currentRefs = testMap.getOrElse(testRef.get, Set[UserStoryReference]())
    testMap += (testRef.get -> (storyRefs ++ currentRefs))
  }
  private def addGlobalsToMap(testRef: Option[TestReference], storyRefs: Set[UserStoryReference]): Unit = {
    testMap += (testRef.get -> storyRefs)
  }

  private[setools] def processScalaFile(file: TestFile): Unit = {
    var globalRefs = Set[UserStoryReference]()
    var testRefs = Set[UserStoryReference]()
    var lastTestReference: Option[TestReference] = None

    def getClassName(line:String) = line.drop(6).takeWhile(_ != ' ')
    def getAbstractClassName(line:String) = line.drop(15).takeWhile(_ != ' ')
    def getTestName(line:String) = line.drop(line.indexOf("test(")+6).takeWhile(_ != '"')
    def getPackageName(line: String) = line.drop(8).takeWhile(_ != ' ')

    val source = Source.fromFile(file.filename)
    var className: Option[String] = None
    var packageName: Option[String] = None
    var lineNumber = 0
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
        lastTestReference = Some(TestReference(file, packageName.getOrElse(""), className.getOrElse("None"), getTestName(line), lineNumber))
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

  private[setools] def processJavaFile(file: TestFile): Unit = {
    var globalRefs = Set[UserStoryReference]()
    var testRefs = Set[UserStoryReference]()
    var lastTestReference: Option[TestReference] = None
    var testStart = false

    def getClassName(line:String) = line.drop(13).takeWhile(_ != ' ')
    def getAbstractClassName(line:String) = line.drop(22).takeWhile(_ != ' ')
    def getTestName(line:String) = line.dropWhile(_ == ' ').drop(12).takeWhile(_ != '(')
    def getPackageName(line: String) = line.drop(8).takeWhile(_ != ';')

    val source = Source.fromFile(file.filename)
    var className: Option[String] = None
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
        lastTestReference = Some(TestReference(file, packageName.getOrElse(""), className.getOrElse("None"), getTestName(line), lineNumber))
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

  def updateMapFromSheets() = {
    //getDataFromSheets()
    //  openCSV(new File("/Users/Weiss/acceptTest/in.txt"))
    testFiles.filter(_.filename.endsWith(".scala")).foreach(processScalaFile)
    testFiles.filter(_.filename.endsWith(".java")).foreach(processJavaFile)
  }

  private def makeSheetsData() = {
    testMap.toList.map(item => List(item._1.file.relativePath, item._1.lineNumber, getGithubLink(item._1), item._1.className, item._1.testName, s"[${item._2.map(_.reference).mkString(",")}]"))
  }

  def writeMapToSheets() = {
    sheets.clearData(spreadsheetId, range)
    sheets.writeData(spreadsheetId, range, makeSheetsData())
  }
//  dumpCSV( new File("/Users/Weiss/acceptTest/test.csv"))

  def createStoryToTestMap() = {
    updateMapFromSheets()
    val tempMap = testMap.toList.flatMap { case (a, b) => b.map(_ -> a) }.groupBy(_._1).mapValues(_.map(_._2))
    tempMap.map { case (a,b) => a -> b.map(ref => ref.packageName+"."+ref.className+"."+ref.testName) }
  }
  def printSortedStoryToTestMap(storyToTestMap: Map[UserStoryReference, List[TestReference]]) = {
    for ((t, s) <- storyToTestMap.toSeq.sortBy(_._1.reference.drop(9).toInt)) {
      println(s"${t.reference}: ->")
      s.foreach(x => println(s"-- ${x.className} - ${x.testName}"))
    }
  }
  def printSortedStoryToTestStringMap(storyToTestMap: Map[UserStoryReference, List[String]]) = {
    for ((t, s) <- storyToTestMap.toSeq.sortBy(_._1.reference.drop(9).toInt)) {
      println(s"${t.reference}: ->")
      s.foreach(x => println(s"-- ${x}"))
    }
  }

}
