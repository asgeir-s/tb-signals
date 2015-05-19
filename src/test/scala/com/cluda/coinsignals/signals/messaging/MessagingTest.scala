package com.cluda.coinsignals.signals.messaging

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * All unit tests should extend this.
 */
abstract class MessagingTest extends TestKit(ActorSystem("test")) with FlatSpecLike with ImplicitSender with Matchers with
OptionValues with Inside with Inspectors with BeforeAndAfterAll {

  implicit val timeout = Timeout(10 second)
  implicit val context = system.dispatcher

  override protected def afterAll() {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
    system.shutdown()
  }

}


