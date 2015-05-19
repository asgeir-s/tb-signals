package com.cluda.coinsignals.signals.messaging.post

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.model.{Meta, Signal}
import com.cluda.coinsignals.signals.postsignal.Step3_WriteDatabaseAndNotifyActor
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException

import scala.concurrent.duration._

class Step3_WriteDatabaseAndNotifyActorTest extends MessagingTest {

  // mocks
  val respondsActor = TestProbe()

  val actor = TestActorRef(Props[Step3_WriteDatabaseAndNotifyActor], "getPriceActor")
  actor ! Meta(Some(respondsActor.ref), "test-id", 0, Some("bitstamp"), Some(BigDecimal(200)), Some(1999999l)) // setts position to CLOSE
  respondsActor.receiveOne(3 seconds)

  "when receiving 'Meta' about a new signal LONG it" should
    "write the signal to the database and return the new signal to the 'respondsActor'" in {
    actor ! Meta(Some(respondsActor.ref), "test-id", 1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l))
    val theResponds = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds.length == 1)
    assert(theResponds.head.id != None)
    assert(theResponds.head.change == BigDecimal(0))

  }

  "when receiving 'Meta' about a new signal that is a dupicate (same position as last signal) it" should
    "return a 'SignalProcessingException'" in {
    actor ! Meta(Some(respondsActor.ref), "test-id", 1, Some("bitstamp"), Some(BigDecimal(220)), Some(3999999l))
    val theResponds = respondsActor.expectMsgType[SignalProcessingException]
  }

  "when receiving 'Meta' about a new CLOSE signal it" should
    "write the signal to the database and return the new signal to the 'respondsActor'. And the change could be set correctly" in {
    actor ! Meta(Some(respondsActor.ref), "test-id", 0, Some("bitstamp"), Some(BigDecimal(800)), Some(2999999l))
    val theResponds = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds.length == 1)
    assert(theResponds.head.id != None)
    assert(theResponds.head.change == BigDecimal(1))
  }

  "it" should
    "handle a losing trade LONG -> CLOSE" in {
    // take LONG
    actor ! Meta(Some(respondsActor.ref), "test-id", 1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l))
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id != None)
    assert(theResponds1.head.change == BigDecimal(0))

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), "test-id", 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l))
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id != None)
    assert(theResponds2.head.change == BigDecimal(-0.5))
  }

  "it" should
    "handle a winning trade LONG -> CLOSE" in {
    // take LONG
    actor ! Meta(Some(respondsActor.ref), "test-id", 1, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l))
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id != None)
    assert(theResponds1.head.change == BigDecimal(0))

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), "test-id", 0, Some("bitstamp"), Some(BigDecimal(150)), Some(2999999l))
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id != None)
    assert(theResponds2.head.change == BigDecimal(0.5))
  }

  "it" should
    "handle a winning trade SHORT -> CLOSE" in {
    // take SHORT
    actor ! Meta(Some(respondsActor.ref), "test-id", -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l))
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id != None)
    assert(theResponds1.head.change == BigDecimal(0))

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), "test-id", 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l))
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id != None)
    assert(theResponds2.head.change == BigDecimal(0.5))
  }

  "it" should
    "handle a losing trade SHORT -> CLOSE" in {
    // take SHORT
    actor ! Meta(Some(respondsActor.ref), "test-id", -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l))
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id != None)
    assert(theResponds1.head.change == BigDecimal(0))

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), "test-id", 0, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l))
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id != None)
    assert(theResponds2.head.change == BigDecimal(-1))
  }

  "it" should
    "handle a position change from SHORT to LONG" in {
    // take LONG
    actor ! Meta(Some(respondsActor.ref), "test-id", 1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l))
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id != None)
    assert(theResponds1.head.change == BigDecimal(0))

    // take SHORT
    actor ! Meta(Some(respondsActor.ref), "test-id", -1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l))
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 2)
    assert(theResponds2(0).id != None)
    assert(theResponds2(0).change == BigDecimal(0))
    assert(theResponds2(1).id != None)
    assert(theResponds2(1).change == BigDecimal(1))

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), "test-id", 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l))
    respondsActor.expectMsgType[Seq[Signal]]

  }

  "it" should
    "handle a position change from LONG to SHORT" in {
    // take SHORT
    actor ! Meta(Some(respondsActor.ref), "test-id", -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l))
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id != None)
    assert(theResponds1.head.change == BigDecimal(0))

    // take LONG
    actor ! Meta(Some(respondsActor.ref), "test-id", 1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l))
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 2)
    assert(theResponds2(0).id != None)
    assert(theResponds2(0).change == BigDecimal(0))
    assert(theResponds2(1).id != None)
    assert(theResponds2(1).change == BigDecimal(-1))

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), "test-id", 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l))
    respondsActor.expectMsgType[Seq[Signal]]

  }

}
