package com.cluda.coinsignals.signals.protocoll

case class GetSignals(
  streamID: String,
  params: GetSignalsParams = GetSignalsParams()
  )

case class GetSignalsParams(
  fromId: Option[Long] = None,
  toId: Option[Long] = None,
  afterTime: Option[Long] = None,
  beforeTime: Option[Long] = None,
  lastN: Option[Int] = None
  ) {
  def hasParameters: Boolean =
    fromId.isDefined ||
      toId.isDefined ||
      afterTime.isDefined ||
      beforeTime.isDefined ||
      lastN.isDefined

  def isValid: Boolean = {

    if (
      fromId.isDefined && afterTime.isDefined ||
      fromId.isDefined && beforeTime.isDefined ||
      toId.isDefined && beforeTime.isDefined ||
      toId.isDefined && afterTime.isDefined
    ) {
      false
    }

    else if (
      lastN.isDefined &&
      (fromId.isDefined ||
        toId.isDefined ||
        afterTime.isDefined ||
        beforeTime.isDefined)
    ) {
      false
    }

    else {
      true
    }
  }
}
