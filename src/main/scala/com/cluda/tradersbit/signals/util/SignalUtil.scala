package com.cluda.tradersbit.signals.util

import com.cluda.tradersbit.signals.model.Signal
import com.cluda.tradersbit.signals.model.{Meta, Signal}


object SignalUtil {

  def newSignals(lastSignal: Signal, newSignalMeta: Meta): List[Signal] = {

    val id = None
    val signal = newSignalMeta.signal
    val timestamp = newSignalMeta.timestamp.get
    val price: BigDecimal = newSignalMeta.price.get


    val lastValue: BigDecimal = lastSignal.value
    val lastPrice: BigDecimal = lastSignal.price

    if (signal == lastSignal.signal) {
      List()
    }
    else if (lastSignal.signal == 0) {
      List(Signal(id, signal, timestamp, price, 0, lastValue))
    }
    else if (lastSignal.signal == 1 && signal == 0) {
      val priceChange = price - lastPrice
      val relativeChange = (BigDecimal(1) / lastPrice) * priceChange
      List(Signal(id, signal, timestamp, price, relativeChange, lastValue * (1 + relativeChange)))
    }
    else if (lastSignal.signal == -1 && signal == 0) {
      val priceChange = -(price - lastPrice)
      val relativeChange = (BigDecimal(1) / lastPrice) * priceChange
      List(Signal(id, signal, timestamp, price, relativeChange, lastValue * (1 + relativeChange)))
    }
    else if (lastSignal.signal == 1 && signal == -1) {
      val priceChange = price - lastPrice
      val relativeChange = (BigDecimal(1) / lastPrice) * priceChange
      val newValue = lastValue * (BigDecimal(1) + relativeChange)

      List(
        Signal(id, 0, timestamp, price, relativeChange, newValue),
        Signal(id, signal, timestamp, price, 0, newValue)
      )
    }
    else if (lastSignal.signal == -1 && signal == 1) {
      val priceChange = -(price - lastPrice)
      val relativeChange = (BigDecimal(1) / lastPrice) * priceChange
      val newValue = lastValue * (BigDecimal(1) + relativeChange)

      List(
        Signal(id, 0, timestamp, price, relativeChange, newValue),
        Signal(id, signal, timestamp, price, 0, newValue)
      )
    }
    else {
      List()
    }
  }
}
