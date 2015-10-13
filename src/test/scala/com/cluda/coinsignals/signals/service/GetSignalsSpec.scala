package com.cluda.coinsignals.signals.service

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import com.cluda.coinsignals.signals.model.{Signal, SignalJsonProtocol}
import com.cluda.coinsignals.signals.{DatabaseUtilBlockingForTests, TestData}

class GetSignalsSpec extends TestService {

  val streamID = "getsignalsspec"
  def globalRequestIDHeader() = RawHeader("Global-Request-ID", UUID.randomUUID().toString)

  override def beforeAll(): Unit = {
    DatabaseUtilBlockingForTests.dropTableIfItExists("notexisting", system.dispatcher)
    DatabaseUtilBlockingForTests.dropTableIfItExists(streamID, system.dispatcher)
    DatabaseUtilBlockingForTests.createDummySignalsTable(streamID, system.dispatcher)
  }

  it should "responds withe all the signals for the given stream with the given ID" in {
    Get("/streams/" + streamID + "/signals").addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 13)
      assert(signals.head.id isDefined)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "responds withe all cloased signals when 'onlyClosed' is true" in {
    Get("/streams/" + streamID + "/signals?onlyClosed=true").addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 12)
      assert(signals.head.id isDefined)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "responds withe an empty array when the their is no stream with the given streamID" in {
    Get("/streams/" + "notexisting" + "/signals").addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      assert(responseAs[String]== "[]")

    }
  }

  it should "on status request responds the last signal stream with the given ID" in {
    Get("/streams/" + streamID + "/status").addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 1)
      assert(signals.head.id isDefined)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "be possible to get all signals with id higher then a specified id" in {
    Get("/streams/" + streamID + "/signals?fromId=5").addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 8)
      assert(signals.head.id isDefined)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "be possible to get all signals after a specified timestamp" in {
    Get("/streams/" + streamID + "/signals?beforeTime=" + (TestData.timestamp - 40000)).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 8)
      assert(signals.head.id.get == 8)
      assert(signals.last.id.get == 1)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }

    Get("/streams/" + streamID + "/signals?afterTime=" + (TestData.timestamp - 40001)).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 5)
      assert(signals.head.id isDefined)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "be possible to get all signals between two timestamps" in {
    Get("/streams/" + streamID + "/signals?afterTime=" + (TestData.timestamp - 110000) + "&beforeTime=" + (TestData.timestamp - 50000)).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 5)
      assert(signals.head.id.get == 7)
      assert(signals.last.id.get == 3)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "be possible to get the last n signals" in {
    Get("/streams/" + streamID + "/signals?lastN=" + 6).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 6)
      assert(signals.head.id.get == 13)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }

    Get("/streams/" + streamID + "/signals?lastN=" + 10).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.head.id.get == 13)
      assert(signals.length == 10)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }

    Get("/streams/" + streamID + "/signals?lastN=" + 1).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 1)
      assert(signals.head.id.get == 13)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "be possible to get all signals between to id's" in {
    Get("/streams/" + streamID + "/signals?fromId=" + 4 + "&toId=" + 9).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 4)
      assert(signals.head.id.get == 8)
      assert(signals.last.id.get == 5)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "be possible to get all signals before a specified id's" in {
    Get("/streams/" + streamID + "/signals?toId=" + 9).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 8)
      assert(signals.head.id.get == 8)
      assert(signals.last.id.get == 1)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "be possible to get all signals before a time" in {
    Get("/streams/" + streamID + "/signals?beforeTime=" + (TestData.timestamp - 80000l)).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals =  responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 4)
      assert(signals.head.id.get == 4)
      assert(signals.last.id.get == 1)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "return a error when a invalid combination of parameters are used (fromID, afterTime)" in {
    Get("/streams/" + streamID + "/signals?fromId=" + 1 + "&afterTime=" + (TestData.timestamp - 80000l))
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
  }

  it should "return a error when a invalid combination of parameters are used (fromId, beforeTime)" in {
    Get("/streams/" + streamID + "/signals?fromId=" + 1 + "&beforeTime=" + (TestData.timestamp - 80000l))
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
  }

  it should "return a error when a invalid combination of parameters are used (toId, beforeTime)" in {
    Get("/streams/" + streamID + "/signals?toId=" + 1 + "&beforeTime=" + (TestData.timestamp - 80000l))
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
  }

  it should "return a error when a invalid combination of parameters are used (toId, afterTime)" in {
    Get("/streams/" + streamID + "/signals?toId=" + 1 + "&afterTime=" + (TestData.timestamp - 80000l))
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
  }

  it should "return a error when a invalid combination of parameters are used (lastN should always be alone)" in {
    Get("/streams/" + streamID + "/signals?lastN=" + 1 + "&afterTime=" + (TestData.timestamp - 80000l))
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
    Get("/streams/" + streamID + "/signals?lastN=" + 1 + "&beforeTime=" + (TestData.timestamp - 80000l))
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
    Get("/streams/" + streamID + "/signals?lastN=" + 1 + "&toId=" + (TestData.timestamp - 80000l))
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
    Get("/streams/" + streamID + "/signals?lastN=" + 1 + "&fromId=" + (TestData.timestamp - 80000l))
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
    Get("/streams/" + streamID + "/signals?lastN=" + 1 + "&afterTime=" + (TestData.timestamp - 80000l) +
      "&beforeTime=" + (TestData.timestamp - 80000l)).addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
    Get("/streams/" + streamID + "/signals?lastN=" + 1 + "&afterTime=" + (TestData.timestamp - 80000l) + "&toId=" + 1)
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
    Get("/streams/" + streamID + "/signals?lastN=" + 1 + "&afterTime=" + (TestData.timestamp - 80000l) + "&fromId=" + 1)
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
    Get("/streams/" + streamID + "/signals?lastN=" + 1 + "&fromId=" + 2 + "&toId=" + 1)
      .addHeader(globalRequestIDHeader) ~> routes ~> check {
      status shouldBe BadRequest
      val responds = responseAs[String]
      assert(responds.contains("invalid combination of parameters"))
    }
  }
}
