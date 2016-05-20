package com.cluda.tradersbit.signals.postsignal

import java.util.Calendar

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.cluda.tradersbit.signals.model.Meta
import com.cluda.tradersbit.signals.protocoll.SignalProcessingException
import com.cluda.tradersbit.signals.util.MetaUtil
import org.knowm.xchange.bitfinex.v1.BitfinexExchange
import org.knowm.xchange.bitstamp.BitstampExchange
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.service.polling.marketdata.PollingMarketDataService
import org.knowm.xchange.{Exchange, ExchangeFactory}

class Step2_GetPriceTimeActor(writeDatabaseActor: ActorRef) extends Actor with ActorLogging {

  var price: Map[String, BigDecimal] = Map("bitstamp" -> 0, "bitfinex" -> 0)
  var refreshTime: Map[String, Long] = Map("bitstamp" -> 0, "bitfinex" -> 0)


  def getPrice(globalRequestID: String, exchangeName: String): Option[(BigDecimal, Long)] = {
    val exchange: Option[Exchange] = {
      if (exchangeName == "bitstamp") {
        Some(ExchangeFactory.INSTANCE.createExchange(classOf[BitstampExchange].getName))
      }
      else if (exchangeName == "bitfinex") {
        Some(ExchangeFactory.INSTANCE.createExchange(classOf[BitfinexExchange].getName))
      }
      else {
        None
      }
    }
    if (exchange isDefined) {
      val marketDataService: PollingMarketDataService = exchange.get.getPollingMarketDataService
      if (refreshTime(exchangeName) + 2000 <= Calendar.getInstance().getTimeInMillis) {
        try {
          val tick = marketDataService.getTicker(CurrencyPair.BTC_USD)
          price = price.updated(exchangeName, tick.getAsk)
          refreshTime = refreshTime.updated(exchangeName, tick.getTimestamp.getTime)
          Some(price(exchangeName), refreshTime(exchangeName))
        } catch {
          case e: Throwable =>
            log.error(s"[$globalRequestID]: marketDataService failed to get the price")
            None
        }
      }
      else {
        Some(price(exchangeName), refreshTime(exchangeName))
      }
    }
    else {
      log.error(s"[$globalRequestID]: not valid exchange name")
      None
    }

  }

  override def receive: Receive = {
    case (globalRequestID: String, meta: Meta) =>
      log.info(s"[$globalRequestID]: Got meta: " + meta)
      val priceTime = getPrice(globalRequestID, meta.exchange.get)
      if (priceTime isDefined) {
        log.info(s"[$globalRequestID]: at time " + priceTime.get._2 + " price for " + meta.exchange.get + " is " + priceTime.get._1)
        writeDatabaseActor ! (globalRequestID, MetaUtil.setPriceTime(meta, priceTime.get._1, priceTime.get._2))
      }
      else {
        log.error(s"[$globalRequestID]: could not get price")
        meta.respondsActor.get ! SignalProcessingException(s"[$globalRequestID]: could not get price")
      }
  }
}

object Step2_GetPriceTimeActor {
  def props(databaseActor: ActorRef): Props = Props(new Step2_GetPriceTimeActor(databaseActor))
}