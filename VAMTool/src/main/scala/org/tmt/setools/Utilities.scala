package org.tmt.setools

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

  case class TestReference(file: TestFile, packageName: String, className: String, testName: String, lineNumber: Int)

  // Gets the contents of the given uri as a String or throws an exception if it fails.
  def httpGet(user: String, token: String, uri: String)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContextExecutor): String = {

    val authorization = Authorization(BasicHttpCredentials(user, token))

    val headers = List(authorization)
    val request = HttpRequest(HttpMethods.GET, uri, headers = headers)
    val response = Await.result(Http(system).singleRequest(request), 20.seconds)

    if (response.status.isFailure()) {
      println(s"Error getting report from Jenkins: URI = $uri:\nrequest: $request\nresponse: $response")
      System.exit(1)
    }

    Await.result(response.entity.dataBytes
      .runWith(Sink.fold(ByteString.empty)(_ ++ _))
      .map(_.utf8String),
      5.seconds)
  }

}
