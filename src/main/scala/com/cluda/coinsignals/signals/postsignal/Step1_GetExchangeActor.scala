package com.cluda.coinsignals.signals.postsignal

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import com.cluda.coinsignals.signals.model.Meta
import com.cluda.coinsignals.signals.protocoll.SignalProcessingException
import com.cluda.coinsignals.signals.util.MetaUtil

import scala.concurrent.Future


/**
 * Used to receive a streams exchange (thereby also checking existence)
 *
 */
class Step1_GetExchangeActor(getPriceActor: ActorRef) extends Actor with ActorLogging {
  
  // TODO: this is just a dummy always returning "bitstamp". Should be implemented when the stats-info service is up and running
  
  implicit val executor = context.system.dispatcher
  implicit val system = context.system
  implicit val materializer = ActorFlowMaterializer()


  def doGet(host: String, port: Int, path: String)(implicit system: ActorSystem, mat: FlowMaterializer): Future[HttpResponse] = {
    val conn: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] = Http().outgoingConnection(host, port)
    val request = HttpRequest(GET, uri = path)
    Source.single(request).via(conn).runWith(Sink.head[HttpResponse])
  }

  def getExchange(streamID: String): Future[String] = {
    //doGet("signals-staging.elasticbeanstalk.com", 80, "/ping").map(x => getPriceActor ! Unmarshal(x.entity).to[String])
    Future("bitstamp")
  }

  override def receive: Receive = {
    case meta: Meta =>
      log.info("Step1_GetExchangeActor got meta: " + meta)
      getExchange(meta.streamID).map{
        case exchangeName: String =>
          getPriceActor ! MetaUtil.setExchange(meta, exchangeName)
      }.recover {
        case _ =>
          log.error("cold not get the exchange for the streamID: " + meta.streamID)
          meta.respondsActor.get ! SignalProcessingException("cold not get the exchange for the streamID: " + meta.streamID)
      }
  }
}

object Step1_GetExchangeActor {
  def props(getPriceActor: ActorRef): Props = Props(new Step1_GetExchangeActor(getPriceActor))
}