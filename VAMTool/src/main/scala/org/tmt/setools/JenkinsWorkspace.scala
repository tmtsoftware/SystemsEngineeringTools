package org.tmt.setools

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

/**
  * Used to download test-reports.txt files for each module from Jenkins.
  */
object JenkinsWorkspace {

  val server        = "54.212.217.224"
  val port          = 8080
  val targetBuild   = "CSW%20Acceptance%20Pipeline/lastBuild"
  val buildRoot     = s"http://$server:$port/job/$targetBuild"
  val workspaceRoot = s"$buildRoot/execution/node/3/ws"

  // csw modules that produce a test report
  val modules = List(
    "admin-server",
    "location-server",
    "location-agent",
    "config-server",
    "config-api",
    "config-client",
    "config-cli",
    "logging",
    "framework",
    "params",
    "command-client",
    "event-client",
    "event-cli",
    "alarm-api",
    "alarm-client",
    "alarm-cli",
    "database",
    "aas",
    "time"
  )

  val testReportPath = "target/test-reports.txt"

  // Returns a single string containing all of the test reports.
  def getTestReports(user: String, token: String)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContextExecutor): String = {
    modules.map(m => Utilities.httpGet(user, token, s"$workspaceRoot/$m/$testReportPath")).mkString("")
  }

}
