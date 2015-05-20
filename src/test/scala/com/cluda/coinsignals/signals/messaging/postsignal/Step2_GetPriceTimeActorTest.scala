package com.cluda.coinsignals.signals.messaging.postsignal

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.model.Meta
import com.cluda.coinsignals.signals.postsignal.Step2_GetPriceTimeActor
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException

class Step2_GetPriceTimeActorTest extends MessagingTest {

  // mocks
  val writeDatabaseActor = TestProbe()
  val respondsActor = TestProbe()

  val actor = TestActorRef(Props(new Step2_GetPriceTimeActor(writeDatabaseActor.ref)), "getPriceActor")

  "when receiving 'Meta' it" should
    "get the price for the exchange(bitstamp), add it to the 'Meta' and send 'Meta' to the 'databaseActor'" in {
    actor ! Meta(None, "test-id", 1, Some("bitstamp"), None, None)
    val theResponds = writeDatabaseActor.expectMsgType[Meta]
    assert(theResponds.price isDefined)
    assert(theResponds.price.get >= 0)
    assert(theResponds.timestamp isDefined)
    assert(theResponds.timestamp.get >= 0)
  }

  "when receiving 'Meta' it" should
    "get the price for the exchange(bitfinex), add it to the 'Meta' and send 'Meta' to the 'databaseActor'" in {
    actor ! Meta(None, "test-id", 1, Some("bitfinex"), None, None)
    val theResponds = writeDatabaseActor.expectMsgType[Meta]
    assert(theResponds.price isDefined)
    assert(theResponds.price.get >= 0)
    assert(theResponds.timestamp isDefined)
    assert(theResponds.timestamp.get >= 0)
  }

  "when receiving 'Meta' with unvalid exchnage it" should
    "send a 'SignalProcessingException' to the respondsActor" in {
    actor ! Meta(Some(respondsActor.ref), "test-id", 1, Some("lol"), None, None)
    writeDatabaseActor.expectNoMsg()
    respondsActor.expectMsgType[SignalProcessingException]
  }
}