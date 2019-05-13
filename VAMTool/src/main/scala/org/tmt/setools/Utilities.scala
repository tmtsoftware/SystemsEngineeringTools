package org.tmt.setools

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
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

  def httpGet(
      uri: String)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContextExecutor): String = {
    val response = Await.result(Http(system).singleRequest(HttpRequest(HttpMethods.GET, uri)), 20.seconds)

    if (response.status.isFailure())
      throw new RuntimeException(s"Error getting report from Jenkins: URI = $uri")

    Await.result(response.entity.dataBytes
                   .runWith(Sink.fold(ByteString.empty)(_ ++ _))
                   .map(_.utf8String),
                 5.seconds)
  }

}
