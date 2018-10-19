import sbt._
import sbt.Keys._
import Def.{setting => dep}

object Dependencies {

  val Version = "0.1-SNAPSHOT"
  val ScalaVersion = "2.12.4"


  lazy val `google-api-client` = "com.google.api-client" % "google-api-client" % "1.23.0"
  lazy val `google-oauth-client` = "com.google.oauth-client" % "google-oauth-client-jetty" % "1.23.0"
  lazy val `google-sheets-client` = "com.google.apis" % "google-api-services-sheets" % "v4-rev516-1.23.0"
  val `scalatest` = "org.scalactic" %% "scalactic" % "3.0.5"
  val `scalatest-test` = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  

}