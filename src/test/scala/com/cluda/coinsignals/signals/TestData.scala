package com.cluda.coinsignals.signals

import com.cluda.coinsignals.signals.model.{Meta, Signal}

object TestData {
  val timestamp = System.currentTimeMillis()
  val signal1 = Signal(Some(1), 1, timestamp, BigDecimal(234.453), BigDecimal(0), BigDecimal(100))

  val signalSeq = Seq(
    Signal(Some(13), 1, System.currentTimeMillis(), BigDecimal(234.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(12), 0, System.currentTimeMillis() - 10000, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(11), 1, System.currentTimeMillis() - 20000, BigDecimal(234.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(10), 0, System.currentTimeMillis() - 30000, BigDecimal(224.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(9), -1, System.currentTimeMillis() - 40000, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(8), 0, System.currentTimeMillis() - 50000, BigDecimal(264.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(7), -1, System.currentTimeMillis() - 60000, BigDecimal(184.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(6), 0, System.currentTimeMillis() - 70000, BigDecimal(154.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(5), 1, System.currentTimeMillis() - 80000, BigDecimal(194.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(4), 0, System.currentTimeMillis() - 90000, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(3), 1, System.currentTimeMillis() - 100000, BigDecimal(304.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(2), 0, System.currentTimeMillis() - 110000, BigDecimal(404.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(1), 0, System.currentTimeMillis() - 110000, BigDecimal(404.453), BigDecimal(0), BigDecimal(100))
  )

  val signalSeqMath = Seq(
    Signal(Some(6), 0, timestamp - 70000, BigDecimal(975), BigDecimal(0.5), BigDecimal(1.5)),
    Signal(Some(5), 1, timestamp - 80000, BigDecimal(650), BigDecimal(0), BigDecimal(1)),
    Signal(Some(4), 0, timestamp - 90000, BigDecimal(450), BigDecimal(-0.5), BigDecimal(1)),
    Signal(Some(3), -1, timestamp - 100000, BigDecimal(300), BigDecimal(0), BigDecimal(2)),
    Signal(Some(2), 0, timestamp - 110000, BigDecimal(200), BigDecimal(1), BigDecimal(2)),
    Signal(Some(1), 1, timestamp - 120000, BigDecimal(100), BigDecimal(0), BigDecimal(1))
  )

  val metaSignalSeqMath = Seq(
    Meta(None, "test-id-math", 0, Some("bitstamp"), Some(975), Some(timestamp - 70000)),
    Meta(None, "test-id-math", 1, Some("bitstamp"), Some(650), Some(timestamp - 80000)),
    Meta(None, "test-id-math", 0, Some("bitstamp"), Some(450), Some(timestamp - 90000)),
    Meta(None, "test-id-math", -1, Some("bitstamp"), Some(300), Some(timestamp - 100000)),
    Meta(None, "test-id-math", 0, Some("bitstamp"), Some(200), Some(timestamp - 110000)),
    Meta(None, "test-id-math", 1, Some("bitstamp"), Some(100), Some(timestamp - 120000))
  )

}
