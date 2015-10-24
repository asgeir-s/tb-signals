package com.cluda.tradersbit.signals.model

import spray.json.DefaultJsonProtocol

/**
 *
 * @param signal 1 = long, 0 = close, -1 = short
 */
case class Signal(id: Option[Long], signal: Int, timestamp: Long, price: BigDecimal, change: BigDecimal, value: BigDecimal)

object SignalJsonProtocol extends DefaultJsonProtocol {
  implicit val signalFormat = jsonFormat6(Signal)
}