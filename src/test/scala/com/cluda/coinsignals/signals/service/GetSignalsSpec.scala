package com.cluda.coinsignals.signals.service

import akka.http.scaladsl.model.StatusCodes._
import com.cluda.coinsignals.signals.DatabaseUtil
import com.cluda.coinsignals.signals.model.{Signal, SignalJsonProtocol}

class GetSignalsSpec extends TestService {

  val streamID = "getsignalsspec"

  override def beforeAll(): Unit = {
    DatabaseUtil.dropTableIfItExists("notexisting", system.dispatcher)
    DatabaseUtil.dropTableIfItExists(streamID, system.dispatcher)
    DatabaseUtil.createDummySignalsTable(streamID, system.dispatcher)
  }

  it should "responds withe all the signals for the given stream with the given ID" in {
    Get("/streams/" + streamID + "/signals") ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals = responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 13)
      assert(signals.head.id isDefined)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

  it should "responds withe an error when the theire is no stream with the given streamID" in {
    Get("/streams/" + "notexisting" + "/signals") ~> routes ~> check {
      status shouldBe NoContent
    }
  }

  it should "on status request responds the last signal stream with the given ID" in {
    Get("/streams/" + streamID + "/status") ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals = responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 1)
      assert(signals.head.id isDefined)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100L)
    }
  }

}
