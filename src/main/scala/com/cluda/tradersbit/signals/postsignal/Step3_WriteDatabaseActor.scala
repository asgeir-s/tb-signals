package com.cluda.tradersbit.signals.postsignal

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.cluda.tradersbit.signals.model.Signal
import com.cluda.tradersbit.signals.database.SignalTable
import com.cluda.tradersbit.signals.model.{Meta, Signal}
import com.cluda.tradersbit.signals.protocoll.SignalProcessingException
import com.cluda.tradersbit.signals.util.SignalUtil
import com.typesafe.config.ConfigFactory
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.{Failure, Success}

class Step3_WriteDatabaseActor(notificationActor: ActorRef) extends Actor with ActorLogging {

  val database: JdbcBackend.DatabaseDef = Database.forConfig("database", ConfigFactory.load())
  implicit val executionContext: ExecutionContext = context.system.dispatcher

  def prosseccSignals(signalsTable: TableQuery[SignalTable], newSignals: List[Signal]): Future[Seq[Signal]] = {
    val promise = Promise[Seq[Signal]]
    database.run(signalsTable ++= newSignals).onComplete {
      case Success(_) =>
        database.run(signalsTable.sortBy(_.id.desc).take(newSignals.length).result).map {
          case theNewSignals: Seq[Signal] =>
            promise.success(theNewSignals)
        }
        case Failure(e: Throwable) =>
          promise.failure(e)
    }
    promise.future
  }

  override def receive: Receive = {
    case (globalRequestID: String, meta: Meta) =>
      log.info(s"[$globalRequestID]: Got meta: " + meta)
      val signalsTable = TableQuery[SignalTable]((tag: Tag) => new SignalTable(tag, meta.streamID))

      database.run(signalsTable.schema.create) onComplete {
        // creates the table if it does not exist // TODO: should only do if does not exist
        case _ =>
          database.run(signalsTable.length.result).map { length =>
            if (length == 0) {
              log.info(s"[$globalRequestID]: This is the first signal for this stream. Adding to DB. Meta: " + meta.toString)
              prosseccSignals(signalsTable, List(Signal(None, meta.signal, meta.timestamp.get, meta.price.get, 0, 1, 0, 1))).map { newSignalsWithId =>
                meta.respondsActor.get ! newSignalsWithId
                notificationActor !(globalRequestID, meta.streamID, meta.awsARN.get, meta.streamName.get, newSignalsWithId)
                log.info(s"[$globalRequestID]: Signal added to DB. And sent to the 'notificationActor'. For stream: " + meta.streamID + ".")
              }.recover {
                case e: Throwable => log.error(s"[$globalRequestID]: Problem writing signal to DB. Error: " + e.toString)
              }

            }
            else {
              database.run(signalsTable.sortBy(_.id.desc).result.head) map { lastSignal: Signal =>
                val newSignals = SignalUtil.newSignals(lastSignal, meta)
                if (newSignals.nonEmpty) {
                  prosseccSignals(signalsTable, newSignals).map {newSignalsWithId =>
                    meta.respondsActor.get ! newSignalsWithId
                    notificationActor ! (globalRequestID, meta.streamID, meta.awsARN.get, meta.streamName.get, newSignalsWithId)
                    log.info(s"[$globalRequestID]: Signals added to DB. And sent to the 'notificationActor'. For stream: " + meta.streamID + ".")
                  }.recover {
                    case e: Throwable => log.error(s"[$globalRequestID]: Problem writing signals to DB. Error: " + e.toString)
                  }
                }
                else {
                  log.error(s"[$globalRequestID]: No signals to add. Possibly: position is already taken. Conflict - Duplicate")
                  meta.respondsActor.get ! SignalProcessingException(s"[$globalRequestID]: No signals to add. Possibly: position is already taken. Conflict - Duplicate")
                }
              }
            }
          }.recover {
            case e: Throwable => log.error(s"[$globalRequestID]: Problem getting table length for stream with id: " + meta.streamID + ". Error: " + e.toString)
          }
      }
  }

}

object Step3_WriteDatabaseActor {
  def props(notificationActor: ActorRef): Props = Props(new Step3_WriteDatabaseActor(notificationActor))
}