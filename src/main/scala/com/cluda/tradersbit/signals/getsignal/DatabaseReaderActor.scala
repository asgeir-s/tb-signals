package com.cluda.tradersbit.signals.getsignal

import akka.actor.{Actor, ActorLogging}
import com.cluda.tradersbit.signals.protocoll._
import com.cluda.tradersbit.signals.database.SignalTable
import com.cluda.tradersbit.signals.model.Signal
import com.cluda.tradersbit.signals.protocoll.{DatabaseReadException, InalidCombinationOfParametersException, GetSignalsParams}
import com.typesafe.config.ConfigFactory
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class DatabaseReaderActor extends Actor with ActorLogging {

  val database: JdbcBackend.DatabaseDef = Database.forConfig("database", ConfigFactory.load())

  implicit val executionContext: ExecutionContext = context.system.dispatcher

  override def receive: Receive = {
    case (globalRequestID: String, GetSignals(streamID: String, paramseters: GetSignalsParams)) =>
      log.info(s"[$globalRequestID]: Got GetSignals with stream id " + streamID + " and getSignalsParams set to " + paramseters)
      val s = sender()
      val signalsTable = TableQuery[SignalTable]((tag: Tag) => new SignalTable(tag, streamID))

      database.run(MTable.getTables(streamID)).map { tables =>
        if (tables.nonEmpty) {

          if (!paramseters.hasParameters) {
            database.run(signalsTable.sortBy(_.id.desc).result).map {
              case signals: Seq[Signal] =>
                s !signals
            }.recover {
              case _ =>
                log.error(s"[$globalRequestID]: Error from database. Probably database for the stream does not exist")
                s ! DatabaseReadException("error from database. Probably database for the stream does not exist")
            }
          }
          else if (paramseters.isValid) {
            val query: slick.lifted.Query[SignalTable, Signal, Seq] = {
              if (paramseters.lastN isDefined) {
                signalsTable.sortBy(_.id.desc).take(paramseters.lastN.get)
              }
              else if (paramseters.fromId isDefined) {
                if (paramseters.toId isDefined) {
                  signalsTable.filter(x => x.id > paramseters.fromId.get && x.id < paramseters.toId.get).sortBy(_.id.desc)
                }
                else {
                  signalsTable.filter(_.id > paramseters.fromId.get).sortBy(_.id.desc)
                }
              }
              else if (paramseters.afterTime isDefined) {
                if (paramseters.beforeTime isDefined) {
                  //println("afterTime:" + paramseters.afterTime.get + " && beforeTime:" + paramseters.beforeTime.get)
                  signalsTable.filter(x => x.timestamp > paramseters.afterTime.get && x.timestamp < paramseters.beforeTime.get).sortBy(_.id.desc)
                }
                else {
                  //println("afterTime:" + paramseters.afterTime.get)
                  signalsTable.filter(_.timestamp > paramseters.afterTime.get).sortBy(_.id.desc)
                }
              }

              else if (paramseters.toId isDefined) {
                signalsTable.filter(x => x.id < paramseters.toId.get).sortBy(_.id.desc)
              }

              else if (paramseters.beforeTime isDefined) {
                //println("toTime:" + paramseters.beforeTime.get)
                signalsTable.filter(_.timestamp < paramseters.beforeTime.get).sortBy(_.id.desc)
              }
              else if (paramseters.onlyClosed isDefined) {
                signalsTable.sortBy(_.id.desc)
              }
              else {
                log.error(s"[$globalRequestID]: Valid parameters was defined but dodent match any combination. Error!! Returning no signals.")
                signalsTable.take(0)
              }
            }

            database.run(query.result).map {
              case signals: Seq[Signal] =>
                if (paramseters.onlyClosed.isDefined && paramseters.onlyClosed.get) {
                  if (signals.head.signal != 0) {
                    s ! signals.drop(1)
                  }
                  else {
                    s ! signals
                  }
                }
                else {
                  s ! signals
                }
            }.recover {
              case _ =>
                log.error(s"[$globalRequestID]: Error from database. Probably database for the stream does not exist")
                s ! DatabaseReadException("error from database. Probably database for the stream does not exist")
            }
          }
          else {
            log.error(s"[$globalRequestID]: Invalid combination of parameters: " + paramseters)
            s ! InalidCombinationOfParametersException("invalid combination of parameters: " + paramseters)
          }
        }
        else {
          s !  Seq[Signal]()
        }
      }


  }
}
