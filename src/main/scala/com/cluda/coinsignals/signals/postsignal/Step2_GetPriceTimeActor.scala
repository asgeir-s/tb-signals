package com.cluda.coinsignals.signals.postsignal

import java.util.Calendar

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.cluda.coinsignals.signals.model.Meta
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException
import com.cluda.coinsignals.signals.util.MetaUtil
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange
import com.xeiam.xchange.bitstamp.BitstampExchange
import com.xeiam.xchange.currency.CurrencyPair
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService
import com.xeiam.xchange.{Exchange, ExchangeFactory}

class Step2_GetPriceTimeActor(writeDatabaseActor: ActorRef) extends Actor with ActorLogging {

  var price: Map[String, BigDecimal] = Map("bitstamp" -> 0, "bitfinex" -> 0)
  var refreshTime: Map[String, Long] = Map("bitstamp" -> 0, "bitfinex" -> 0)


  def getPrice(exchangeName: String): Option[(BigDecimal, Long)] = {
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
            log.error("marketDataService failed to get the price")
            None
        }
      }
      else {
        Some(price(exchangeName), refreshTime(exchangeName))
      }
    }
    else {
      log.error("not valid exchange name")
      None
    }

  }

  override def receive: Receive = {
    case meta: Meta =>
      log.info("Step2_GetPriceTimeActor got meta: " + meta)
      val priceTime = getPrice(meta.exchange.get)
      if (priceTime isDefined) {
        log.info("at time " + priceTime.get._2 + " price for " + meta.exchange.get + " is " + priceTime.get._1)
        writeDatabaseActor ! MetaUtil.setPriceTime(meta, priceTime.get._1, priceTime.get._2)
      }
      else {
        meta.respondsActor.get ! SignalProcessingException("could not get price")
      }
  }
}

object Step2_GetPriceTimeActor {
  def props(databaseActor: ActorRef): Props = Props(new Step2_GetPriceTimeActor(databaseActor))
}