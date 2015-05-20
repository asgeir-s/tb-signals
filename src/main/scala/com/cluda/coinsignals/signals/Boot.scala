package com.cluda.coinsignals.signals

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import com.cluda.coinsignals.signals.getsignal.DatabaseReaderActor
import com.cluda.coinsignals.signals.postsignal.{Step3_WriteDatabaseAndNotifyActor, _}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._


object Boot extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  override val timeout = Timeout(2 minutes)

  override val databaseWriterActor = system.actorOf(Props[Step3_WriteDatabaseAndNotifyActor])
  override val getPriceActor = system.actorOf(Step2_GetPriceTimeActor.props(databaseWriterActor))
  override val getExchangeActor = system.actorOf(Step1_GetExchangeActor.props(getPriceActor))
  override val databaseReaderActor: ActorRef = system.actorOf(Props[DatabaseReaderActor])

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}