package org.tmt.setools

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.tmt.setools.Utilities.UserStoryReference

import scala.concurrent.ExecutionContextExecutor

object VAMTool extends App {

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
  private val testToStoryMapper = new TestToStoryMapper("csw", s"$HOME/tmtsoftware")

  // list of Requirements
  private val allRequirements = VCRMParser.getRequirements()

  // map of requirement id to set of user story ids
  private val reqToStoryMap = VerificationMatrixParser.createMap()

  private val storyToTestMap = testToStoryMapper.createStoryToTestMap()
  testToStoryMapper.printSortedStoryToTestStringMap(storyToTestMap)
  private val testToResultMap = TestResultParser.parseCSV(testResults)
  TestResultParser.print(testToResultMap)

  reqToStoryMap.foreach { req =>
    val stories = req._2
    stories.foreach { story =>
      val tests = storyToTestMap.get(UserStoryReference(story))
      if (tests.isDefined) {
        tests.get.foreach { test =>
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
