package com.cluda.coinsignals.signals.messaging.postsignal

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.model.Meta
import com.cluda.coinsignals.signals.postsignal.Step1_GetExchangeActor

class Step1_GetExchangeActorTest extends MessagingTest {

  "when receiving 'Meta' it" should
    "get the exchange from the 'stream-info-service' and set it in 'Meta'. The new 'Meta' should be sent to 'getPriceActor'" in {
    // mocks
    val getPriceActor = TestProbe()

    val actor = TestActorRef(Props(new Step1_GetExchangeActor(getPriceActor.ref)), "postSignalActorTest")

    actor ! Meta(None, "test-id", 1, None, None, None)
    val theResponds = getPriceActor.expectMsgType[Meta]
    assert(theResponds.exchange.get == "bitstamp")
  }
}
