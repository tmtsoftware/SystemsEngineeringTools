package org.tmt.setools

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._


class DocushareAccess {
  private val defaultUser: String = sys.env.getOrElse("DCCUSER", "")
  private val defaultPw: String = sys.env.getOrElse("DCCPW", "")

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def login(user: String = defaultUser, pw: String = defaultPw): Option[Seq[HttpCookie]] = {
    val loginBody = s"""<?xml version="1.0" ?><authorization><username><![CDATA[$user]]></username><password><![CDATA[$pw]]></password><domain><![CDATA[DocuShare]]></domain></authorization>"""
    val uri = "https://docushare.tmt.org/docushare/dsweb/LOGIN"
    val agent = headers.`User-Agent`(ProductVersion("DsAxess", "4.0"))
    val accLang = headers.`Accept-Language`(LanguageRange("en"))
    val acc = headers.`Accept`(MediaRanges.`*/*`, MediaTypes.`text/xml`)
    val entity = HttpEntity(ContentTypes.`text/xml(UTF-8)`, ByteString(loginBody))
    val request  = HttpRequest(HttpMethods.POST, uri, headers=List(agent, acc, accLang), entity=entity)
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
}
