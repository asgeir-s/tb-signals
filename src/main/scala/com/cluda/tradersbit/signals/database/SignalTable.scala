package com.cluda.tradersbit.signals.database

import com.cluda.tradersbit.signals.model.Signal
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

class SignalTable(tag: Tag, streamId: String) extends Table[Signal](tag, streamId.toString) {
  val dbBigDecimalType = "DECIMAL(13,10)"

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def signal = column[Int]("signal")

  def timestamp = column[Long]("timestamp")

  def price = column[BigDecimal]("price", O.SqlType("DECIMAL(10,4)"))

  def change = column[BigDecimal]("change", O.SqlType(dbBigDecimalType))

  def value = column[BigDecimal]("value", O.SqlType(dbBigDecimalType))

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id.?, signal, timestamp, price, change, value) <>(Signal.tupled, Signal.unapply)
}