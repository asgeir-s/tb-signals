package com.cluda.coinsignals.signals.messaging.getsignal

import java.util.UUID

import akka.http.scaladsl.model.HttpResponse
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.TestData
import com.cluda.coinsignals.signals.getsignal.GetSignalsActor
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.protocoll.GetSignals

class GetSignalsActorTest extends MessagingTest {

  def globalRequestID = UUID.randomUUID().toString

  "when receiving 'GetSignals' it" should
    "forward it to the 'databaseReaderActor' and become responder" in {
    val databaseReaderActor = TestProbe()
    val interfaceActor = TestProbe()
    val actor = TestActorRef(GetSignalsActor.props(databaseReaderActor.ref), "DatabaseReaderActor1")

    interfaceActor.send(actor, (globalRequestID, GetSignals("someStreamId")))
    val respond = databaseReaderActor.expectMsgType[(String, GetSignals)]._2
    assert(respond.streamID == "someStreamId")
  }

  "when in respoder mode and receiving 'Seq[Signals]' it" should
    "create http responds and send it to the interfaceActor" in {
    val databaseReaderActor = TestProbe()
    val interfaceActor = TestProbe()
    val actor = TestActorRef(GetSignalsActor.props(databaseReaderActor.ref), "DatabaseReaderActor2")

    interfaceActor.send(actor, (globalRequestID, GetSignals("someStreamId"))) // become responder
    databaseReaderActor.send(actor, (globalRequestID, TestData.signalSeq))
    interfaceActor.expectMsgType[HttpResponse]

  }

}
