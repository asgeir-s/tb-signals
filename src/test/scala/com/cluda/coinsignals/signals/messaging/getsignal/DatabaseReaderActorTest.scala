package com.cluda.coinsignals.signals.messaging.getsignal

import java.util.UUID

import akka.actor.Props
import akka.http.scaladsl.model.headers.RawHeader
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.DatabaseUtilBlockingForTests
import com.cluda.coinsignals.signals.getsignal.DatabaseReaderActor
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.model.Signal
import com.cluda.coinsignals.signals.protocoll.{GetSignalsParams, DatabaseReadException, GetSignals}


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
    val respond = getSignalsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(respond.length == 13)
  }

  "when receiving 'GetSignals' with id of an NOT existing stream and 'numberOfSignals' set to None it" should
    "return an error saying stream does not exist" in {
    val getSignalsActor = TestProbe()
    val actor = TestActorRef(Props[DatabaseReaderActor], "DatabaseReaderActor2")

    getSignalsActor.send(actor, (globalRequestID, GetSignals("notexistingdb")))
    val respond = getSignalsActor.expectMsgType[(String, DatabaseReadException)]._2
    assert(respond.reason.contains("not exist"))

  }

  "when receiving 'GetSignals' with id of an existing stream and 'numberOfSignals' set to 3 it" should
    "return the 3 newest signals" in {
    val getSignalsActor = TestProbe()
    val actor = TestActorRef(Props[DatabaseReaderActor], "DatabaseReaderActor3")

    getSignalsActor.send(actor, (globalRequestID, GetSignals(testStream, GetSignalsParams(lastN = Some(3)))))
    val respond = getSignalsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(respond.length == 3)
    respond.foreach(x => assert(x.id.get > 10))
    assert(respond(0).id.get > respond(1).id.get)
  }

  "when receiving 'GetSignals' with id of an existing stream and 'numberOfSignals' set to 10 it" should
    "return the 10 newest signals" in {
    val getSignalsActor = TestProbe()
    val actor = TestActorRef(Props[DatabaseReaderActor], "DatabaseReaderActor4")

    getSignalsActor.send(actor, (globalRequestID, GetSignals(testStream, GetSignalsParams(lastN = Some(10)))))
    val respond = getSignalsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(respond.length == 10)
    respond.foreach(x => assert(x.id.get > 3))
    assert(respond(0).id.get > respond(1).id.get)
  }

}
