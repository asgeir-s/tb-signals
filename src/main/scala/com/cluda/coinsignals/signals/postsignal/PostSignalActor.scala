package com.cluda.coinsignals.signals.postsignal

import akka.actor._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import com.cluda.coinsignals.signals.model.{Meta, Signal, SignalJsonProtocol}
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException
import com.cluda.coinsignals.signals.util.MetaUtil
import SignalJsonProtocol._
import spray.json._


class PostSignalActor(getExchangeActor: ActorRef) extends Actor with ActorLogging {
  log.info("PostSignalActor started on address " + self)
  override def receive: Receive = {
    case (globalRequestID: String, meta: Meta) =>
      log.info(s"[$globalRequestID]: Got 'Meta' object -> sends it to getExchangeActor and waits for responds")
      getExchangeActor ! (globalRequestID, MetaUtil.setRespondsActor(meta, self))
      context.become(responder(sender()))
  }

  def responder(respondTo: ActorRef): Receive = {

    case (globalRequestID: String, signals: Seq[Signal]) =>
      log.info(s"[$globalRequestID]: Got signal(s) back: " + signals)
      respondTo ! HttpResponse(OK, entity = signals.map(_.toJson).toJson.prettyPrint)
      self ! PoisonPill

    case (globalRequestID: String, e: SignalProcessingException) if e.reason.contains("Conflict") =>
      log.error(s"[$globalRequestID]: Got SignalProcessingException: " + e.reason)
      respondTo ! HttpResponse(Conflict, entity = "duplicate")
      self ! PoisonPill

    case (globalRequestID: String, e: SignalProcessingException) =>
      log.error(s"[$globalRequestID]: Got SignalProcessingException: " + e.reason)
      if(e.reason.contains("cold not get the exchange")) {
        respondTo ! HttpResponse(NotFound, entity = "no stream with that ID")
      }
      else {
        respondTo ! HttpResponse(InternalServerError, entity = "error")
      }
      self ! PoisonPill
  }
}

object PostSignalActor {
  def props(getExchangeActor: ActorRef): Props = Props(new PostSignalActor(getExchangeActor))
}