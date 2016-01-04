package com.cluda.tradersbit.signals.util

import com.cluda.tradersbit.signals.model.Signal
import com.cluda.tradersbit.signals.model.{Meta, Signal}
import com.typesafe.config.ConfigFactory


object SignalUtil {

  val config = ConfigFactory.load()

  val bitstampFee = config.getDouble("exchang.bitstamp.fee")
  val bitfinexFee = config.getDouble("exchang.bitfinex.fee")

  def newSignals(lastSignal: Signal, newSignalMeta: Meta): List[Signal] = {

    val id = None
    val signal = newSignalMeta.signal
    val timestamp = newSignalMeta.timestamp.get
    val price: BigDecimal = newSignalMeta.price.get

    val lastValue: BigDecimal = lastSignal.value
    val lastValueInclFee: BigDecimal = lastSignal.valueInclFee
    val lastPrice: BigDecimal = lastSignal.price
    val fee: BigDecimal = {
      if(newSignalMeta.exchange.get == "bitfinex") {
        bitfinexFee
      }
      else if (newSignalMeta.exchange.get == "bitstamp") {
        bitstampFee
      }
      else {
        println("unknown exchnage: " + newSignalMeta.exchange.getOrElse("") + ". Using 0.002 for fee")
        0.002
      }
    }

    if (signal == lastSignal.signal) {
      List()
    }
    else if (lastSignal.signal == 0) {
      List(Signal(id, signal, timestamp, price, 0, lastValue, -fee, lastValueInclFee * (1 - fee)))
    }
    else if (lastSignal.signal == 1 && signal == 0) {
      val priceChange = price - lastPrice
      val relativeChange = (BigDecimal(1) / lastPrice) * priceChange
      List(Signal(id, signal, timestamp, price, relativeChange, lastValue * (1 + relativeChange), relativeChange - fee, lastValueInclFee * (1 + (relativeChange - fee))))
    }
    else if (lastSignal.signal == -1 && signal == 0) {
      val priceChange = -(price - lastPrice)
      val relativeChange = (BigDecimal(1) / lastPrice) * priceChange
      List(Signal(id, signal, timestamp, price, relativeChange, lastValue * (1 + relativeChange), relativeChange - fee, lastValueInclFee * (1 + (relativeChange - fee))))
    }
    else if (lastSignal.signal == 1 && signal == -1) {
      val priceChange = price - lastPrice
      val relativeChange = (BigDecimal(1) / lastPrice) * priceChange
      val newValue = lastValue * (BigDecimal(1) + relativeChange)

      List(
        Signal(id, 0, timestamp, price, relativeChange, newValue, relativeChange - fee, lastValueInclFee * (1 + (relativeChange - fee))),
        Signal(id, signal, timestamp, price, 0, newValue, -fee, lastValueInclFee * (1 - fee))
      )
    }
    else if (lastSignal.signal == -1 && signal == 1) {
      val priceChange = -(price - lastPrice)
      val relativeChange = (BigDecimal(1) / lastPrice) * priceChange
      val newValue = lastValue * (BigDecimal(1) + relativeChange)

      List(
        Signal(id, 0, timestamp, price, relativeChange, newValue, relativeChange - fee, lastValueInclFee * (1 + (relativeChange - fee))),
        Signal(id, signal, timestamp, price, 0, newValue, -fee, lastValueInclFee * (1 - fee))
      )
    }
    else {
      List()
    }
  }
}
