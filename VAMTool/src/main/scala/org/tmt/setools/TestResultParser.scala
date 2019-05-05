package org.tmt.setools

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import spray.json._

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._
import scala.io.Source

object TestResultParser {
  val delim = "\t"
  val header = s"class$delim title$delim duration (ms)$delim status"

  val baseGitUri = "https://api.github.com/repos/tmtsoftware/csw-acceptance/contents/results/"
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  def parseCSV(file: File): Map[String, Boolean] = {
    val source = Source.fromFile(file)
    try {
      source
        .getLines
        .filter(_.startsWith("csw."))
        .map(_.split(delim))
        .map(parts => parts(0) + "." + parts(1) -> parts(2).equalsIgnoreCase("PASSED"))
        .toMap
    } finally {
      source.close()
    }
  }

  def print(map: Map[String, Boolean]): Unit = {
    map.foreach { a =>
      println(s"${a._1} --> ${a._2}")
    }
  }

  def getResultMapFromGithub: Map[String, Boolean] = {
    val gitResults = Await.result(getReportFromGithub(lastReportUri), 10.seconds)
    Http().shutdownAllConnectionPools
    println(gitResults)
    // check result code
    if (gitResults.status.isFailure()) {
      throw new RuntimeException(s"Error getting report from Github:")
    }
    val entity = gitResults.entity
    // entity data bytes return as stream.  concat as it comes in
    val entityString = Await.result(entity.dataBytes.runWith(Sink.fold(ByteString.empty)(_ ++ _)).map(_.utf8String), 5.seconds)
    // parse the entity as JSON, extract content field, remove quotes and CRs, and decode
    val resultsAsString = base64ToString(trimQuotes(entityString.parseJson.asJsObject.fields("content").toString()).replace("\\n", ""))

    // first two lines are headers
    val resultsAsList = resultsAsString
      .split("\n")
      .toList
      .drop(2)

    resultsAsList.
      map(_.split("\t")).
      map(ev => ev(0) + "." + ev(1) -> ev(2).equalsIgnoreCase("PASSED"))
      .toMap
  }

  private def lastReportUri = {
    // TODO determine from Git, last file
    val file = "20190117_151834_csw_acceptance_results.tsv"
    baseGitUri + file
  }

  private def trimQuotes(s: String) = {
    s.dropWhile(c => c == '\"').takeWhile(c => c != '\"')
  }

  private def getReportFromGithub(uri: String) = Http(system).singleRequest(HttpRequest(HttpMethods.GET, uri))


  private def base64ToString(message: String) = Base64.getDecoder.decode(message).map(_.toChar).mkString

  private def stringToBase64(message: String) = Base64.getEncoder.encodeToString(message.getBytes(StandardCharsets.UTF_8))
}
