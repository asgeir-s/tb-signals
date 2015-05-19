package com.cluda.coinsignals.signals.messaging

import com.cluda.coinsignals.signals.model.Signal

object TestData {
  val timestamp = System.currentTimeMillis()
  val signal1 = Signal(Some("test-id"), 1, timestamp, BigDecimal(234.453), BigDecimal(0), BigDecimal(100))

}
