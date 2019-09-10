package org.tmt.setools

import java.io.{File, PrintWriter}

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
  val workspaceRoot = s"$buildRoot/execution/node/5/ws"

  // csw modules that produce a test report
  val modules = List(
    "aas",
    "admin-server",
    "alarm",
    "command",
    "config",
    "database",
    "event",
    "framework",
    "location",
    "logging",
    "network-utils",
    "params",
    "testkit",
    "time"
  )

  val testReportPath = "target/test-reports.txt"

  // Returns a single string containing all of the test reports.
  def getTestReports(user: String, token: String)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContextExecutor): String = {
    modules.map(m => s"$m\n${Utilities.httpGet(user, token, s"$workspaceRoot/$m/$testReportPath")}").mkString("")
  }

  def writeTestReportToFile(file: File, report: String): Unit = {
    val writer = new PrintWriter(file)
    writer.write(report)
    writer.close()
  }
}
