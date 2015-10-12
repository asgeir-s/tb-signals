package com.cluda.coinsignals.signals.messaging.postsignal

import java.util.UUID

import akka.actor.Props
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.TestData
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.model.{Meta, SignalJsonProtocol}
import com.cluda.coinsignals.signals.postsignal.PostSignalActor
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException

class PostSignalActorTest extends MessagingTest {

  def globalRequestID = UUID.randomUUID().toString

  "when receiving 'Meta' it" should
    "set itself as 'respondsActor' and send the new 'Meta' to the 'getExchangeActor'" in {
    // mocks
    val getExchangeActor = TestProbe()
    val parent = TestProbe()
    val actor = TestActorRef(Props(new PostSignalActor(getExchangeActor.ref)), parent.ref, "postSignalActorTest")

    actor ! (globalRequestID, Meta(None, "test-id", 1, None, None, None, None))
    val newMeta = getExchangeActor.expectMsgType[(String, Meta)]._2
    assert(newMeta.respondsActor.get == actor)
  }

  "when in responder mode it" should
    "when receiving a 'signal' return a 'HttpResponse', including the signal as json, to its parent" in {
    // mocks
    val getExchangeActor = TestProbe()
    val interface = TestProbe()

    val actor = TestActorRef(Props(new PostSignalActor(getExchangeActor.ref)), "postSignalActorTest2")
    interface.send(actor, (globalRequestID, Meta(None, "test-id", 1, None, None, None, None))) // become reponder

    actor ! (globalRequestID, Seq(TestData.signal1))
    val responseParent = interface.expectMsgType[HttpResponse]
    import SignalJsonProtocol._
    import spray.json._

    //TODO: make a equalent test but not this one, because this wil fail with crypt and jtw
    //assert(responseParent == SendReceiveHelper.Sec(OK, entity = """[""" + TestData.signal1.toJson.prettyPrint + """]"""))

    // check that the actor killed itself
    actor ! TestData.signal1
    interface.expectNoMsg()

  }

  "when receiving a 'SignalProcessingException' in responder mode it" should
    "send a 'HttpResponse' with InternalServerError-code" in {
    // mocks
    val getExchangeActor = TestProbe()
    val interface = TestProbe()

    val actor = TestActorRef(Props(new PostSignalActor(getExchangeActor.ref)), "postSignalActorTest3")
    interface.send(actor, (globalRequestID, Meta(None, "test-id", 1, None, None, None, None))) // become reponder

    actor ! (globalRequestID, SignalProcessingException("some error"))
    val responseParent = interface.expectMsgType[HttpResponse]

    // check that the actor killed itself
    actor ! (globalRequestID, TestData.signal1)
    interface.expectNoMsg()
  }

}
