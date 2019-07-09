package org.tmt.setools

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import Utilities.UserStoryReference
import Utilities.VAMEntry

import scala.collection.immutable.TreeMap
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor

object VAMTool extends App {

  /*
   output should be a table with following columns:

   A: Absolute Number (blank)
   B: JIRA Story ID
   C: VA Name (blank)
   D: User Story text (as a _, i want to _, so that _)
   E: VA Method ("M")
   F: VA Milestone ("PSR")
   G: Requirement IDs (no brackets or text, comma-delimited)
   H: Category (Service Name)
   I: Test name
   J: Link to test report (DCC link) and line number
   K: Test pass/fail
  */

  implicit val system: ActorSystem             = ActorSystem()
  implicit val ec: ExecutionContextExecutor    = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val config = system.settings.config
  if (!config.hasPath("jenkins.user") || !config.hasPath("jenkins.token")) {
    println(s"Please add -Djenkins.user=<user> -Djenkins.token=<token> to the command line.")
    System.exit(1)
  }
  private val jenkinsUser = config.getString("jenkins.user")
  private val jenkinsToken = config.getString("jenkins.token")

  private val HOME      = System.getProperty("user.home")
  private val testResults = JenkinsWorkspace.getTestReports(jenkinsUser, jenkinsToken)

  // list of Requirements (currently, this isn't used)
  private val allRequirements = VCRMParser.getRequirements()

  // map of requirement id to set of user story ids.  Store in TreeMap so it's sorted
  private val storyToReqMap = TreeMap(VerificationMatrixParser.createStoryToReqMap().toArray:_*)

  private val testToStoryMapper = new TestToStoryMapper("csw", s"$HOME/tmtsoftware")
  private val storyToTestMap = testToStoryMapper.createStoryToTestMap()

  testToStoryMapper.printSortedStoryToTestStringMap(storyToTestMap)
  private val testToResultMap = TestResultParser.parseCSV(testResults)
  TestResultParser.print(testToResultMap)

  private var vamEntries = ListBuffer[Utilities.VAMEntry]()

  storyToReqMap.foreach { item =>
    val tests = storyToTestMap.get(item._1.reference)
    if (tests.isDefined) {
      tests.get.foreach { test =>
        val pass = testToResultMap.get(test)
        if (pass.isDefined) {
          vamEntries += VAMEntry(item._1.reference.reference, item._1.getText, item._2.mkString(","), item._1.service, test, pass.get.lineNumber, pass.get.passFail)
        }
      }
    }
  }

  vamEntries.foreach(x => println(s"${x.jiraStoryID} | ${x.userStoryText} | ${x.requirementId} | ${x.serviceName} | ${x.testName} | ${x.testReportLine} | ${x.testPassOrFail}"))

  System.exit(0)
}
