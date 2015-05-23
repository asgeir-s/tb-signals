package com.cluda.coinsignals.signals

import com.cluda.coinsignals.signals.database.SignalTable
import com.typesafe.config.ConfigFactory
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{JdbcBackend, StaticQuery}
import slick.lifted.TableQuery

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

object DatabaseUtil {

  def dropTableIfItExists(tableName: String, executionContext: ExecutionContext): Unit = {
    val database: JdbcBackend.DatabaseDef = Database.forConfig("database", ConfigFactory.load())
    implicit val session = database.createSession()
    implicit val ec = executionContext

    try {
      (StaticQuery.u + "DROP TABLE " + tableName).execute
    } catch {
      case _: Throwable =>
        // ignore
    }
  }

  def createDummySignalsTable(streamID: String, executionContext: ExecutionContext): Unit = {
    val database: JdbcBackend.DatabaseDef = Database.forConfig("database", ConfigFactory.load())
    implicit val session = database.createSession()
    implicit val ec = executionContext

    val signalsTable = TableQuery[SignalTable]((tag: Tag) => new SignalTable(tag, streamID))

    database.run(signalsTable.schema.create)

    Await.result(database.run(
      signalsTable ++= TestData.signalSeq.reverse
    ) map(x => println("database created")), 5 seconds)
  }

}
