package com.cluda.coinsignals.signals.util

import akka.actor.ActorRef
import com.cluda.coinsignals.signals.model.Meta

object MetaUtil {

  def setRespondsActor(meta: Meta, actorRef: ActorRef) = {
    Meta(Some(actorRef), meta.streamID, meta.signal, meta.exchange, meta.price, meta.timestamp, meta.awsARN)
  }

  def setExchangeAndARN(meta: Meta, exchange: String,  arn: String) = {
    Meta(meta.respondsActor, meta.streamID, meta.signal, Some(exchange.replace("\"", "")), meta.price, meta.timestamp, Some(arn.replace("\"", "")))
  }

  def setPriceTime(meta: Meta, price: BigDecimal, timestamp: Long) = {
    Meta(meta.respondsActor, meta.streamID, meta.signal, meta.exchange, Some(price), Some(timestamp), meta.awsARN)
  }

}


// case class Meta(respondsActor: Option[ActorRef], streamID: String, signal: Int, exchange: Option[String], price: Option[BigDecimal])