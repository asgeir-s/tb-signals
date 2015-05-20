package com.cluda.coinsignals.signals.getsignal

import akka.actor.{Actor, ActorLogging}
import com.cluda.coinsignals.signals.database.SignalTable
import com.cluda.coinsignals.signals.model.Signal
import com.cluda.coinsignals.signals.protocoll.{DatabaseReadException, GetSignals}
import com.typesafe.config.ConfigFactory
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class DatabaseReaderActor extends Actor with ActorLogging {

  val database: JdbcBackend.DatabaseDef = Database.forConfig("database", ConfigFactory.load())

  implicit val executionContext: ExecutionContext = context.system.dispatcher

  override def receive: Receive = {
    case GetSignals(streamID: String, maxReturnSize: Option[Int]) =>
      log.info("DatabaseReaderActor: got GetSignals with stream id " + streamID + " and maxReturnSize set to " + maxReturnSize)
      val s = sender()
      val signalsTable = TableQuery[SignalTable]((tag: Tag) => new SignalTable(tag, streamID))

      if (maxReturnSize isEmpty) {
        database.run(signalsTable.sortBy(_.id.desc).result).map {
          case signals: Seq[Signal] =>
            s ! signals
        }.recover {
          case _ =>
            log.error("DatabaseReaderActor: error from database. Probably database for the stream does not exist")
            s ! DatabaseReadException("error from database. Probably database for the stream does not exist")
        }
      }
      else {
        database.run(signalsTable.sortBy(_.id.desc).take(maxReturnSize.get).result).map {
          case signals: Seq[Signal] =>
            s ! signals
        }.recover {
          case _ =>
            log.error("DatabaseReaderActor: error from database. Probably database for the stream does not exist")
            s ! DatabaseReadException("error from database. Probably database for the stream does not exist")
        }
      }
  }
}
