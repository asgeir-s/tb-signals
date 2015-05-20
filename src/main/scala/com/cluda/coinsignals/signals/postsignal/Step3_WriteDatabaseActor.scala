package com.cluda.coinsignals.signals.postsignal

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.cluda.coinsignals.signals.database.SignalTable
import com.cluda.coinsignals.signals.model.{Meta, Signal}
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException
import com.cluda.coinsignals.signals.util.SignalUtil
import com.typesafe.config.ConfigFactory
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class Step3_WriteDatabaseActor(notificationActor: ActorRef) extends Actor with ActorLogging {

  val database: JdbcBackend.DatabaseDef = Database.forConfig("database", ConfigFactory.load())

  implicit val executionContext: ExecutionContext = context.system.dispatcher


  override def receive: Receive = {
    case meta: Meta =>
      log.info("Step3_WriteDatabaseAndNotifyActor got meta: " + meta)
      val signalsTable = TableQuery[SignalTable]((tag: Tag) => new SignalTable(tag, meta.streamID))

      val future = database.run(signalsTable.schema.create) // creates the table if it does not exist

      future onComplete {
        case _ =>
          database.run(signalsTable.length.result) map { length =>
            if (length == 0) {
              database.run(signalsTable += Signal(None, meta.signal, meta.timestamp.get, meta.price.get, 0, 1)) map {
                case _ =>
                  database.run(signalsTable.sortBy(_.id.desc).take(1).result) map {
                    case theNewSignals: Seq[Signal] =>
                      meta.respondsActor.get ! theNewSignals
                      notificationActor ! theNewSignals
                  }
              }
            }
            else {
              database.run(signalsTable.sortBy(_.id.desc).result.head) map { lastSignal:Signal =>
                val newSignals = SignalUtil.newSignals(lastSignal, meta)
                if(newSignals.length > 0) {
                  database.run(signalsTable ++= newSignals) map {
                    case _ =>
                      database.run(signalsTable.sortBy(_.id.desc).take(newSignals.length).result) map {
                        case theNewSignals: Seq[Signal] =>
                          meta.respondsActor.get ! theNewSignals
                          notificationActor ! theNewSignals
                      }
                  }
                }
                else {
                  log.error("No signals to add. Possibly: position is already taken. Conflict - Duplicate")
                  meta.respondsActor.get ! SignalProcessingException("No signals to add. Possibly: position is already taken. Conflict - Duplicate")
                }
              }
            }
          }
      }
  }

}

object Step3_WriteDatabaseActor {
  def props(notificationActor: ActorRef): Props = Props(new Step3_WriteDatabaseActor(notificationActor))
}