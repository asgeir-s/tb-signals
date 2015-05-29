package com.cluda.coinsignals.signals.messaging.postsignal

import akka.testkit.{TestActorRef, TestProbe}
import com.cluda.coinsignals.signals.DatabaseUtil
import com.cluda.coinsignals.signals.messaging.MessagingTest
import com.cluda.coinsignals.signals.model.{Meta, Signal}
import com.cluda.coinsignals.signals.postsignal.Step3_WriteDatabaseActor
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException

class Step3_WriteDatabaseActorTest extends MessagingTest {

  val testID = "step3writedatabaseandnotifyactortest"

  override def beforeAll(): Unit = {
    DatabaseUtil.dropTableIfItExists(testID, context)
  }

  // mocks
  val respondsActor = TestProbe()
  val notificationActor = TestProbe()

  val actor = TestActorRef(Step3_WriteDatabaseActor.props(notificationActor.ref), "getPriceActor")

  "when receiving 'Meta' about a new signal LONG it" should
    "write the signal to the database and return the new signal to the 'respondsActor'" in {
    actor ! Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn"))
    val notificationResponds = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds.length == 1)
    assert(theResponds.head.id.isDefined)
    assert(theResponds.head.change == BigDecimal(0))
    assert(theResponds == notificationResponds._2)
    assert(notificationResponds._1 == "arn")

  }

  "when receiving 'Meta' about a new signal that is a dupicate (same position as last signal) it" should
    "return a 'SignalProcessingException'" in {
    actor ! Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(220)), Some(3999999l), Some("arn"))
    val theResponds = respondsActor.expectMsgType[SignalProcessingException]
    notificationActor.expectNoMsg()
  }

  "when receiving 'Meta' about a new CLOSE signal it" should
    "write the signal to the database and return the new signal to the 'respondsActor'. And the change could be set correctly" in {
    actor ! Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(800)), Some(2999999l), Some("arn"))
    val notificationResponds = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds.length == 1)
    assert(theResponds.head.id.isDefined)
    assert(theResponds.head.change == BigDecimal(1))
    assert(theResponds == notificationResponds._2)
    assert(notificationResponds._1 == "arn")

  }

  "it" should
    "handle a losing trade LONG -> CLOSE" in {
    // take LONG
    actor ! Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"))
    val notificationResponds1 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._2)
    assert(notificationResponds1._1 == "arn")



    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"))
    val notificationResponds2 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(-0.5))
    assert(theResponds2 == notificationResponds2._2)
    assert(notificationResponds2._1 == "arn")
  }

  "it" should
    "handle a winning trade LONG -> CLOSE" in {
    // take LONG
    actor ! Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"))
    val notificationResponds1 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._2)
    assert(notificationResponds1._1 == "arn")

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(150)), Some(2999999l), Some("arn"))
    val notificationResponds2 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(0.5))
    assert(theResponds2 == notificationResponds2._2)
    assert(notificationResponds2._1 == "arn")

  }

  "it" should
    "handle a winning trade SHORT -> CLOSE" in {
    // take SHORT
    actor ! Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"))
    val notificationResponds1 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._2)
    assert(notificationResponds1._1 == "arn")

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"))
    val notificationResponds2 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(0.5))
    assert(theResponds2 == notificationResponds2._2)
    assert(notificationResponds2._1 == "arn")
  }

  "it" should
    "handle a losing trade SHORT -> CLOSE" in {
    // take SHORT
    actor ! Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"))
    val notificationResponds1 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._2)
    assert(notificationResponds1._1 == "arn")

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn"))
    val notificationResponds2 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 1)
    assert(theResponds2.head.id.isDefined)
    assert(theResponds2.head.change == BigDecimal(-1))
    assert(theResponds2 == notificationResponds2._2)
    assert(notificationResponds2._1 == "arn")
  }

  "it" should
    "handle a position change from SHORT to LONG" in {
    // take LONG
    actor ! Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"))
    val notificationResponds1 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._2)
    assert(notificationResponds1._1 == "arn")

    // take SHORT
    actor ! Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn"))
    val notificationResponds2 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 2)
    assert(theResponds2(0).id.isDefined)
    assert(theResponds2(0).change == BigDecimal(0))
    assert(theResponds2(1).id.isDefined)
    assert(theResponds2(1).change == BigDecimal(1))
    assert(theResponds2 == notificationResponds2._2)
    assert(notificationResponds2._1 == "arn")

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"))
    respondsActor.expectMsgType[Seq[Signal]]
    notificationActor.expectMsgType[(String, Seq[Signal])]
  }

  "it" should
    "handle a position change from LONG to SHORT" in {
    // take SHORT
    actor ! Meta(Some(respondsActor.ref), testID, -1, Some("bitstamp"), Some(BigDecimal(200)), Some(2999999l), Some("arn"))
    val notificationResponds1 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds1 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds1.length == 1)
    assert(theResponds1.head.id.isDefined)
    assert(theResponds1.head.change == BigDecimal(0))
    assert(theResponds1 == notificationResponds1._2)
    assert(notificationResponds1._1 == "arn")

    // take LONG
    actor ! Meta(Some(respondsActor.ref), testID, 1, Some("bitstamp"), Some(BigDecimal(400)), Some(2999999l), Some("arn"))
    val notificationResponds2 = notificationActor.expectMsgType[(String, Seq[Signal])]
    val theResponds2 = respondsActor.expectMsgType[Seq[Signal]]
    assert(theResponds2.length == 2)
    assert(theResponds2(0).id.isDefined)
    assert(theResponds2(0).change == BigDecimal(0))
    assert(theResponds2(1).id.isDefined)
    assert(theResponds2(1).change == BigDecimal(-1))
    assert(theResponds2 == notificationResponds2._2)
    assert(notificationResponds2._1 == "arn")

    // CLOSE it
    actor ! Meta(Some(respondsActor.ref), testID, 0, Some("bitstamp"), Some(BigDecimal(100)), Some(2999999l), Some("arn"))
    respondsActor.expectMsgType[Seq[Signal]]
    notificationActor.expectMsgType[(String, Seq[Signal])]
  }

}
