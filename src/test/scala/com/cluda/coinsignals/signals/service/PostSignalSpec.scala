package com.cluda.coinsignals.signals.service

import akka.http.scaladsl.model.StatusCodes._
import com.cluda.coinsignals.signals.DatabaseUtilBlockingForTests
import com.cluda.coinsignals.signals.model.{Signal, SignalJsonProtocol}


class PostSignalSpec extends TestService {
  val streamID = config.getString("testSignalStream.id")

  override def beforeAll(): Unit = {
    DatabaseUtilBlockingForTests.dropTableIfItExists(streamID, system.dispatcher)
  }

  it should "prohibit duplicate" in {
    Post("/streams/" + streamID + "/signals", "0") ~> routes ~> check {
      val signals = responseAs[String]
    }
  }

  it should "responds withe the written signal (with id, price, timestamp etc.)" in {
    Post("/streams/" + streamID + "/signals", "1") ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals = responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 1)
      assert(signals.head.id.isDefined)
      assert(signals.head.signal == 1)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100000L)
    }
  }

  it should "responds with the written signals SHORT (with id, price, timestamp etc.)" in {
    Post("/streams/" + streamID + "/signals", "-1") ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals = responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 2)
      assert(signals(0).id.isDefined)
      assert(signals(0).signal == -1)
      assert(signals(0).price > 0)
      assert(signals(0).timestamp > 11100000L)

      assert(signals(1).id.isDefined)
      assert(signals(1).signal == 0)
      assert(signals(1).price > 0)
      assert(signals(1).timestamp > 11100000L)
    }
  }

  it should "responds with the written signal CLOSE (with id, price, timestamp etc.)" in {
    Post("/streams/" + streamID + "/signals", "0") ~> routes ~> check {
      status shouldBe OK
      import SignalJsonProtocol._
      import spray.json._
      val signals = responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 1)
      assert(signals.head.id.isDefined)
      assert(signals.head.signal == 0)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100000L)
    }
  }

  it should "responds with status 'Conflict' (when the sendt signal is the same as the last signal)" in {
    Post("/streams/" + streamID + "/signals", "0") ~> routes ~> check {
      status shouldBe Conflict
      val error = responseAs[String]
      assert(error.contains("duplicate"))
    }
  }


  it should "responds withe an error when the stream with the provieded ID does not exist in the 'Stream Info Service'" in {
    Post("/streams/" + "thisisnotreal" + "/signals", "0") ~> routes ~> check {
      status shouldBe NotFound
    }
  }


}