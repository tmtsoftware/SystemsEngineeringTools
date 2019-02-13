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
  val `junit`              = "junit" % "junit" % "4.12" //Eclipse Public License 1.0

  object Akka {
    val Version                    = "2.5.13" //all akka is Apache License 2.0
    val `akka-stream-testkit`      = "com.typesafe.akka" %% "akka-stream-testkit" % Version
    val `akka-actor-testkit-typed` = "com.typesafe.akka" %% "akka-actor-testkit-typed" % Version
    val `akka-multi-node-testkit`  = "com.typesafe.akka" %% "akka-multi-node-testkit" % Version
    val `akka-actor`               = "com.typesafe.akka" %% "akka-actor" % Version
    val `akka-stream`              = "com.typesafe.akka" %% "akka-stream" % Version
  }

  object AkkaHttp {
    val Version             = "10.1.3"
    val `akka-http-testkit` = "com.typesafe.akka" %% "akka-http-testkit" % Version //ApacheV2
    val `akka-http`         = "com.typesafe.akka" %% "akka-http" % Version
    val `spray-json`        = "com.typesafe.akka" %% "akka-http-spray-json" % Version
  }

}