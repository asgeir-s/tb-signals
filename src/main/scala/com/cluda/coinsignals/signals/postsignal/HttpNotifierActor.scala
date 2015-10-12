package com.cluda.coinsignals.signals.postsignal

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import com.cluda.coinsignals.signals.util.HttpUtil

import scala.concurrent.{Future, Promise}
import spray.json._
import DefaultJsonProtocol._

/**
 * Created by sogasg on 28/08/15.
 */
class HttpNotifierActor extends Actor with ActorLogging {

  implicit val ec = context.dispatcher
  implicit val sy = context.system
  implicit val am = ActorMaterializer()

  def doPost(globalRequestID: String, httpNotification: HttpNotification): Future[Boolean] = {
    log.info(s"[$globalRequestID]: doPost: " + httpNotification)

    HttpUtil.request(
      HttpMethods.POST,
      false,
      httpNotification.host,
      httpNotification.path,
      body = httpNotification.body,
      headers = List(RawHeader("Global-Request-ID", globalRequestID))
    ).map {responds =>
      if(responds.status == StatusCodes.Accepted) {
        true
      }
      else {
        false
      }
    }
    /*
    val promise = Promise[Boolean]()
    Http().singleRequest(httpRequest).map { x =>
      if (x.status == StatusCodes.Accepted) {
        promise.success(true)
      }
      else {
        promise.success(false)
      }
    }
    promise.future
    */
  }

  override def receive: Receive = {
    case (globalRequestID: String, httpNotification: HttpNotification, retry: Int) =>
      log.info(s"[$globalRequestID]: got httpNotification: " + httpNotification)
      doPost(globalRequestID, httpNotification).map { x =>
        if (x) {
          log.info(s"[$globalRequestID]: signal notification was SUCCESSFUL on the " + retry + "th try.")
        }
        else if (retry <= 0) {
          log.error(s"[$globalRequestID]: signal notification FAILED. Noe more retrys. WILL NOT RETRY!!")
        }
        else {
          import scala.concurrent.duration._
          sy.scheduler.scheduleOnce(10 seconds, self, (globalRequestID, httpNotification, retry-1))
          log.warning(s"[$globalRequestID]: signal notification FAILED. Will retry in 10 secounds. " + (retry-1) + " retrys left.")
        }
      }
  }
}

case class HttpNotification(host: String, path: String, body: String) {
  override def toString = s"""{ "host": "$host", "path": "$path" + "body": """ + body.parseJson.compactPrint
}