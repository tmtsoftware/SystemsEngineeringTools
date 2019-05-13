package org.tmt.setools

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

/**
  * Used to download test-reports.txt files for each module from Jenkins.
  */
object JenkinsWorkspace {

  val server        = "ec2-35-154-215-191.ap-south-1.compute.amazonaws.com"
  val port          = 8080
  val targetBuild   = "acceptance-dev-nightly-build"
  val buildRoot     = s"http://$server:$port/job/$targetBuild/lastSuccessfulBuild"
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

  // Returns a single string containing all of the test reports.
  def getTestReports(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContextExecutor): String = {
    modules.map(m => Utilities.httpGet(s"$workspaceRoot/$m/target/test-reports.txt")).mkString("")
  }

}
