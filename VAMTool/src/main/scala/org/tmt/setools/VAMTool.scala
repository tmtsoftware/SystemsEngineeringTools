package org.tmt.setools

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.tmt.setools.Utilities.VAMEntry

import scala.collection.immutable.TreeMap
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

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val config = system.settings.config
  if (!config.hasPath("jenkins.user") || !config.hasPath("jenkins.token")) {
    println(s"Please add -Djenkins.user=<user> -Djenkins.token=<token> to the command line.")
    System.exit(1)
  }
  private val jenkinsUser = config.getString("jenkins.user")
  private val jenkinsToken = config.getString("jenkins.token")

  private val HOME = System.getProperty("user.home")
  private val reportFile = new File("/tmp/testReport.tsv")

  private val testResults = JenkinsWorkspace.getTestReports(jenkinsUser, jenkinsToken)
  JenkinsWorkspace.writeTestReportToFile(reportFile, testResults)

  // list of Requirements (currently, this isn't used)
  //private val allRequirements = VCRMParser.getRequirements()

  // map of requirement id to set of user story ids.  Store in TreeMap so it's sorted
  private val storyToReqMap = TreeMap(VerificationMatrixParser.createStoryToReqMap().toArray: _*)

  private val testToStoryMapper = new TestToStoryMapper("csw", s"$HOME/tmtsoftware")
  private val storyToTestMap = testToStoryMapper.createStoryToTestMap()

  testToStoryMapper.printSortedStoryToTestStringMap(storyToTestMap)
  private val testToResultMap = TestResultParser.parseCSV(testResults)
  TestResultParser.print(testToResultMap)

  val vamEntries = storyToReqMap.flatMap {
    case (story, req) =>
      storyToTestMap
        .get(story.reference)
        .map(tests =>
          tests.flatMap(test =>
            testToResultMap
              .get(test)
              .map(result =>
                VAMEntry(story.reference.reference, story.getText, req.mkString(","), story.service, test, result.lineNumber, result.passFail))
          )
        )
  }.flatten
    .toList

  vamEntries.foreach(x => println(s"${x.jiraStoryID} | ${x.userStoryText} | ${x.requirementId} | ${x.serviceName} | ${x.testName} | ${x.testReportLine} | ${x.testPassOrFail}"))

  System.exit(0)



}
