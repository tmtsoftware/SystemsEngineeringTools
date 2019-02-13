import sbt._
import Settings._

lazy val `VAMTool` = project
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.`google-api-client`,
      Dependencies.`google-oauth-client`,
      Dependencies.`google-sheets-client`,
      Dependencies.`scalatest`,
      Dependencies.`scalatest-test`,
      Dependencies.Akka.`akka-actor`,
      Dependencies.Akka.`akka-stream`,
      Dependencies.AkkaHttp.`akka-http`,
      Dependencies.AkkaHttp.`spray-json`,
      Dependencies.`junit`
    )
  )
