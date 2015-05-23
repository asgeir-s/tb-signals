package com.cluda.coinsignals.signals

import com.cluda.coinsignals.signals.model.{Meta, Signal}

object TestData {
  val timestamp = 1432380666636L
  val signal1 = Signal(Some(1), 1, timestamp, BigDecimal(234.453), BigDecimal(0), BigDecimal(100))

  val signalSeq = Seq(
    Signal(Some(13), 1, timestamp, BigDecimal(234.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(12), 0, timestamp - 10000l, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(11), 1, timestamp - 20000l, BigDecimal(234.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(10), 0, timestamp - 30000l, BigDecimal(224.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(9), -1, timestamp - 40000l, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(8), 0, timestamp - 50000l, BigDecimal(264.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(7), -1, timestamp - 60000l, BigDecimal(184.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(6), 0, timestamp - 70000l, BigDecimal(154.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(5), 1, timestamp - 80000l, BigDecimal(194.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(4), 0, timestamp - 90000l, BigDecimal(254.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(3), 1, timestamp - 100000l, BigDecimal(304.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(2), 0, timestamp - 110000l, BigDecimal(404.453), BigDecimal(0), BigDecimal(100)),
    Signal(Some(1), 0, timestamp - 110000l, BigDecimal(404.453), BigDecimal(0), BigDecimal(100))
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
