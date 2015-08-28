package com.cluda.coinsignals.signals

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.cluda.coinsignals.signals.getsignal.DatabaseReaderActor
import com.cluda.coinsignals.signals.postsignal.{Step3_WriteDatabaseActor, _}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._


object Boot extends App with Service {
  override implicit val system = ActorSystem()
  override implicit def executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  override val timeout = Timeout(2.minutes)

  override val httpNotifyActor = system.actorOf(Props[HttpNotifierActor])
  override val notificationActor = system.actorOf(NotifyActor.props(httpNotifyActor))
  override val databaseWriterActor = system.actorOf(Step3_WriteDatabaseActor.props(notificationActor))
  override val getPriceActor = system.actorOf(Step2_GetPriceTimeActor.props(databaseWriterActor))
  override val getExchangeActor = system.actorOf(Step1_StreamInfoActor.props(getPriceActor))
  override val databaseReaderActor: ActorRef = system.actorOf(Props[DatabaseReaderActor])

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}