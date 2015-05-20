package com.cluda.coinsignals.signals.service

import akka.actor.{ActorRef, Props}
import akka.event.Logging
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import com.cluda.coinsignals.signals.Service
import com.cluda.coinsignals.signals.getsignal.DatabaseReaderActor
import com.cluda.coinsignals.signals.postsignal.{Step1_GetExchangeActor, Step2_GetPriceTimeActor, Step3_WriteDatabaseAndNotifyActor}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._


trait TestService extends FlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = DEBUG"

  override def config = testConfig

  override val logger = Logging(system, getClass)

  override implicit val timeout: Timeout  = Timeout(2 minutes)

  implicit val routeTestTimeout = RouteTestTimeout(20 second)

  override val databaseWriterActor = system.actorOf(Props[Step3_WriteDatabaseAndNotifyActor])
  override val getPriceActor = system.actorOf(Step2_GetPriceTimeActor.props(databaseWriterActor))
  override val getExchangeActor = system.actorOf(Step1_GetExchangeActor.props(getPriceActor))
  override val databaseReaderActor: ActorRef = system.actorOf(Props[DatabaseReaderActor])
}
