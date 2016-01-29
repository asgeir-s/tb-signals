package com.cluda.tradersbit.signals.messaging.postsignal

import java.util.UUID

import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.tradersbit.signals.model.Signal
import com.cluda.tradersbit.signals.DatabaseUtilBlockingForTests
import com.cluda.tradersbit.signals.messaging.MessagingTest
import com.cluda.tradersbit.signals.model.{Meta, Signal}
import com.cluda.tradersbit.signals.postsignal.Step3_WriteDatabaseActor
import com.cluda.tradersbit.signals.protocoll.SignalProcessingException

class Step3_WriteDatabaseActorTest extends MessagingTest {

  val testID = "step3writedatabaseandnotifyactortest"
  def globalRequestID = UUID.randomUUID().toString
  val streamName = Some("test33")

  override def beforeAll(): Unit = {
    DatabaseUtilBlockingForTests.dropTableIfItExists(testID, context)
  }

  // mocks
  val respondsActor = TestProbe()
  val notificationActor = TestProbe()

  val actor = TestActorRef(Step3_WriteDatabaseActor.props(notificationActor.ref), "writeDBActor")

  "when receiving 'Meta' about signals starting with SHORT and going directly to LONG it" should
    "calculate the valueInclFee correctly" in {
    val newStreamID = UUID.randomUUID().toString

    val streamName = Some(newStreamID + "Name")

    val respondsActor = TestProbe()
    val notificationActor = TestProbe()

    val actor = TestActorRef(Step3_WriteDatabaseActor.props(notificationActor.ref), "writeDBActor2")

    actor ! (globalRequestID, Meta(Some(respondsActor.ref), newStreamID, -1, Some("bitfinex"), Some(BigDecimal(382.2100)), Some(1454000046080l), Some("arn"), streamName))
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    println(theResponds1)
    assert(theResponds1.length == 1)
    assert(theResponds1.head.valueInclFee >= 1)

    actor ! (globalRequestID, Meta(Some(respondsActor.ref), newStreamID, 1, Some("bitfinex"), Some(BigDecimal(367.1100)), Some(1454045134848l), Some("arn"), streamName))
    val theResponds3 = respondsActor.expectMsgType[Seq[Signal]]
    println("imp: " + theResponds3.last)
    println("imp: " + theResponds3.head)

    assert(theResponds3.length == 2)
    assert(theResponds3.head.valueInclFee >= 1)

    actor ! (globalRequestID, Meta(Some(respondsActor.ref), newStreamID, 0, Some("bitfinex"), Some(BigDecimal(368.0800)), Some(1454045265920l), Some("arn"), streamName))
    val theResponds4 = respondsActor.expectMsgType[Seq[Signal]]
    println(theResponds4)
    assert(theResponds4.length == 1)
    assert(theResponds4.head.valueInclFee >= 1)

    actor ! (globalRequestID, Meta(Some(respondsActor.ref), newStreamID, -1, Some("bitfinex"), Some(BigDecimal(370.3700)), Some(1454046445568l), Some("arn"), streamName))
    val theResponds5 = respondsActor.expectMsgType[Seq[Signal]]
    println(theResponds5)
    assert(theResponds5.length == 1)
    assert(theResponds5.head.valueInclFee >= 1)
  }



  "when receiving 'Meta' about a new signal LONG it" should
    "write the signal to the database and return the new signal to the 'respondsActor'" in {
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds = notificationActor.expectMsgType[(String, String, String ,String, Seq[Signal])]
    val theResponds = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds.length == 1)
    assert(theResponds.head.id.isDefined)
    assert(theResponds.head.change == BigDecimal(0))
    assert(streamName.get == notificationResponds._4)
    assert(theResponds == notificationResponds._5)
    assert(notificationResponds._3 == "arn")
    assert(notificationResponds._2 == testID)

  }

  "when receiving 'Meta' about a new signal that is a dupicate (same position as last signal) it" should
    "return a 'SignalProcessingException'" in {
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(220)), Some(3999999l), Some("arn"), streamName))
    val theResponds = respondsActor.expectMsgType[SignalProcessingException]
    notificationActor.expectNoMsg()
  }

  "when receiving 'Meta' about a new CLOSE signal it" should
    "write the signal to the database and return the new signal to the 'respondsActor'. And the change could be set correctly" in {
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(800)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds.length == 1)
    assert(theResponds.head.id.isDefined)
    assert(theResponds.head.change == BigDecimal(1))
    assert(streamName.get == notificationResponds._4)
    assert(theResponds == notificationResponds._5)
    assert(notificationResponds._3 == "arn")
    assert(notificationResponds._2 == testID)
  }

  "it" should
    "handle a losing trade LONG -> CLOSE" in {
    // take LONG
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(streamName.get == notificationResponds1._4)
    assert(theResponds1 == notificationResponds1._5)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(-0.5))
    assert(streamName.get == notificationResponds2._4)
    assert(theResponds2 == notificationResponds2._5)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)

  }

  "it" should
    "handle a winning trade LONG -> CLOSE" in {
    // take LONG
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(streamName.get == notificationResponds1._4)
    assert(theResponds1 == notificationResponds1._5)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(150)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(0.5))
    assert(theResponds2 == notificationResponds2._5)
    assert(streamName.get == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)


  }

  "it" should
    "handle a winning trade SHORT -> CLOSE" in {
    // take SHORT
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(streamName.get == notificationResponds1._4)
    assert(theResponds1 == notificationResponds1._5)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(0.5))
    assert(theResponds2 == notificationResponds2._5)
    assert(streamName.get == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)

  }

  "it" should
    "handle a losing trade SHORT -> CLOSE" in {
    // take SHORT
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(streamName.get == notificationResponds1._4)
    assert(theResponds1 == notificationResponds1._5)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(-1))
    assert(theResponds2 == notificationResponds2._5)
    assert(streamName.get == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)

  }

  "it" should
    "handle a position change from SHORT to LONG" in {
    // take LONG
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(streamName.get == notificationResponds1._4)
    assert(theResponds1 == notificationResponds1._5)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // take SHORT
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 2)
    assert(theResponds2(0).id.isDefined)
    assert(theResponds2(0).change == BigDecimal(0))
    assert(theResponds2(1).id.isDefined)
    assert(theResponds2(1).change == BigDecimal(1))
    assert(theResponds2 == notificationResponds2._5)
    assert(streamName.get == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"), streamName))
    respondsActor.expectMsgType[Seq[Signal]]
    notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
  }

  "it" should
    "handle a position change from LONG to SHORT" in {
    // take SHORT
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds1 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._5)
    assert(streamName.get == notificationResponds1._4)
    assert(notificationResponds1._3 == "arn")
    assert(notificationResponds1._2 == testID)


    // take LONG
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn"), streamName))
    val notificationResponds2 = notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 2)
    assert(theResponds2(0).id.isDefined)
    assert(theResponds2(0).change == BigDecimal(0))
    assert(theResponds2(1).id.isDefined)
    assert(theResponds2(1).change == BigDecimal(-1))
    assert(theResponds2 == notificationResponds2._5)
    assert(streamName.get == notificationResponds2._4)
    assert(notificationResponds2._3 == "arn")
    assert(notificationResponds2._2 == testID)


    // CLOSE it
    actor ! (globalRequestID, Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"), streamName))
    respondsActor.expectMsgType[Seq[Signal]]
    notificationActor.expectMsgType[(String, String, String, String, Seq[Signal])]
  }

}
