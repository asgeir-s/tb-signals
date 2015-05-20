package com.cluda.coinsignals.signals

import com.cluda.coinsignals.signals.model.Signal

object TestData {
  val timestamp = System.currentTimeMillis()
  val signal1 = Signal(Some(1), 1, timestamp, BigDecimal(234.453), BigDecimal(0), BigDecimal(100))

  val signalSeq = Seq(
    Signal(Some(13), 1, System.currentTimeMillis(), BigDecimal(234.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(12), 0, System.currentTimeMillis()-10000, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(11), 1, System.currentTimeMillis()-20000, BigDecimal(234.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(10), 0, System.currentTimeMillis()-30000, BigDecimal(224.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(9), -1, System.currentTimeMillis()-40000, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(8), 0, System.currentTimeMillis()-50000, BigDecimal(264.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(7), -1, System.currentTimeMillis()-60000, BigDecimal(184.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(6), 0, System.currentTimeMillis()-70000, BigDecimal(154.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(5), 1, System.currentTimeMillis()-80000, BigDecimal(194.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(4), 0, System.currentTimeMillis()-90000, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(3), 1, System.currentTimeMillis()-100000, BigDecimal(304.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(2), 0, System.currentTimeMillis()-110000, BigDecimal(404.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(1), 0, System.currentTimeMillis()-110000, BigDecimal(404.453), BigDecimal(0), BigDecimal(100))
  )


}
