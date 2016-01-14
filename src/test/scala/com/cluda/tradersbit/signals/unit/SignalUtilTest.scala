package com.cluda.tradersbit.signals.unit

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import com.cluda.tradersbit.signals.TestData
import com.cluda.tradersbit.signals.model.{SignalJsonProtocol, Meta, Signal}
import com.cluda.tradersbit.signals.util.SignalUtil


class SignalUtilTest extends UnitTest {

  val streamName = Some("test22")

  def signalsValuesEqual(signal1: Signal, signal2: Signal): Boolean = {
    signal1.change.toDouble == signal2.change.toDouble &&
      signal1.price == signal2.price &&
      signal1.signal == signal2.signal &&
      signal1.timestamp == signal2.timestamp &&
      signal1.value.toDouble == signal2.value.toDouble
  }

  "newSignals function" should
    "set the change and value correctly for new signals" in {

    val newSignals = SignalUtil.newSignals(TestData.signalSeqMath(5), TestData.metaSignalSeqMath(4))

    assert(newSignals.length == 1)
    assert(signalsValuesEqual(newSignals.head, TestData.signalSeqMath(4)))

    val newSignals2 = SignalUtil.newSignals(TestData.signalSeqMath(4), TestData.metaSignalSeqMath(3))

    assert(newSignals2.length == 1)
    assert(signalsValuesEqual(newSignals2.head, TestData.signalSeqMath(3)))

    val newSignals3 = SignalUtil.newSignals(TestData.signalSeqMath(3), TestData.metaSignalSeqMath(2))

    assert(newSignals3.length == 1)
    assert(signalsValuesEqual(newSignals3.head, TestData.signalSeqMath(2)))

    val newSignals4 = SignalUtil.newSignals(TestData.signalSeqMath(2), TestData.metaSignalSeqMath(1))

    assert(newSignals4.length == 1)
    assert(signalsValuesEqual(newSignals4.head, TestData.signalSeqMath(1)))

    val newSignals5 = SignalUtil.newSignals(TestData.signalSeqMath(1), TestData.metaSignalSeqMath(0))

    assert(newSignals5.length == 1)
    assert(signalsValuesEqual(newSignals5.head, TestData.signalSeqMath(0)))

  }

  "newSignals function" should
    "set valueIncluFee and changeInclFee correctly" in {

    val newSignals = SignalUtil.newSignals(
      Signal(Some(1), 1, 1432380666636L - 120000, BigDecimal(100), BigDecimal(0), BigDecimal(1), BigDecimal(-0.002), BigDecimal(1)),
      Meta(None, "test-id-math", 0, Some("bitfinex"), Some(200), Some(1432380666636L - 120000), None, streamName))

    import SignalJsonProtocol._
    import spray.json._

    assert(newSignals.length == 1)
    assert(newSignals.head.value == 2)
    assert(newSignals.head.valueInclFee == 1.998)
    assert(newSignals.head.change == 1)
    assert(newSignals.head.changeInclFee == 0.998)
  }

  }
