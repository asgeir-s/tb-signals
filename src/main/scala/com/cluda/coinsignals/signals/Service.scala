package com.cluda.coinsignals.signals

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.stream.FlowMaterializer
import com.typesafe.config.Config

import scala.concurrent.ExecutionContextExecutor

trait Service {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: FlowMaterializer

  def config: Config

  val logger: LoggingAdapter

  val routes = {
    logRequestResult("ping") {
      pathPrefix("ping") {
        get {
          complete {
            "pong"

          }
        }
      }
    }
  }
}
