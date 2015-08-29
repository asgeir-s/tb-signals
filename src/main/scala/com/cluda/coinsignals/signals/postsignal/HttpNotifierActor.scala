package com.cluda.coinsignals.signals.postsignal

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.stream.ActorMaterializer

import scala.concurrent.{Future, Promise}

/**
 * Created by sogasg on 28/08/15.
 */
class HttpNotifierActor extends Actor with ActorLogging {

  implicit val executor = context.system.dispatcher
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  def doPost(httpRequest: HttpRequest): Future[Boolean] = {
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
  }

  override def receive: Receive = {
    case (httpNotification: HttpNotification, retry: Int) =>
      log.info("HttpNotifierActor: got httpNotification: " + httpNotification)
      doPost(HttpRequest(method = HttpMethods.POST, uri = httpNotification.uri, entity = httpNotification.content)).map { x =>
        if (x) {
          log.info("HttpNotifierActor: signal notification was successful on the " + retry + "th try.")
        }
        else if (retry >= 10) {
          log.error("HttpNotifierActor: signal notification FAILED for the " + retry + "th time. WILL NOT RETRY!!")
        }
        else {
          import scala.concurrent.duration._
          system.scheduler.scheduleOnce(10 seconds, self, (httpNotification, retry+1))
          log.warning("HttpNotifierActor: signal notification FAILED. Will retry in 10 secounds for the " + retry+1 + "th time.")
        }
      }
  }
}

case class HttpNotification(uri: String, content: String)