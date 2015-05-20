package com.cluda.coinsignals.signals.protocoll

case class GetSignals(streamID: String, maxReturnSize: Option[Int] = None)
