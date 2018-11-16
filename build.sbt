import sbt._
import Settings._

lazy val `VAMTools` = project
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.`google-api-client`,
      Dependencies.`google-oauth-client`,
      Dependencies.`google-sheets-client`,
      Dependencies.`scalatest`,
      Dependencies.`scalatest-test`
    )
  )
