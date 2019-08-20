package org.tmt.setools

import java.io.PrintWriter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

object Utilities {

  case class Requirement(id: String, fullText: String, verifiedByTestSuite: Boolean)

  case class TestFile(relativePath: String, filename: String)

  case class UserStoryReference(reference: String)

  case class UserStory(reference: UserStoryReference, service: String, asA: String, iWantTo: String, soThat: String) extends Ordered[UserStory] {
    def getText = s"As a/an $asA, I want to $iWantTo so that $soThat"
    private def getNumber = reference.reference.drop(9).toInt
    def compare(that: UserStory): Int = this.getNumber - that.getNumber
  }

  object UserStory {
    val none = UserStory(UserStoryReference("none"), "none", "none", "none", "none")
  }

  case class TestReference(file: TestFile, packageName: String, className: String, testName: String, lineNumber: Int)

  case class TestReportResult(lineNumber: Int, passFail: Boolean)

  case class VAMEntry(jiraStoryID: String, userStoryText: String, requirementId: String, serviceName: String, testName: String, testReportLine: String, testPassed: Option[Boolean]) {
    val testPassOrFail: String = testPassed match {
      case Some(b) => if (b) "PASS" else "FAIL"
      case None => "NONE"
    }
    private val testType = if (testPassed.isDefined) "M" else "I"
    def toString(delim: String) = s"$jiraStoryID$delim$userStoryText$delim$testType${delim}PSR$delim$requirementId$delim$serviceName$delim$testName$delim${endString(delim)}"
    def toStringRep(delim: String) = s"${rep(jiraStoryID)}$delim${rep(userStoryText)}$delim$testType${delim}PSR$delim${rep(requirementId)}$delim${rep(serviceName)}$delim${rep(testName)}$delim${rep(testReportLine)}$delim$testPassOrFail"
    def print(): Unit = println(toStringRep(" | "))
    def write(writer: PrintWriter): Unit = writer.write(s"${toString("\t")}\n")
    private def rep(s: String) = if (s.isEmpty) "none" else s
    private def endString(delim: String) = if (testReportLine.isEmpty) delim else s"$testReportLine$delim$testPassOrFail"
  }

  object VAMEntry {
    def apply(requirementId: String): VAMEntry = {
      VAMEntry("", "", requirementId, "", "", "", testPassed = None )
    }
  }

  def invertMap[A,B](map: Map[A,Iterable[B]]): Map[B, List[A]] = {
    map
      .toList
      .flatMap
        { case (a, b) => b.map(_ -> a) }
      .groupBy(_._1)
      .mapValues(_.map(_._2))
  }

  // Gets the contents of the given uri as a String or throws an exception if it fails.
  def httpGet(user: String, token: String, uri: String)(implicit system: ActorSystem,
                                                        materializer: ActorMaterializer,
                                                        ec: ExecutionContextExecutor): String = {

    val authorization = Authorization(BasicHttpCredentials(user, token))

    val headers  = List(authorization)
    val request  = HttpRequest(HttpMethods.GET, uri, headers = headers)
    val response = Await.result(Http(system).singleRequest(request), 20.seconds)

    if (response.status.isFailure()) {
      println(s"Error getting report from Jenkins: URI = $uri")
//      println(s"Error getting report from Jenkins: URI = $uri:\nrequest: $request\nresponse: $response")
      //      System.exit(1)
      ""
    } else {
      Await.result(response.entity.dataBytes
                     .runWith(Sink.fold(ByteString.empty)(_ ++ _))
                     .map(_.utf8String),
                   5.seconds)
    }
  }

}
