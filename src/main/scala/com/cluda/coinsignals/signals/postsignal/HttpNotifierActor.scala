package com.cluda.coinsignals.signals.postsignal

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.stream.ActorMaterializer

import scala.concurrent.{Promise, Future}

/**
 * Created by sogasg on 28/08/15.
 */
class HttpNotifierActor extends Actor with ActorLogging {

  implicit val executor = context.system.dispatcher
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  def doPost(httpRequest: HttpRequest, retryNum: Int): Future[Boolean] = {
    val promise = Promise[Boolean]()

    Http().singleRequest(httpRequest).map { x =>
      if (x.status == StatusCodes.Accepted && x.entity == httpRequest.entity) {
        promise.success(true)
      }
      else if (retryNum == 10) {
        promise.success(false)
      }
      else {
        promise.completeWith(doPost(httpRequest, retryNum + 1))
      }
    }.recoverWith { case _ => doPost(httpRequest, retryNum + 1) }
    promise.future
  }

  override def receive: Receive = {
    case httpNotification: HttpNotification =>
      println("notify: " + httpNotification)
      doPost(HttpRequest(method = HttpMethods.POST, uri = httpNotification.uri, entity = httpNotification.content), 0)
  }
}

case class HttpNotification(uri: String, content: String)