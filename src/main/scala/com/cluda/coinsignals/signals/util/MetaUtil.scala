package com.cluda.coinsignals.signals.util

import akka.actor.ActorRef
import com.cluda.coinsignals.signals.model.Meta

object MetaUtil {

  def setRespondsActor(meta: Meta, actorRef: ActorRef) = {
    Meta(Some(actorRef), meta.streamID, meta.signal, meta.exchange, meta.price, meta.timestamp)
  }

  def setExchange(meta: Meta, exchange: String) = {
    Meta(meta.respondsActor, meta.streamID, meta.signal, Some(exchange), meta.price, meta.timestamp)
  }

  def setPriceTime(meta: Meta, price: BigDecimal, timestamp: Long) = {
    Meta(meta.respondsActor, meta.streamID, meta.signal, meta.exchange, Some(price), Some(timestamp))
  }

}


// case class Meta(respondsActor: Option[ActorRef], streamID: String, signal: Int, exchange: Option[String], price: Option[BigDecimal])