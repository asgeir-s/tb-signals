package com.cluda.tradersbit.signals.postsignal

import akka.actor._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import scala.concurrent.duration._
import com.cluda.tradersbit.signals.model.SignalJsonProtocol
import SignalJsonProtocol._
import com.cluda.tradersbit.signals.model.{Signal, Meta}
import com.cluda.tradersbit.signals.protocoll.SignalProcessingException
import com.cluda.tradersbit.signals.util.MetaUtil
import spray.json._


class PostSignalActor(globalRequestID: String, getExchangeActor: ActorRef) extends Actor with ActorLogging {
  log.info("PostSignalActor started on address " + self)

  context.setReceiveTimeout(10.seconds)

  override def receive: Receive = {
    case meta: Meta =>
      log.info(s"[$globalRequestID]: Got 'Meta' object -> sends it to getExchangeActor and waits for responds")
      getExchangeActor ! (globalRequestID, MetaUtil.setRespondsActor(meta, self))
      context.become(responder(sender()))
  }

  def responder(respondTo: ActorRef): Receive = {

    case signals: Seq[Signal] =>
      log.info(s"[$globalRequestID]: Got signal(s) back: " + signals)
      respondTo ! HttpResponse(OK, entity = signals.map(_.toJson).toJson.prettyPrint)
      self ! PoisonPill

    case e: SignalProcessingException if e.reason.contains("Conflict") =>
      log.error(s"[$globalRequestID]: Got SignalProcessingException: " + e.reason)
      respondTo ! HttpResponse(Conflict, entity = "duplicate")
      self ! PoisonPill

    case e: SignalProcessingException =>
      log.error(s"[$globalRequestID]: Got SignalProcessingException: " + e.reason)
      if(e.reason.contains("cold not get the exchange")) {
        respondTo ! HttpResponse(NotFound, entity = "no stream with that ID")
      }
      else {
        respondTo ! HttpResponse(InternalServerError, entity = "error")
      }
      self ! PoisonPill

    case ReceiveTimeout =>
      log.info(s"[$globalRequestID]: Received Timeout. Got no message back for 10 seconds.")
      respondTo ! HttpResponse(InternalServerError, entity = "The signal was not handled for 10 seconds. Is the exchange's API down?")
      self ! PoisonPill
  }
}

object PostSignalActor {
  def props(globalRequestID: String, getExchangeActor: ActorRef): Props = Props(new PostSignalActor(globalRequestID, getExchangeActor))
}