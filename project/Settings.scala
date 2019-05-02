import sbt.Keys._
import sbt.{Compile, Test, url}

object Settings {

  val buildSettings = Seq(
    organization := "org.tmt",
    organizationName := "TMT",
    organizationHomepage := Some(url("http://www.tmt.org")),
    version := Dependencies.Version,
    scalaVersion := Dependencies.ScalaVersion,
    crossPaths := true,
    parallelExecution in Test := false,
    fork := true
  )

  lazy val defaultSettings = buildSettings ++ Seq(
    // compile options ScalaUnidoc, unidoc
    scalacOptions ++= Seq("-encoding", "UTF-8", "-feature", "-deprecation", "-unchecked"),
    javacOptions in(Compile, compile) ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
  )
}