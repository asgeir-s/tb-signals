package com.cluda.coinsignals.signals

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.FlowMaterializer
import akka.util.Timeout
import com.cluda.coinsignals.signals.model.Meta
import com.cluda.coinsignals.signals.postsignal.PostSignalActor
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContextExecutor, Future}

trait Service {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: FlowMaterializer

  implicit val timeout: Timeout


  def config: Config

  val logger: LoggingAdapter

  val getExchangeActor: ActorRef

  val databaseActor: ActorRef

  val getPriceActor: ActorRef

  /**
   * Start a actor and pass it the decodedHttpRequest.
   * Returns a future. If anything fails it returns HttpResponse with "BadRequest",
   * else it returns the HttpResponse returned by the started actor
   *
   * @param props of the actor to start
   * @return Future[HttpResponse]
   */
  def perRequestActor[T](props: Props, message: T): Future[HttpResponse] = {
    (system.actorOf(props) ? message)
      .recover { case _ => HttpResponse(BadRequest, entity = "BadRequest") }
      .asInstanceOf[Future[HttpResponse]]
  }

  val routes = {
    logRequestResult("signals") {
      pathPrefix("streams" / Segment) { streamID =>
        pathPrefix("signals") {
          post {
            entity(as[String]) { signal => ctx =>
              ctx.complete {
                if (List(-1, 0, 1).contains(signal.toInt)) {
                  perRequestActor[Meta](
                    PostSignalActor.props(getExchangeActor),
                    Meta(None, streamID, signal.toInt, None, None, None)
                  )
                }
                else {
                  HttpResponse(BadRequest, entity = "BadRequest")
                }
              }
            }
          }~
            get {
              complete {
                "get all signals for stream with id " + streamID
              }
            }
        } ~
          pathPrefix("status") {
            complete {
              // return the last signal
              "the streams current status with id " + streamID
            }
          }

      }
    }

  }
}
