package org.tmt.setools

import java.io.File

import org.tmt.setools.utilities.UserStoryReference

object VAMTool extends App {

  val allRequirements = VCRMParser.getRequirements()

  val testResultsPath = "/Users/weiss/acceptTest/fakeTestResults.csv"
  val testResultParser = new TestResultParser()
  val testToStoryMapper = new TestToStoryMapper("csw", "/Users/weiss/tmtsoftware")

  val reqToStoryMap = VerificationMatrixParser.createMap()

  val storyToTestMap = testToStoryMapper.createStoryToTestMap()
  testToStoryMapper.printSortedStoryToTestStringMap(storyToTestMap)
  val testToResultMap = testResultParser.parseCSV(new File(testResultsPath))
  testResultParser.print(testToResultMap)

  reqToStoryMap.foreach { req =>
    val stories = req._2
    stories.foreach { story =>
      //println("Story:" + story)
      val tests = storyToTestMap.get(UserStoryReference(story))
      if (tests.isDefined) {
        tests.get.foreach { test =>
          //println(s"test: $test")
          val pass = testToResultMap.get(test)
          if (pass.isDefined) {
            val passString = {
              if (pass.get) "PASSED" else "FAILED"
            }

            println(req._1 + "|" + story + "|" + test + "|" + passString)
          }
        }
      }
    }
  }
}
