package org.tmt.setools

import java.io.File

import org.tmt.setools.Utilities.UserStoryReference

object VAMTool extends App {

  // TODO: Make these paths configurable
  val HOME = System.getProperty("user.home")
  val testResultsPath = s"$HOME/acceptTest/20190123_testReport.tsv"
  val testToStoryMapper = new TestToStoryMapper("csw", s"$HOME/tmtsoftware")

  // list of Requirements
  val allRequirements = VCRMParser.getRequirements()

  // map of requirement id to set of user story ids
  val reqToStoryMap = VerificationMatrixParser.createMap()

  val storyToTestMap = testToStoryMapper.createStoryToTestMap()
  testToStoryMapper.printSortedStoryToTestStringMap(storyToTestMap)
  val testToResultMap = TestResultParser.parseCSV(new File(testResultsPath))
  TestResultParser.print(testToResultMap)

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
  System.exit(0)
}
