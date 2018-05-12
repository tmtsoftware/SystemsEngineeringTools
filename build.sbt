import sbt._
import Settings._

lazy val `TestToStoryMapper` = project
  .settings(defaultSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.`google-api-client`,
      Dependencies.`google-oauth-client`,
      Dependencies.`google-sheets-client`
    )
  )
