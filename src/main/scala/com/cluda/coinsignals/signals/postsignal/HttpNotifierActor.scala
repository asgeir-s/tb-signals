package com.cluda.coinsignals.signals.postsignal

import akka.actor.{ActorLogging, Actor}

/**
 * Created by sogasg on 28/08/15.
 */
class HttpNotifierActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case httpNotification: HttpNotification =>
      println("notify: " + httpNotification)
  }
}

case class HttpNotification(uri: String, content: String)