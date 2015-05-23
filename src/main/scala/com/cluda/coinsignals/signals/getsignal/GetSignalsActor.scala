package com.cluda.coinsignals.signals.getsignal

import akka.actor._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import com.cluda.coinsignals.signals.model.{Signal, SignalJsonProtocol}
import com.cluda.coinsignals.signals.protocoll.{InalidCombinationOfParametersException, DatabaseReadException, GetSignals}

class GetSignalsActor(databaseReaderActor: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case request: GetSignals =>
      log.info("GetSignalsActor: Got GetSignals. Forwarding to databaseReaderActor and becoming responder")
      databaseReaderActor ! request
      context.become(responder(sender()))
  }

  def responder(respondTo: ActorRef): Receive = {
    case signals: Seq[Signal] =>
      log.info("GetSignalsActor: Got signal(s) back: " + signals)
      import SignalJsonProtocol._
      import spray.json._
      respondTo ! HttpResponse(OK, entity = signals.map(_.toJson).toJson.prettyPrint)
      self ! PoisonPill

    case e: DatabaseReadException =>
      log.error("GetSignalsActor returns 'no stream with that id'. Reason: " + e.reason)
      respondTo ! HttpResponse(NoContent)

    case e: InalidCombinationOfParametersException =>
      log.error(e.info)
      respondTo ! HttpResponse(BadRequest, entity = "invalid combination of parameters")
  }
}

object GetSignalsActor {
  def props(databaseReaderActor: ActorRef): Props = Props(new GetSignalsActor(databaseReaderActor))
}