package com.cluda.coinsignals.signals.getsignal

import akka.actor._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import com.cluda.coinsignals.signals.model.{Signal, SignalJsonProtocol}
import com.cluda.coinsignals.signals.protocoll.{InalidCombinationOfParametersException, DatabaseReadException, GetSignals}

class GetSignalsActor(databaseReaderActor: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case (globalRequestID: String, request: GetSignals) =>
      log.info(s"[$globalRequestID]: Got GetSignals. Forwarding to databaseReaderActor and becoming responder")
      databaseReaderActor ! (globalRequestID, request)
      context.become(responder(sender()))
  }

  def responder(respondTo: ActorRef): Receive = {
    case (globalRequestID: String, signals: Seq[Signal]) =>
      log.info(s"[$globalRequestID]: Got signal(s) back: " + signals)
      import SignalJsonProtocol._
      import spray.json._
      respondTo ! HttpResponse(OK, entity = signals.map(_.toJson).toJson.prettyPrint)
      self ! PoisonPill

    case (globalRequestID: String, e: DatabaseReadException) =>
      log.error(s"[$globalRequestID]: returns 'no stream with that id'. Reason: " + e.reason)
      respondTo ! HttpResponse(NoContent)

    case (globalRequestID: String, e: InalidCombinationOfParametersException) =>
      log.error(s"[$globalRequestID]: Reason: "  + e.info)
      respondTo ! HttpResponse(BadRequest, entity = "invalid combination of parameters")
  }
}

object GetSignalsActor {
  def props(databaseReaderActor: ActorRef): Props = Props(new GetSignalsActor(databaseReaderActor))
}