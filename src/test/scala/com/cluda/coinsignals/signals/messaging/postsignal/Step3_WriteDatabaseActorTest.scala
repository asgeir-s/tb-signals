package com.cluda.coinsignals.signals.messaging.postsignal

import java.util.UUID

import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.DatabaseUtilBlockingForTests
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.model.{Meta, Signal}
import com.cluda.coinsignals.signals.postsignal.Step3_WriteDatabaseActor
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException

class Step3_WriteDatabaseActorTest extends MessagingTest {

  val testID = "step3writedatabaseandnotifyactortest"
  def globalRequestID = UUID.randomUUID().toString

  override def beforeAll(): Unit = {
    DatabaseUtilBlockingForTests.dropTableIfItExists(testID, context)
  }

  // mocks
  val respondsActor = TestProbe()
  val notificationActor = TestProbe()

  val actor = TestActorRef(Step3_WriteDatabaseActor.props(notificationActor.ref), "getPriceActor")

  "when receiving 'Meta' about a new signal LONG it" should
    "write the signal to the database and return the new signal to the 'respondsActor'" in {
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn")))
    val notificationResponds = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds.length == 1)
    assert(theResponds.head.id.isDefined)
    assert(theResponds.head.change == BigDecimal(0))
    assert(theResponds == notificationResponds._4)
    assert(notificationResponds._3 == "arn")
    assert(notificationResponds._2 == testID)

  }

  "when receiving 'Meta' about a new signal that is a dupicate (same position as last signal) it" should
    "return a 'SignalProcessingException'" in {
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(220)), Some(3999999l), Some("arn")))
    val theResponds = respondsActor.expectMsgType[(String, SignalProcessingException)]._2
    notificationActor.expectNoMsg()
  }

  "when receiving 'Meta' about a new CLOSE signal it" should
    "write the signal to the database and return the new signal to the 'respondsActor'. And the change could be set correctly" in {
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(800)), Some(2999999l), Some("arn")))
    val notificationResponds = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds.length == 1)
    assert(theResponds.head.id.isDefined)
    assert(theResponds.head.change == BigDecimal(1))
    assert(theResponds == notificationResponds._4)
    assert(notificationResponds._3 == "arn")
    assert(notificationResponds._2 == testID)

  }

  "it" should
    "handle a losing trade LONG -> CLOSE" in {
    // take LONG
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn")))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._4)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)




    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn")))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(-0.5))
    assert(theResponds2 == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)

  }

  "it" should
    "handle a winning trade LONG -> CLOSE" in {
    // take LONG
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn")))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._4)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(150)), Some(2999999l), Some("arn")))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(0.5))
    assert(theResponds2 == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)


  }

  "it" should
    "handle a winning trade SHORT -> CLOSE" in {
    // take SHORT
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn")))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._4)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn")))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(0.5))
    assert(theResponds2 == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)

  }

  "it" should
    "handle a losing trade SHORT -> CLOSE" in {
    // take SHORT
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn")))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._4)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn")))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(-1))
    assert(theResponds2 == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)

  }

  "it" should
    "handle a position change from SHORT to LONG" in {
    // take LONG
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn")))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._4)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // take SHORT
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn")))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds2.length == 2)
    assert(theResponds2(0).id.isDefined)
    assert(theResponds2(0).change == BigDecimal(0))
    assert(theResponds2(1).id.isDefined)
    assert(theResponds2(1).change == BigDecimal(1))
    assert(theResponds2 == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn")))
    respondsActor.expectMsgType[(String, Seq[Signal])]._2
    notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
  }

  "it" should
    "handle a position change from LONG to SHORT" in {
    // take SHORT
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn")))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._4)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // take LONG
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn")))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[(String, Seq[Signal])]._2
    assert(theResponds2.length == 2)
    assert(theResponds2(0).id.isDefined)
    assert(theResponds2(0).change == BigDecimal(0))
    assert(theResponds2(1).id.isDefined)
    assert(theResponds2(1).change == BigDecimal(-1))
    assert(theResponds2 == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn")))
    respondsActor.expectMsgType[(String, Seq[Signal])]._2
    notificationActor.expectMsgType[(String, String, String, Seq[Signal])]
  }

}
