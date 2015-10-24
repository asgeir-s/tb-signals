package com.cluda.tradersbit.signals.messaging.postsignal

import java.util.UUID

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.tradersbit.signals.messaging.MessagingTest
import com.cluda.tradersbit.signals.model.Meta
import com.cluda.tradersbit.signals.postsignal.Step2_GetPriceTimeActor
import com.cluda.tradersbit.signals.protocoll.SignalProcessingException

class Step2_GetPriceTimeActorTest extends MessagingTest {

  // mocks
  val writeDatabaseActor = TestProbe()
  val respondsActor = TestProbe()
  def globalRequestID = UUID.randomUUID().toString

  val actor = TestActorRef(Props(new Step2_GetPriceTimeActor(writeDatabaseActor.ref)), "getPriceActor")

  "when receiving 'Meta' it" should
    "get the price for the exchange(bitstamp), add it to the 'Meta' and send 'Meta' to the 'databaseActor'" in {
    actor ! (globalRequestID, Meta(None, "test-id", 1, Some("bitstamp"), None, None, Some("arn")))
    val theResponds = writeDatabaseActor.expectMsgType[(String, Meta)]._2
    assert(theResponds.price.isDefined)
    assert(theResponds.price.get >= 0)
    assert(theResponds.timestamp.isDefined)
    assert(theResponds.timestamp.get >= 0)
  }

  //TODO: fix for bitfinex
  /*
  "when receiving 'Meta' it" should
    "get the price for the exchange(bitfinex), add it to the 'Meta' and send 'Meta' to the 'databaseActor'" in {
    actor ! Meta(None, "test-id", 1, Some("bitstamp"), None, None, Some("arn"))
    val theResponds = writeDatabaseActor.expectMsgType[Meta]
    assert(theResponds.price.isDefined)
    assert(theResponds.price.get >= 0)
    assert(theResponds.timestamp.isDefined)
    assert(theResponds.timestamp.get >= 0)
  }
  */

  "when receiving 'Meta' with unvalid exchnage it" should
    "send a 'SignalProcessingException' to the respondsActor" in {
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), "test-id", 1, Some("lol"), None, None, Some("arn")))
    writeDatabaseActor.expectNoMsg()
    respondsActor.expectMsgType[SignalProcessingException]
  }
}