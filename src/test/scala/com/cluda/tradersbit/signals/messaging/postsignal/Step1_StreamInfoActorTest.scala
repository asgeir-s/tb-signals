package com.cluda.tradersbit.signals.messaging.postsignal

import java.util.UUID

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.tradersbit.signals.messaging.MessagingTest
import com.cluda.tradersbit.signals.model.Meta
import com.cluda.tradersbit.signals.postsignal.Step1_StreamInfoActor

class Step1_StreamInfoActorTest extends MessagingTest {

  val streamID = config.getString("testSignalStream.id")
  def globalRequestID = UUID.randomUUID().toString

  "when receiving 'Meta' it" should
    "get the exchange from the 'stream-info-service' and set it in 'Meta'. The new 'Meta' should be sent " +
      "to 'getPriceActor'" in {
    // mocks
    val getPriceActor = TestProbe()

    val actor = TestActorRef(Props(new Step1_StreamInfoActor(getPriceActor.ref)), "postSignalActorTest")

    actor ! (globalRequestID, Meta(None, streamID, 1, None, None, None, None))
    val theResponds = getPriceActor.expectMsgType[(String, Meta)]._2
    assert(theResponds.exchange.get == "bitfinex")
  }
}
