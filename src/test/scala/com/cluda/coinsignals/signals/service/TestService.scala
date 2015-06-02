package com.cluda.coinsignals.signals.service

import akka.actor.{ActorRef, Props}
import akka.event.Logging
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestKit
import akka.util.Timeout
import com.cluda.coinsignals.signals.Service
import com.cluda.coinsignals.signals.getsignal.DatabaseReaderActor
import com.cluda.coinsignals.signals.postsignal.{Step1_StreamInfoActor, NotifyActor, Step2_GetPriceTimeActor, Step3_WriteDatabaseActor}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._


trait TestService extends FlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = DEBUG"

  override val config = ConfigFactory.load("application-test")
  override val logger = Logging(system, getClass)
  implicit val routeTestTimeout = RouteTestTimeout(20.second)

  override implicit val timeout: Timeout  = Timeout(2.minutes)

  override val notificationActor = system.actorOf(Props[NotifyActor])
  override val databaseWriterActor = system.actorOf(Step3_WriteDatabaseActor.props(notificationActor))
  override val getPriceActor = system.actorOf(Step2_GetPriceTimeActor.props(databaseWriterActor))
  override val getExchangeActor = system.actorOf(Step1_StreamInfoActor.props(getPriceActor))
  override val databaseReaderActor: ActorRef = system.actorOf(Props[DatabaseReaderActor])

  implicit val context = system.dispatcher

  def afterTest(): Unit ={}

  override protected def afterAll() {
    afterTest()
    TestKit.shutdownActorSystem(system)
    super.afterAll()
    system.shutdown()
  }

}
