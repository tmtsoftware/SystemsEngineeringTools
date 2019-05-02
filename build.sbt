import sbt._
import Settings._
import Dependencies._

lazy val `VAMTool` = project
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      `google-api-client`,
      `google-oauth-client`,
      `google-sheets-client`,
      `scalatest`,
      `scalatest-test`,
      Akka.`akka-actor`,
      Akka.`akka-stream`,
      AkkaHttp.`akka-http`,
      AkkaHttp.`spray-json`
    )
  )
