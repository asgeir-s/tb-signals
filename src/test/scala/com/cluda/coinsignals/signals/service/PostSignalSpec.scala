package com.cluda.coinsignals.signals.service

import akka.actor.Props
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import com.cluda.coinsignals.signals.Service
import com.cluda.coinsignals.signals.model.{Signal, SignalJsonProtocol}
import com.cluda.coinsignals.signals.postsignal.{Step1_GetExchangeActor, Step2_GetPriceTimeActor, Step3_WriteDatabaseAndNotifyActor}
import org.scalatest._

import scala.concurrent.duration._



class PostSignalSpec extends FlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = DEBUG"

  override def config = testConfig

  override val logger = Logging(system, getClass)

  override implicit val timeout = Timeout(2 minutes)

  implicit val routeTestTimeout = RouteTestTimeout(20 second)

  override val databaseActor = system.actorOf(Props[Step3_WriteDatabaseAndNotifyActor])
  override val getPriceActor = system.actorOf(Step2_GetPriceTimeActor.props(databaseActor))
  override val getExchangeActor = system.actorOf(Step1_GetExchangeActor.props(getPriceActor))

  it should "make sure the last signal is CLOSE" in {
    Post("/streams/test-id/signals", """0""") ~> routes ~> check {
      val respondsBack: String = responseAs[String]
      assert(respondsBack.length >= 0)
    }
  }

  it should "responds withe the written signal (with id, price, timestamp etc.)" in {
    Post("/streams/test-id/signals", """1""") ~> routes ~> check {
      status shouldBe Accepted
      import SignalJsonProtocol._
      import spray.json._
      val signals = responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 1)
      assert(signals.head.id != None)
      assert(signals.head.signal == 1)
      assert(signals.head.price > 0)
      assert(signals.head.timestamp > 11100000L)
    }
  }

  it should "responds with the written signals SHORT (with id, price, timestamp etc.)" in {
    Post("/streams/test-id/signals", """-1""") ~> routes ~> check {
      status shouldBe Accepted
      import SignalJsonProtocol._
      import spray.json._
      val signals = responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 2)
      assert(signals(0).id != None)
      assert(signals(0).signal == -1)
      assert(signals(0).price > 0)
      assert(signals(0).timestamp > 11100000L)

      assert(signals(1).id != None)
      assert(signals(1).signal == 0)
      assert(signals(1).price > 0)
      assert(signals(1).timestamp > 11100000L)
    }
  }

  it should "responds with the written signal CLOSE (with id, price, timestamp etc.)" in {
    Post("/streams/test-id/signals", """0""") ~> routes ~> check {
      status shouldBe Accepted
      import SignalJsonProtocol._
      import spray.json._
      val signals = responseAs[String].parseJson.convertTo[List[Signal]]

      assert(signals.length == 1)
      assert(signals(0).id != None)
      assert(signals(0).signal == 0)
      assert(signals(0).price > 0)
      assert(signals(0).timestamp > 11100000L)
    }
  }

  it should "responds with status 'Conflict' (when the sendt signal is the same as the last signal)" in {
    Post("/streams/test-id/signals", """0""") ~> routes ~> check {
      status shouldBe Conflict
      val error = responseAs[String]
      assert(error.contains("duplicate"))
    }
  }

  // TODO: Should be implemented when the stats-info service is up and running
  /*
  it should "responds withe an error when the stream with the provieded ID does not exist in the 'Stream Info Service'" in {
    Post("/streams/test-lal-unreal/signals", """0""") ~> routes ~> check {
      status shouldBe InternalServerError
      val error = responseAs[String]
      assert(error.contains("error"))
    }
  }
  */

}