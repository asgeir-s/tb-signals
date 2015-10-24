package com.cluda.tradersbit.signals.messaging.getsignal

import java.util.UUID

import akka.actor.Props
import akka.http.scaladsl.model.headers.RawHeader
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.tradersbit.signals.protocoll.GetSignalsParams
import com.cluda.tradersbit.signals.DatabaseUtilBlockingForTests
import com.cluda.tradersbit.signals.getsignal.DatabaseReaderActor
import com.cluda.tradersbit.signals.messaging.MessagingTest
import com.cluda.tradersbit.signals.model.Signal
import com.cluda.tradersbit.signals.protocoll.{GetSignalsParams, GetSignals}


class DatabaseReaderActorTest extends MessagingTest {

  val testStream = "databasereaderactortest"
  def globalRequestID = UUID.randomUUID().toString


  override def beforeAll(): Unit = {
    DatabaseUtilBlockingForTests.dropTableIfItExists(testStream, context)
    DatabaseUtilBlockingForTests.createDummySignalsTable(testStream, context)
  }

  "when receiving 'GetSignals' with id of an existing stream and 'numberOfSignals' set to None it" should
    "return all signals for the stream" in {
    val getSignalsActor = TestProbe()
    val actor = TestActorRef(Props[DatabaseReaderActor], "DatabaseReaderActor1")

    getSignalsActor.send(actor, (globalRequestID, GetSignals(testStream)))
    val respond = getSignalsActor.expectMsgType[Seq[Signal]]
    assert(respond.length == 13)
  }


  "when receiving 'GetSignals' with id of an NOT existing stream and 'numberOfSignals' set to None it" should
    "return an empty array" in {
    val getSignalsActor = TestProbe()
    val actor = TestActorRef(Props[DatabaseReaderActor], "DatabaseReaderActor2")

    getSignalsActor.send(actor, (globalRequestID, GetSignals("notexistingdb")))
    val respond = getSignalsActor.expectMsgType[Seq[Signal]]
    assert(respond.isEmpty)

  }


  "when receiving 'GetSignals' with id of an existing stream and 'numberOfSignals' set to 3 it" should
    "return the 3 newest signals" in {
    val getSignalsActor = TestProbe()
    val actor = TestActorRef(Props[DatabaseReaderActor], "DatabaseReaderActor3")

    getSignalsActor.send(actor, (globalRequestID, GetSignals(testStream, GetSignalsParams(lastN = Some(3)))))
    val respond = getSignalsActor.expectMsgType[Seq[Signal]]
    assert(respond.length == 3)
    respond.foreach(x => assert(x.id.get > 10))
    assert(respond(0).id.get > respond(1).id.get)
  }

  "when receiving 'GetSignals' with id of an existing stream and 'numberOfSignals' set to 10 it" should
    "return the 10 newest signals" in {
    val getSignalsActor = TestProbe()
    val actor = TestActorRef(Props[DatabaseReaderActor], "DatabaseReaderActor4")

    getSignalsActor.send(actor, (globalRequestID, GetSignals(testStream, GetSignalsParams(lastN = Some(10)))))
    val respond = getSignalsActor.expectMsgType[Seq[Signal]]
    assert(respond.length == 10)
    respond.foreach(x => assert(x.id.get > 3))
    assert(respond(0).id.get > respond(1).id.get)
  }

}
