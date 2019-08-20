package org.tmt.setools

import java.io.File

import akka.actor.ActorSystem
import akka.http.javadsl.model.BodyPartEntity
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._


class DocushareAccess {
  private val defaultUser: String = sys.env.getOrElse("DCCUSER", "")
  private val defaultPw: String = sys.env.getOrElse("DCCPW", "")
  private val agent = headers.`User-Agent`(ProductVersion("DsAxess", "4.0"))
  private val acceptLanguage = headers.`Accept-Language`(LanguageRange("en"))
  private val acceptGet = headers.`Accept`(MediaRanges.`*/*`, MediaTypes.`text/xml`)
  private val acceptPublish = headers.`Accept`(MediaRanges.`*/*`, MediaTypes.`text/html`)

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def login(user: String = defaultUser, pw: String = defaultPw): Option[Seq[HttpCookie]] = {
    val loginBody = s"""<?xml version="1.0" ?><authorization><username><![CDATA[$user]]></username><password><![CDATA[$pw]]></password><domain><![CDATA[DocuShare]]></domain></authorization>"""
    val uri = "https://docushare.tmt.org/docushare/dsweb/LOGIN"
    val entity = HttpEntity(ContentTypes.`text/xml(UTF-8)`, ByteString(loginBody))
    val request  = HttpRequest(HttpMethods.POST, uri, headers=List(agent, acceptGet, acceptLanguage), entity=entity)
    val response = Await.result(Http(system).singleRequest(request), 20.seconds)
    response.status match {
      case StatusCodes.OK => Some(response.headers.collect { case headers.`Set-Cookie`(hc) => hc })
      case x =>
        println(s"Error logging in ($x), user=$user, pw=$pw")
        None
    }
  }

  def get(uri: Uri, cookies: Seq[HttpCookie]): String = {
    val request  = HttpRequest(HttpMethods.GET, uri, headers=List(headers.Cookie(cookies.map(_.pair()).toList)))
    val response = Await.result(Http(system).singleRequest(request), 20.seconds)

    if (response.status.isFailure()) {
      println(s"Error getting report from DCC: URI = $uri")
      ""
    } else {
      Await.result(response.entity.dataBytes
        .runWith(Sink.fold(ByteString.empty)(_ ++ _))
        .map(_.utf8String),
        5.seconds)
    }
  }

  def publish(cookies: Seq[HttpCookie], collection: Int, file: File, packageName: String, packageVersion: Float, author: String, documentNumber: String = "none", description: String = ""): StatusCode = {
    val filename = "file1"
    //    val filename = s"TestReport_${packageName}_${packageVersion}_${DateTime.now.toString()}.tsv"
//    val title = s"$packageName v$packageVersion Test Report (${DateTime.now.toString()})"
    val title = "Test Report"
    val collectionString = s"Collection-$collection"
    val uri = s"https://docushare.tmt.org/docushare/dsweb/ApplyUpload/$collectionString"
    val uri2 = "https://docushare.tmt.org/docushare/dsweb/ApplyUpload/Collection-24009"

    val manCookies = List(HttpCookiePair("JSESSIONID", "EBF888E0E4FD7CC9E755D4C0E7244910.tomcat1"), HttpCookiePair("AmberUser", "48.2DFA2D5C1D61076D5CDEC4FBE2013FCF7C18C406ADBEB4D222.-1bbvpbdyivz4ujyqi7mzn"))

    def defaultEntity(content: String) =
      HttpEntity.Default(ContentTypes.`text/plain(UTF-8)`, content.length, Source(ByteString(content) :: Nil))

    val data = Multipart.FormData(
      Multipart.FormData.BodyPart("parent", defaultEntity(collectionString)),
      Multipart.FormData.BodyPart("max_versions", defaultEntity(100.toString)),
      Multipart.FormData.BodyPart("title", defaultEntity(title)),
      Multipart.FormData.BodyPart("summary", defaultEntity(documentNumber)),
      Multipart.FormData.BodyPart("author", defaultEntity(author)),
      Multipart.FormData.BodyPart.fromFile("file1", ContentTypes.`text/plain(UTF-8)`, file)
    )

    val request  = HttpRequest(HttpMethods.POST, uri2, headers=List(headers.Cookie(manCookies), agent, acceptPublish, acceptLanguage), entity=data.toEntity())
    println(request.toString())
    val response = Await.result(Http(system).singleRequest(request), 20.seconds)
    response.status match {
      case StatusCodes.Created => println("File published")
        println(Await.result(response.entity.dataBytes
          .runWith(Sink.fold(ByteString.empty)(_ ++ _))
          .map(_.utf8String),
          5.seconds))
      case x =>
        println(s"Error publishing file: $x")
        println(Await.result(response.entity.dataBytes
          .runWith(Sink.fold(ByteString.empty)(_ ++ _))
          .map(_.utf8String),
          5.seconds))
    }
    response.status

  }
}
