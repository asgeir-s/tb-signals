package com.cluda.coinsignals.signals.postsignal

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.cluda.coinsignals.signals.util.HttpUtil

import scala.concurrent.{Future, Promise}

/**
 * Created by sogasg on 28/08/15.
 */
class HttpNotifierActor extends Actor with ActorLogging {

  implicit val executor = context.system.dispatcher
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  def doPost(httpNotification: HttpNotification): Future[Boolean] = {

    log.info("HttpNotifierActor: doPost: host: " + httpNotification.host + ", path: " + httpNotification.path + ", body: " + httpNotification.body)

    HttpUtil.request(
      system,
      HttpMethods.POST,
      false,
      httpNotification.host,
      httpNotification.path,
      body = httpNotification.body
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
    case (httpNotification: HttpNotification, retry: Int) =>
      log.info("HttpNotifierActor: got httpNotification: " + httpNotification)
      doPost(httpNotification).map { x =>
        if (x) {
          log.info("HttpNotifierActor: signal notification was SUCCESSFUL on the " + retry + "th try.")
        }
        else if (retry >= 10) {
          log.error("HttpNotifierActor: signal notification FAILED for the " + retry + "th time. WILL NOT RETRY!!")
        }
        else {
          import scala.concurrent.duration._
          system.scheduler.scheduleOnce(10 seconds, self, (httpNotification, retry+1))
          log.warning("HttpNotifierActor: signal notification FAILED. Will retry in 10 secounds for the " + (retry+1) + "th time.")
        }
      }
  }
}

case class HttpNotification(host: String, path: String, body: String)