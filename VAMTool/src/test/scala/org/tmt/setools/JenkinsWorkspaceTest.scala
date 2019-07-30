package org.tmt.setools

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.ExecutionContextExecutor

class JenkinsWorkspaceTest extends FunSuite with Matchers {
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  private val config = system.settings.config
  if (!config.hasPath("jenkins.user") || !config.hasPath("jenkins.token")) {
    println(s"Please add -Djenkins.user=<user> -Djenkins.token=<token> to the command line.")
  }
  private val jenkinsUser = config.getString("jenkins.user")
  private val jenkinsToken = config.getString("jenkins.token")


  test("should download and concat test reports") {
    println(s"$jenkinsUser - $jenkinsToken")
    val testResults = JenkinsWorkspace.getTestReports(jenkinsUser, jenkinsToken)
    testResults.isEmpty shouldBe false
  }


  test("should download reports and write to file") {
    val file = new File("/tmp/testReportFile")
    val testResults = JenkinsWorkspace.getTestReports(jenkinsUser, jenkinsToken)
    testResults.isEmpty shouldBe false
    JenkinsWorkspace.writeTestReportToFile(file, testResults)
  }
}
