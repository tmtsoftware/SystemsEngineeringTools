package org.tmt.setools

import java.io.{File, PrintWriter}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.tmt.setools.Utilities.{UserStoryReference, VAMEntry}

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

  private val vamHeader = "JIRA Story ID\tUser Story\tVA Method\tVA Milestone\tRequirement IDs\tCategory\tTest name\tLink to Test report link and line number\tTest pass/fail\n"


  private val extraTestLinkageFile = new File(s"$HOME/Desktop/CSW Verification/CSWMissingTestToStoryLinkage.txt")
  private val testResults = JenkinsWorkspace.getTestReports(jenkinsUser, jenkinsToken)
  private val testResultsLink = "https://docushare.tmt.org/docushare/dsweb/Get/Document-79767/TestReport_CSW_v1.0.0-RC4_20190828_final.txt"

  // This is commented because the file was manually created using JenkinsWorkspaceTest "should download reports and write to file"
  // and then manually uploaded to docushare.  This can be done automatically if the DCC uploading is figured out.
//  private val reportFile = new File("/tmp/testReport.tsv")
//  JenkinsWorkspace.writeTestReportToFile(reportFile, testResults)

  // list of Requirements (currently, this isn't used)
  //private val allRequirements = VCRMParser.getRequirements()

  // map of requirement id to set of user story ids.  Store in TreeMap so it's sorted
  private val storyToReqMap = TreeMap(VerificationMatrixParser.createStoryToReqMap().toArray: _*)

  private val testToStoryMapper = new TestToStoryMapper("csw", s"$HOME/tmtsoftware")
  private val storyToTestMap = testToStoryMapper.createStoryToTestMap(Some(extraTestLinkageFile))

  testToStoryMapper.printSortedStoryToTestStringMap(storyToTestMap)
  private val testToResultMap = TestResultParser.parseCSV(testResults)
  TestResultParser.print(testToResultMap)

  def reqSetToString(reqs: Set[String]) = if (reqs.isEmpty) "Architectural Design Choice" else reqs.mkString(", ")

  // create entries with all user stories that have tests
  val vamEntries = storyToReqMap.flatMap {
    case (story, req) =>
      storyToTestMap
        .getOrElse(story.reference, List("none"))
        .map(test =>
          testToResultMap
            .get(test) match {
            case Some(result) =>
              VAMEntry(story.reference.reference, story.getText, reqSetToString(req), story.service, test, s"$testResultsLink ${result.lineNumber}", Some(result.passFail))
            case None =>
              VAMEntry(story.reference.reference, story.getText, reqSetToString(req), story.service, test, "", None)
          }
        )
  }.toList

  // add entries for requirements not covered by testing, with empty data, to be filled in by hand later
  val allReqs = DRDParser.getRequirements()
  val reqToStoryMap = VerificationMatrixParser.createReqToStoryMap(allReqs)
  val sortedKeys = reqToStoryMap.keys.toList.sortBy(_.id)
  val extraVamEntries =
    for {
      req <- sortedKeys
      if !vamEntries.exists(entry => entry.requirementId.contains(req.id))
    } yield {
      VAMEntry(req.id)
    }

  val allEntries = vamEntries ++ extraVamEntries

  allEntries.foreach(_.print())

  val vamFile = new File("/tmp/vam.txt")
  val writer = new PrintWriter(vamFile)
  writer.write(vamHeader)
  allEntries.foreach(_.write(writer))
  writer.close()

  System.exit(0)



}
