package com.cluda.coinsignals.signals.messaging.getsignal

import akka.http.scaladsl.model.HttpResponse
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.TestData
import com.cluda.coinsignals.signals.getsignal.GetSignalsActor
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.protocoll.GetSignals

class GetSignalsActorTest extends MessagingTest {

  "when receiving 'GetSignals' it" should
    "forward it to the 'databaseReaderActor' and become responder" in {
    val databaseReaderActor = TestProbe()
    val interfaceActor = TestProbe()

    val actor = TestActorRef(GetSignalsActor.props(databaseReaderActor.ref), "DatabaseReaderActor1")

    interfaceActor.send(actor, GetSignals("someStreamId"))
    val respond = databaseReaderActor.expectMsgType[GetSignals]
    assert(respond.streamID == "someStreamId")
  }

  "when in respoder mode and receiving 'Seq[Signals]' it" should
    "create http responds and send it to the interfaceActor" in {
    val databaseReaderActor = TestProbe()
    val interfaceActor = TestProbe()

    val actor = TestActorRef(GetSignalsActor.props(databaseReaderActor.ref), "DatabaseReaderActor2")

    interfaceActor.send(actor, GetSignals("someStreamId")) // become responder

    databaseReaderActor.send(actor, TestData.signalSeq)

    interfaceActor.expectMsgType[HttpResponse]

  }

}
