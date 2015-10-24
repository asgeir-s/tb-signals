package com.cluda.tradersbit.signals.getsignal

import akka.actor._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import com.cluda.tradersbit.signals.model.SignalJsonProtocol
import com.cluda.tradersbit.signals.protocoll.GetSignals
import com.cluda.tradersbit.signals.model.{SignalJsonProtocol, Signal}
import com.cluda.tradersbit.signals.protocoll.{GetSignals, DatabaseReadException, InalidCombinationOfParametersException}

class GetSignalsActor(globalRequestID: String, databaseReaderActor: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case request: GetSignals =>
      log.info(s"[$globalRequestID]: Got GetSignals. Forwarding to databaseReaderActor and becoming responder")
      databaseReaderActor ! (globalRequestID, request)
      context.become(responder(sender()))
  }

  def responder(respondTo: ActorRef): Receive = {
    case signals: Seq[Signal] =>
      log.info(s"[$globalRequestID]: Got "+ signals.length + " signal(s) back: " + signals)
      import SignalJsonProtocol._
      import spray.json._
      respondTo ! HttpResponse(OK, entity = signals.map(_.toJson).toJson.prettyPrint)
      self ! PoisonPill

    case e: DatabaseReadException =>
      log.error(s"[$globalRequestID]: returns 'no stream with that id'. Reason: " + e.reason)
      respondTo ! HttpResponse(NoContent)

    case e: InalidCombinationOfParametersException =>
      log.error(s"[$globalRequestID]: Reason: "  + e.info)
      respondTo ! HttpResponse(BadRequest, entity = "invalid combination of parameters")
  }
}

object GetSignalsActor {
  def props(globalRequestID: String, databaseReaderActor: ActorRef): Props = Props(new GetSignalsActor(globalRequestID, databaseReaderActor))
}